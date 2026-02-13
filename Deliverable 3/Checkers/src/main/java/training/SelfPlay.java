package training;

import game.Board;
import game.GameResult;
import mcts.MCTS;
import model.NeuralNet;

import java.util.ArrayList;
import java.util.List;

public class SelfPlay {
    private NeuralNet net;
    private MCTS mcts;

    public SelfPlay(NeuralNet net) {
        this.net = net;
        this.mcts = new MCTS(net);
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
            double[] policy = mcts.run(board);

            examples.add(new TrainingExample(board.splitBoardChannels(), policy, !board.isWhiteToMove()));

            // TODO pick action with sampling early, then argmax later
            double bestMove = argmax(policy);

            // TODO apply bestMove to board
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
     * Gets the max value (not idx) from a policy array
     *
     * @param policy the policy probability distribution
     * @return the max value from the policy array
     */
    private Double argmax(double[] policy) {
        if (policy == null || policy.length == 0) {
            return Double.NaN;
        }

        double max = policy[0];

        for (double p : policy) {
            max = Math.max(max, p);
        }

        return max;
    }
}
