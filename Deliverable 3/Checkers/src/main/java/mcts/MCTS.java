package mcts;

import game.Board;
import game.GameResult;
import model.NeuralNet;

public class MCTS {

    private static final double C_PUCT = 1.4;
    private static final int SIMULATIONS = 100; // arbitrary number of simulations to run for every search of ONE ouputted prior policy

    private NeuralNet net;

    /**
     * Runs an MCTS on a board, doing x simulations (set on initialization) to determine the best move
     * @param startingBoard The current board (where the player has not moved yet)
     * @return a score distribution for each move (picking the highest = picking the best move)
     */
    public double[] run (Board startingBoard) {

        Node root = new Node(1.0);

        for (int i = 0; i < SIMULATIONS; i++) {
            simulate(root, startingBoard);
        }

        return new double[5];
    }

    private double simulate(Node node, Board board) {

        GameResult result;


        return 3;
    }
}
