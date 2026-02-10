package game;

import java.util.ArrayList;
import java.util.List;

public class Board {

    public int[][] cells; //[row index][column index], row are counted from white side to black side
    public boolean whiteToMove;

    private static int MAN_VALUE = 1;
    private static int KING_VALUE = 2;

    int[][] KING_DIRS = {
            {-1, -1}, {-1, +1},
            {+1, -1}, {+1, +1}
    };


    public Board(int[][] cells, boolean whiteToMove) {
        this.cells = cells;
        this.whiteToMove = whiteToMove;
    }

    public Board() {
        this.cells = getStartingBoard();
        this.whiteToMove = true;
    }

    private static int[][] getStartingBoard() {
        //empty board
        int[][] newCells = new int[8][8];
        //place pieces
        for (int rowIdx = 0; rowIdx < 8; rowIdx++) {
            //no pieces in the middle 2 rows
            if (rowIdx == 3 || rowIdx == 4) {
                continue;
            }
            //row[0] starts with a valid square and alternates every row, place pieces space out by 1
            for (int xIdx = (rowIdx % 2 == 0 ? 0 : 1); xIdx < 8; xIdx += 2) {
                newCells[rowIdx][xIdx] = rowIdx < 3 ? MAN_VALUE : -MAN_VALUE;
            }
        }
        return newCells;
    }

    @Override
    //The array string is flipped so the board can be perceived through the white player's point of view
    public String toString() {
        String str = String.format("%s to move \n", whiteToMove ? "White" : "Black");
        for (int rowIdx = 7; rowIdx >= 0; rowIdx--) {
            for (int xIdx = 0; xIdx < 8; xIdx++) {
                str += String.format("[%4s]", cells[rowIdx][xIdx]);
            }
            str += "\n";
        }
        return str;
    }

    public void applyAction(Action action) {
        Coordinate start = action.getStart();

        int piece = getPieceAt(start);
        if (piece == 0) {
            return;
        }
        Coordinate destination = action.getDestination();
        Coordinate capturedCoordinate = action.getCaptureCoordinate();
        cells[start.getY()][start.getX()] = 0;
        cells[destination.getY()][destination.getX()] = piece;
        if (capturedCoordinate != null) {
            cells[capturedCoordinate.getY()][capturedCoordinate.getX()] = 0;
        }
    }


    /**
     * Gets the action space of the piece
     * @param coordinate coordinate of the piece
     * @param piece the value of the piece (1 for man, 2 for king, positive for white, negative for black)
     * @return a list of possible actions for the piece (or null if invalid arguments)
     */
    public List<Action> getPieceActionSpace(Coordinate coordinate, int piece) {
        if (coordinate.isInvalid() || !isPieceValid(piece)) {
            return null;
        }
        List<Action> actionSpace = new ArrayList<>();
        List<Action> pieceActions = (Math.abs(piece) > 1 ? coordinate.getPossibleKingActions() : coordinate.getPossibleManActions(piece > 0));
        for (Action action : pieceActions) {
            Coordinate moveDestination = action.getDestination();
            int destinationPiece = getPieceAt(moveDestination);
            if (destinationPiece == 0) {
                actionSpace.add(action);
                continue;
            }
            Coordinate captureDestination = moveDestination.addedWith(action.getDeltaCoordinate());
            if (arePiecesSameTeam(destinationPiece, piece) && getPieceAt(captureDestination) == 0) {
                actionSpace.add(new Action(action.getStart(), captureDestination));
            }
        }
        return actionSpace;
    }

    /**
     * Gets the move space of the piece
     *
     * @param coordinate       coordinate of the piece
     * @param piece            the value of the piece (1 for man, 2 for king, positive for white, negative for black)
     * @param previousCaptures [used for recursion], stores the previous captures made by the piece (if the move follows a capture)
     * @return a list of possible moves for the piece (or null if invalid arguments)
     */
    public List<Move> getPieceMoveSpace(Coordinate coordinate, int piece, List<Coordinate> previousCaptures) {
        if (coordinate.isInvalid() || !isPieceValid(piece)) {
            return null;
        }
        List<Move> moveSpace = new ArrayList<>();
        List<Action> pieceActions = (Math.abs(piece) > 1 ? coordinate.getPossibleKingActions() : coordinate.getPossibleManActions(piece > 0));
        for (Action action : pieceActions) {
            Coordinate moveDestination = action.getDestination();
            int destinationPiece = getPieceAt(moveDestination);
            //if there is no piece on the square, the move is valid
            if (destinationPiece == 0 && (previousCaptures == null || previousCaptures.isEmpty())) {
                moveSpace.add(new Move(action));
                continue;
            }
            Coordinate captureDestination = action.getDestination().addedWith(action.getDeltaCoordinate());
            //if there is a piece on the square, check if it can be captured
            if (!captureDestination.isInvalid() && !arePiecesSameTeam(destinationPiece, piece)) {
                List<Coordinate> captures = previousCaptures == null ? new ArrayList<>() : previousCaptures;
                captures.add(action.getDestination());
                moveSpace.add(new Move(action.getStart(), captures));
                //after captures, can move again
                List<Move> nextActionSpace = getPieceMoveSpace(captureDestination, piece, captures);
                moveSpace.addAll(nextActionSpace);
            }
        }
        return moveSpace;
    }

    /**
     * Gets the move space for every peace for a player
     *
     * @param whiteToMove the player to find actions for
     * @return a list of every possible move for the player
     */
    public List<Move> getGlobalMoveSpace(boolean whiteToMove) {
        List<Move> globalMoveSpace = new ArrayList<>();
        //Assuming piece values: Man = 1, King = 2 (+ for white, - for black)
        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[0].length; col++) {
                int piece = cells[row][col];
                //if no piece or piece color is not the color of the player who moves, skip
                if (piece == 0 || (piece < 0 == whiteToMove)) {
                    continue;
                }
                Coordinate coordinate = new Coordinate(col, row);
                globalMoveSpace.addAll(getPieceMoveSpace(coordinate, piece, null));
            }
        }
        return globalMoveSpace;
    }




    public int getPieceAt(Coordinate coordinate) {
        return cells[coordinate.getY()][coordinate.getX()];
    }

    public static boolean arePiecesSameTeam(int piece1, int piece2) {
        return piece1 > 0 == piece2 > 0 && !(piece1 == 0 || piece2 == 0);
    }

    public static boolean isPieceValid(int piece) {
        return piece > -3 && piece < 3 && piece != 0;
    }

    /**
     * Splits the board cells into 4 channels:
     * 0: ALLY men
     * 1: ALLY king
     * 2: OPPONENT men
     * 3: OPPONENT king
     * <p>
     * Ally / Opponent assumes that it is from the white player's POV
     *
     * @return the board with pieces split into 4 channels
     */
    public double[][][] splitBoardChannels() {
        double[][][] board = new double[4][8][8];

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int val = cells[r][c];

                // ALLY men
                if (val == 1) {
                    board[0][r][c] = 1;
                }

                // ALLY king
                else if (val == 2) {
                    board[1][r][c] = 1;
                }

                // OPPONENT men
                else if (val == -1) {
                    board[2][r][c] = 1;
                }

                // OPPONENT king
                else if (val == -2) {
                    board[3][r][c] = 1;
                }
            }
        }
        return board;
    }

}
