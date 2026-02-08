package game;

import java.util.Arrays;
import java.util.List;

public class Board {

    public int[] cells;
    public int currentPlayer;

    int[][] RED_DIRS = { {-1, -1}, {-1, +1} };
    int[][] BLACK_DIRS = { {+1, -1}, {+1, +1} };

    int[][] KING_DIRS = {
            {-1, -1}, {-1, +1},
            {+1, -1}, {+1, +1}
    };


    public Board(int[] cells, int player) {
        this.cells = cells;
        this.currentPlayer = player;
    }

    public Board() {
        this.cells = initializeBoard();
        this.currentPlayer = 1;
    }

    private int[] initializeBoard() {
        int[] board = new int[64];


        for (int colIdx = 0; colIdx < 8; colIdx += 2) {
            board[idx(0, colIdx + 1)] = -1;
            board[idx(1, colIdx)] = -1;
        }

        for (int colIdx = 0; colIdx < 8; colIdx += 2) {
            board[idx(6, colIdx + 1)] = 1;
            board[idx(7, colIdx)] = 1;
        }

        return board;
    }

    public double[] convertToDoubleArray() {
        return Arrays.stream(cells).asDoubleStream().toArray();
    }

//    public List<Move> getLegalMoves() {
//
//    }

    /**
     * Applies a move to a board and returns a new board
     * Assumes the move is already legal
     * @param move a legal move
     * @return the new board state
     */
    public Board applyMove(Move move) {
        cells[move.to] = move.from;

        currentPlayer = -currentPlayer;

        return new Board(cells, currentPlayer);
    }

    public static int idx(int r, int c) {
        return r * 8 + c;
    }

    public static int row(int idx) {
        return idx / 8;
    }

    public static int col(int idx) {
        return idx % 8;
    }



}
