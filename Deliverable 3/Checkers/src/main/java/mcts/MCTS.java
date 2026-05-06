package mcts;

import java.util.List;

import Util.Tuple;
import game.Board;
import game.GameResult;
import game.Move;
import game.MoveResult;
import model.NeuralNet;
import model.PolicyValue;

public class MCTS {

    private static final double C_PUCT = 1.4;
    private int SIMULATIONS = 100; // arbitrary number of simulations to run for every search of ONE ouputted prior policy

    private NeuralNet net;

    public MCTS(NeuralNet net) {
        this.net = net;
    }

    /**
     * Runs an MCTS on a board, doing x simulations (set on initialization) to determine the best move
     *
     * @param startingBoard The current board (where the player has not moved yet)
     * @return a score distribution for each move (picking the highest = picking the best move)
     */
    public Tuple<double[], List<Move>> run(Board startingBoard) {
        Node root = new Node(1.0);

        for (int i = 0; i < SIMULATIONS; i++) {
            simulate(root, startingBoard);
        }

        List<Move> legalMoves = startingBoard.getBoardMoveSpace();
        double[] policy = new double[legalMoves.size()];
        double sum = 0.0;

        for (int i = 0; i < legalMoves.size(); i++) {
            Move m = legalMoves.get(i);
            Node child = root.children.get(m);
            policy[i] = (child == null) ? 0.0 : child.visitCount;
            sum += policy[i];
        }

        // Normalize
        if (sum > 0.0) {
            for (int i = 0; i < policy.length; i++) {
                policy[i] /= sum;
            }
        } else if (!legalMoves.isEmpty()) {
            double uniform = 1.0 / legalMoves.size();
            for (int i = 0; i < policy.length; i++) {
                policy[i] = uniform;
            }
        }

        return new Tuple<>(policy, legalMoves);
    }

    private double simulate(Node node, Board board) {

        GameResult result = board.getGameResult();

        if (result != GameResult.ONGOING) {
            return terminalValue(board, result);
        }

        if (!node.isExpanded()) {
            return expand(node, board);
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

        Board nextBoard = board.getBoardDuplicate();
        MoveResult moveResult = nextBoard.applyMove(bestMove);
        if (moveResult == null || bestChild == null) {
            return 0;
        }
        double value = -simulate(bestChild, nextBoard);

        bestChild.visitCount++;
        bestChild.valueSum += value;

        node.visitCount++;
        node.valueSum += value;

        return value;
    }

    private double expand(Node node, Board board) {
        PolicyValue pv = net.forward(board.splitBoardChannels());

        double[] policy = pv.policy;
        double value = pv.value;

        List<Move> legalMoves = board.getBoardMoveSpace();

        int[] globalMoveIndices = new int[legalMoves.size()];
        double legalPolicySum = 0.0;
        for (int moveIdx = 0; moveIdx < legalMoves.size(); moveIdx++) {
            Move move = legalMoves.get(moveIdx);
            int idx = -1;

            for (int i = 0; i < Move.GLOBAL_MOVE_SPACE.length; i++) {
                if (Move.GLOBAL_MOVE_SPACE[i].equals(move)) {
                    idx = i;
                    break;
                }
            }

            if (idx == -1) {
                throw new ArrayIndexOutOfBoundsException("Index not found for a move in the global action space: " + move.toString());
            }

            globalMoveIndices[moveIdx] = idx;
            legalPolicySum += policy[idx];
        }

        for (int moveIdx = 0; moveIdx < legalMoves.size(); moveIdx++) {
            Move move = legalMoves.get(moveIdx);
            int idx = globalMoveIndices[moveIdx];
            double policyAmount = legalPolicySum > 0.0 ? policy[idx] / legalPolicySum : (1.0 / legalMoves.size());
            node.children.put(move, new Node(policyAmount));
        }

        return value;
    }

    private double terminalValue(Board board, GameResult result) {
        if (result == GameResult.DRAW) return 0.0;

        boolean whiteToMove = board.isWhiteToMove();
        boolean playerToMoveWon = (result == GameResult.WHITE_WIN && whiteToMove)
                || (result == GameResult.BLACK_WIN && !whiteToMove);

        if (playerToMoveWon) return 1.0;
        if (result == GameResult.WHITE_WIN || result == GameResult.BLACK_WIN) return -1.0;
        return 0.0;
    }

    public int getSIMULATIONS() {
        return SIMULATIONS;
    }

    public void setSIMULATIONS(int SIMULATIONS) {
        this.SIMULATIONS = SIMULATIONS;
    }
}
