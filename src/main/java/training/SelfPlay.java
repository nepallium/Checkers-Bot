package training;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Util.Tuple;
import game.Board;
import game.GameResult;
import game.Move;
import mcts.MCTS;
import model.NeuralNet;

public class SelfPlay {
    private MCTS mcts;
    private NeuralNet net;

    /**
     * Constructor expects an MCTS object already initialized with a NN object
     * @param mcts the initialized MCTS object
     */
    public SelfPlay(MCTS mcts) {
        this.mcts = mcts;
    }

    public SelfPlay(NeuralNet net) {
        this.net = net;
        this.mcts = new MCTS(net);
    }

    /**
     * Plays a single game using MCTS. Stores a TrainingExample for every move in that game
     * @return the training dataset from a single self-play game
     */
    public List<TrainingExample> playOneGame() {
        MCTS search = this.mcts != null ? this.mcts : (this.net != null ? new MCTS(this.net) : null);
        if (search == null) {
            throw new IllegalStateException("SelfPlay requires either a non-null MCTS or NeuralNet");
        }

        Board board = new Board();

        List<TrainingExample> examples = new ArrayList<>();

        int moveCount = 0;
        boolean noLegalMoves = false;
        boolean noMoveWinnerIsWhite = false;
        while (board.getGameResult() == GameResult.ONGOING) {
            moveCount++;
//            System.out.println("Result: " + board.getGameResult());

            // gets mcts policy, aka not improved
            Tuple<double[], List<Move>> output = search.run(board);
            double[] policy = output.e1;

            List<Move> moves = output.e2;

            TrainingExample example = new TrainingExample(board.splitBoardChannels(), policy, board.isWhiteToMove());
            example.legalMoves = moves;
            examples.add(example);

            // TODO pick action with sampling early, then argmax later

            if (policy == null) {
                System.out.println("POLICY IS NULL");
            } else if (policy.length == 0) {
                System.out.println("No legal moves found");
                noLegalMoves = true;
                noMoveWinnerIsWhite = !board.isWhiteToMove();
                break;
            }

            int bestMoveIdx = argmax(policy);

            board.applyMove(moves.get(bestMoveIdx));
//            System.out.println("Move: " + moves.get(bestMoveIdx).toString());
        }

//        System.out.println("Result: " + board.getGameResult());

        // UPDATE z-value for training examples
        GameResult winner = board.getGameResult();
        int factor = 0; // init at 0 == GameResult.DRAW
        // since a TrainingExample's player is stored as boolean whiteToMove
        // +1 white, -1 black, 0 draw
        if (noLegalMoves) {
            factor = noMoveWinnerIsWhite ? 1 : -1;
        } else if (GameResult.WHITE_WIN == winner) {
            factor = 1;
        } else if (GameResult.BLACK_WIN == winner) {
            factor = -1;
        }

        for (TrainingExample ex : examples) {
            // z is now "winner from player-to-move at that state" POV
            double player = ex.whiteToMove ? 1.0 : -1.0;
            ex.z = factor * player;
        }

        System.out.println("Game over after " + moveCount + " moves, result: " + board.getGameResult());
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
            System.out.println("POLICY : " + Arrays.toString(policy));
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
