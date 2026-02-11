package mcts;

import game.Action;
import game.Board;
import game.GameResult;
import game.Move;
import model.NeuralNet;
import model.PolicyValue;

import java.util.List;

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

        GameResult result = board.getGameResult();

        if (result != GameResult.ONGOING) {
            // terminal state reached
        }

        if (!node.isExpanded()) {
            // expand node, go back to start
        }

        // select highest ucb score
        Move bestMove = null;
        Node bestChild = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Move move : node.children.keySet()) {
            Node child = node.children.get(move);

            double ucb = child.getQ() + C_PUCT * child.prior * Math.sqrt(node.visitCount) / (1 + child.visitCount);

            if (ucb > bestScore) {
                // new best found, replace
                bestScore = ucb;
                bestMove = move;
                bestChild = child;
            }
        }

        Board nextBoard = board.applyMove(bestMove);
        double value = -simulate(bestChild, nextBoard);

        node.visitCount++;
        node.valueSum += value;

        return value;
    }

    private double expand(Node node, Board board) {
        PolicyValue pv = net.forward(board);

        double[] policy = pv.policy;
        double value = pv.value;

        List<Move> legalMoves = board.getBoardMoveSpace(board.isWhiteToMove());

        double sum = 0.0;

        for (double pol : policy) {
            sum += pol;
        }

        for (Move move : legalMoves) {
            Action firstAction = move.getActions().getFirst();

            int idx = -1;

            for (int i = 0; i < Action.globalActionSpace.size(); i++) {
                if (Action.globalActionSpace.get(i).equals(firstAction)) {
                    idx = i;
                    break;
                }
            }

            double policyAmount = policy[idx] / sum;
            node.children.put(move, new Node(policyAmount));
        }

        return value;
    }

    private double terminalValue(GameResult result) {
        if (result == GameResult.WIN) return 1.0;
        if (result == GameResult.LOSS) return -1.0;
        return 0.0;
    }
}
