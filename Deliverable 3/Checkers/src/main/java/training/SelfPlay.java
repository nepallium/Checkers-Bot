package training;

import Util.Tuple;
import game.Board;
import game.GameResult;
import game.Move;
import game.MoveResult;
import mcts.MCTS;

import java.util.ArrayList;
import java.util.List;

public class SelfPlay {
    private MCTS mcts;

    /**
     * Constructor expects an MCTS object already initialized with a NN object
     * @param mcts the initialized MCTS object
     */
    public SelfPlay(MCTS mcts) {
        this.mcts = mcts;
    }

    /**
     * Plays a single game using MCTS. Stores a TrainingExample for every move in that game
     * @return the training dataset from a single self-play game
     */
    public List<TrainingExample> playOneGame() {
        Board board = new Board();

        List<TrainingExample> examples = new ArrayList<>();

        while (board.getGameResult() == GameResult.ONGOING) {
            // gets mcts policy, aka not improved
            Tuple<double[], List<Move>> output = mcts.run(board);
            double[] policy = output.e1;
            List<Move> moves = output.e2;

            examples.add(new TrainingExample(board.splitBoardChannels(), policy, !board.isWhiteToMove()));

            // TODO pick action with sampling early, then argmax later
            int bestMoveIdx = argmax(policy);

            board.applyMove(moves.get(bestMoveIdx));
        }

        // UPDATE z-value for training examples
        GameResult winner = board.getGameResult();
        int factor = 0; // init at 0 == GameResult.DRAW
        // since a TrainingExample's player is stored as boolean whiteToMove
        // +1 white, -1 black, 0 draw
        if (GameResult.WHITE_WIN == winner) {
            factor = 1;
        } else if (GameResult.BLACK_WIN == winner) {
            factor = -1;
        }

        for (TrainingExample ex : examples) {
            // z is now "winner from player-to-move at that state" POV
            double player = ex.whiteToMove ? 1.0 : -1.0;
            ex.z = factor * player;
        }

        return examples;
    }

    /**
     * Gets the index of the max value from the policy array
     *
     * @param policy the policy probability distribution
     * @return the max value from the policy array
     */
    private int argmax(double[] policy) {
        if (policy == null || policy.length == 0) {
            return -1;
        }

        double max = policy[0];
        int idx = 0;

        for (int i = 1; i < policy.length; i++) {
            if (policy[i] > max) {
                idx = i;
                max = policy[i];
            }
        }

        return idx;
    }
}
