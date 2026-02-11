package game;

import Util.Tuple;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Board {

    public int[][] cells; //[row index][column index], row are counted from white side to black side
    public boolean whiteToMove;
    public Coordinate forcedPieceCaptureCoordinate = null;

    private static final int MAN_VALUE = 1;
    private static final int KING_VALUE = 2;
    private MoveLog moveLog;
    private PositionLog positionLog;

    public Board(int[][] cells, boolean whiteToMove) {
        this.cells = cells;
        this.whiteToMove = whiteToMove;
    }

    public Board(int positionLogCount) {
        this.cells = getStartingBoard();
        this.whiteToMove = true;
        this.moveLog = new MoveLog();
        this.positionLog = new PositionLog(positionLogCount); //save up to 3 positions for the CNN
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
        str += String.format("\nLOGS\n%s", moveLog.toString());
        return str;
    }

    public void applyAction(Action action) {
        Coordinate start = action.getStart();

        int piece = getPieceAt(start);
        if (piece == 0 || piece > 0 != whiteToMove || (forcedPieceCaptureCoordinate != null && !start.equals(forcedPieceCaptureCoordinate))) {
            return;
        }
        positionLog.addPosition(cells);
        Coordinate destination = action.getDestination();
        Coordinate capturedCoordinate = action.getCaptureCoordinate();
        cells[start.getY()][start.getX()] = 0;
        cells[destination.getY()][destination.getX()] = ((destination.getY() == 7 && piece == 1) || (destination.getY() == 0 && piece == -1)) ? (piece == 1 ? KING_VALUE : -KING_VALUE) : piece;

        if (capturedCoordinate == null) {
            whiteToMove = !whiteToMove;
            forcedPieceCaptureCoordinate = null;
            moveLog.addActionLog(action, true);
            return;
        }
        System.out.println(capturedCoordinate);
        cells[capturedCoordinate.getY()][capturedCoordinate.getX()] = 0;
        List<Action> chainActionSpace = getPieceActionSpace(destination, piece).e1;

        forcedPieceCaptureCoordinate = chainActionSpace.isEmpty() ? null : action.getDestination();
        moveLog.addActionLog(action, chainActionSpace.isEmpty());
        whiteToMove = chainActionSpace.isEmpty() != whiteToMove;
    }


    /**
     * Gets the action space of the piece
     *
     * @param coordinate coordinate of the piece
     * @param piece      the value of the piece (1 for man, 2 for king, positive for white, negative for black)
     * @return two lists of all possible actions for the piece with no captures and captures (captures, non captures)
     */
    public Tuple<List<Action>, List<Action>> getPieceActionSpace(Coordinate coordinate, int piece) {
        if (coordinate.isInvalid() || isPieceInvalid(piece)) {
            return null;
        }
        List<Action> captureActions = new ArrayList<>();
        List<Action> nonCaptureActions = new ArrayList<>();
        List<Action> pieceActions = (Math.abs(piece) > 1 ? coordinate.getPossibleKingActions() : coordinate.getPossibleManActions(piece > 0));
        boolean captureAvailable = false;
        for (Action action : pieceActions) {
            Coordinate moveDestination = action.getDestination();
            int destinationPiece = getPieceAt(moveDestination);
            if (destinationPiece == 0) {
                if (!captureAvailable) {
                    nonCaptureActions.add(action);
                }
                continue;
            }
            Coordinate captureDestination = moveDestination.addedWith(action.getDeltaCoordinate());
            if (arePiecesOpposite(destinationPiece, piece) && getPieceAt(captureDestination) == 0) {
                captureAvailable = true;
                captureActions.add(new Action(action.getStart(), captureDestination));
            }
        }
        return new Tuple<List<Action>, List<Action>>(captureActions, nonCaptureActions);
    }

    public List<Action> getBoardActionSpace() {
        if (forcedPieceCaptureCoordinate != null) {
            List<Action> actionSpace = getPieceActionSpace(forcedPieceCaptureCoordinate, getPieceAt(forcedPieceCaptureCoordinate)).e1;
            return actionSpace.isEmpty() ? List.of(new Action(forcedPieceCaptureCoordinate, forcedPieceCaptureCoordinate)) : actionSpace;
        }
        List<Action> captureActions = new ArrayList<>();
        List<Action> nonCaptureActions = new ArrayList<>();
        boolean captureAvailable = false;
        for (int rowIdx = 0; rowIdx < 8; rowIdx++) {
            for (int xIdx = (rowIdx % 2 == 0 ? 0 : 1); xIdx < 8; xIdx += 2) {
                int piece = cells[rowIdx][xIdx];
                //if no piece or piece color is not the color of the player who moves, skip
                if (piece == 0 || (piece < 0 == whiteToMove)) {
                    continue;
                }
                Coordinate coordinate = new Coordinate(xIdx, rowIdx);
                Tuple<List<Action>, List<Action>> pieceActionSpace = getPieceActionSpace(coordinate, piece);
                if (!pieceActionSpace.e1.isEmpty()) {
                    captureAvailable = true;
                }
                if (captureAvailable) {
                    captureActions.addAll(pieceActionSpace.e1);
                } else {
                    nonCaptureActions.addAll(pieceActionSpace.e2);
                }
            }
        }
        System.out.println(captureAvailable);
        return captureAvailable ? captureActions : nonCaptureActions;
    }

    /**
     * Gets the move space of the piece
     *
     * @param coordinate coordinate of the piece
     * @param piece      the value of the piece (1 for man, 2 for king, positive for white, negative for black)
     *                   //* @param previousCaptures [used for recursion], stores the previous captures made by the piece (if the move follows a capture)
     * @return two lists of all possible moves for the piece (captures, non captures)
     */
    public Tuple<List<Move>, List<Move>> getPieceMoveSpace(Coordinate coordinate, int piece) {
        if (coordinate.isInvalid() || isPieceInvalid(piece)) {
            return null;
        }
        List<Move> captureMoves = new ArrayList<>();
        List<Move> nonCaptureMoves = new ArrayList<>();
        boolean captureAvailable = false;
        List<Action> pieceActions = (Math.abs(piece) > 1 ? coordinate.getPossibleKingActions() : coordinate.getPossibleManActions(piece > 0));
        for (Action action : pieceActions) {
            Coordinate moveDestination = action.getDestination();
            int destinationPiece = getPieceAt(moveDestination);
            //if there is no piece on the square, the move is valid
            if (destinationPiece == 0) {
                if (!captureAvailable) {
                    nonCaptureMoves.add(new Move(action));
                }
                continue;
            }
            Coordinate captureDestination = action.getDestination().addedWith(action.getDeltaCoordinate());
            //if there is a piece on the square, check if it can be captured
            if (!captureDestination.isInvalid() && arePiecesOpposite(destinationPiece, piece)) {
                captureAvailable = true;
                captureMoves.add(new Move(action.getStart(), captureDestination));
                //after captures, can move again if is another capture
                Tuple<List<Move>, List<Move>> nextActionSpaces = getPieceMoveSpace(captureDestination, piece);
                for (Move captureMove : nextActionSpaces.e1) {
                    List<Action> moveActions = captureMove.getActions();
                    moveActions.addFirst(action);
                    captureMoves.add(new Move(moveActions));
                }
            }
        }
        return new Tuple<List<Move>, List<Move>>(captureMoves, nonCaptureMoves);
    }

    /**
     * Gets the move space for every peace for a player
     *
     * @param whiteToMove the player to find actions for
     * @return a list of every possible move for the player
     */
    public List<Move> getBoardMoveSpace(boolean whiteToMove) {
        List<Move> nonCaptureMoves = new ArrayList<>();
        List<Move> captureMoves = new ArrayList<>();
        boolean captureAvailable = false;
        //Assuming piece values: Man = 1, King = 2 (+ for white, - for black)
        for (int rowIdx = 0; rowIdx < 8; rowIdx++) {
            for (int xIdx = (rowIdx % 2 == 0 ? 0 : 1); xIdx < 8; xIdx += 2) {
                int piece = cells[rowIdx][xIdx];
                //if no piece or piece color is not the color of the player who moves, skip
                if (piece == 0 || (piece < 0 == whiteToMove)) {
                    continue;
                }
                Coordinate coordinate = new Coordinate(xIdx, rowIdx);
                Tuple<List<Move>, List<Move>> pieceMoveSpaces = getPieceMoveSpace(coordinate, piece);
                if (!pieceMoveSpaces.e1.isEmpty()) {
                    captureAvailable = true;
                }
                if (captureAvailable) {
                    captureMoves.addAll(pieceMoveSpaces.e1);
                } else {
                    nonCaptureMoves.addAll(pieceMoveSpaces.e2);
                }
            }
        }
        return captureAvailable ? captureMoves : nonCaptureMoves;
    }


    public int getPieceAt(Coordinate coordinate) {
        if (coordinate.isInvalid()) {
            return 0;
        }
        return cells[coordinate.getY()][coordinate.getX()];
    }

    public static boolean arePiecesOpposite(int piece1, int piece2) {
        return piece1 > 0 != piece2 > 0 || (piece1 == 0 || piece2 == 0);
    }

    public static boolean isPieceInvalid(int piece) {
        return piece <= -3 || piece >= 3 || piece == 0;
    }

    /**
     * * Splits the board cells into 4 channels:
     * 0: ALLY men
     * 1: ALLY king
     * 2: OPPONENT men
     * 3: OPPONENT king
     * * <p>
     *
     * @param cells       cells of the board
     * @param whiteAsAlly whether to consider white as an ally (or black)
     * @return [board channel]([][] board)
     */
    public static double[][][] splitBoardChannels(int[][] cells, boolean whiteAsAlly) {
        double[][][] board = new double[4][8][8];

        for (int rowIdx = 0; rowIdx < 8; rowIdx++) {
            for (int xIdx = (rowIdx % 2 == 0 ? 0 : 1); xIdx < 8; xIdx += 2) {
                int piece = cells[rowIdx][xIdx];

                // White men
                if (piece == 1) {
                    board[whiteAsAlly ? 0 : 2][rowIdx][xIdx] = 1;
                }

                // White king
                else if (piece == 2) {
                    board[whiteAsAlly ? 1 : 3][rowIdx][xIdx] = 1;
                }

                // Black men
                else if (piece == -1) {
                    board[whiteAsAlly ? 0 : 2][rowIdx][xIdx] = 1;
                }

                // Black king
                else if (piece == -2) {
                    board[whiteAsAlly ? 3 : 1][rowIdx][xIdx] = 1;
                }
            }
        }
        return board;
    }

    /**
     * Splits the board cells into 4 channels:
     * 0: ALLY men
     * 1: ALLY king
     * 2: OPPONENT men
     * 3: OPPONENT king
     * <p>
     * Ally / Opponent assumes that it is from the player to move's POV
     *
     * @return the board with pieces split into 4 channels [Ally men, Ally king, Enemy men, Enemy king]
     */
    public double[][][] splitBoardChannels() {
        return splitBoardChannels(cells, whiteToMove);
    }



    private static class MoveLog {
        private final List<Move> whiteMoves;
        private final List<Move> blackMoves; //size is either equal to or one less than that of white moves
        private boolean whiteTurn = true;

        public MoveLog() {
            this.whiteMoves = new ArrayList<Move>(List.of(new Move()));
            this.blackMoves = new ArrayList<Move>(List.of(new Move()));
        }

        /**
         * Adds an action to the log
         *
         * @param action   action to add
         * @param turnOver whether the turn is over or not
         */
        public void addActionLog(Action action, boolean turnOver) {
            List<Move> playerMoves = whiteTurn ? whiteMoves : blackMoves;
            playerMoves.getLast().addAction(action);
            if (!turnOver) {
                return;
            }
            playerMoves.addLast(new Move());
            whiteTurn = !whiteTurn;
        }

        @Override
        public String toString() {
            String str = "";
            int blackMoveCount = blackMoves.size();
            for (int i = 0; i < blackMoveCount; i++) {
                str += String.format("%s | %s\n", whiteMoves.get(i).toString(), blackMoves.get(i).toString());
            }
            return str;
        }
    }

    private static class PositionLog {
        private final List<int[][]> recentPositions; //index from most recent to least
        private final int maxPositionLogs;

        public PositionLog(int maxPositionLogs) {
            recentPositions = new LinkedList<>();
            this.maxPositionLogs = maxPositionLogs;
        }

        /**
         * Adds a board position to the log
         *
         * @param board board to save position of (deep copies it)
         */
        public void addPosition(int[][] board) {
            int[][] boardCopy = new int[8][8];
            for (int i = 0; i < 8; i++) {
                boardCopy[i] = board[i].clone();
            }
            recentPositions.addFirst(boardCopy);
            if (recentPositions.size() > maxPositionLogs) {
                recentPositions.removeLast();
            }
        }

        public List<int[][]> getRecentPositions() {
            return recentPositions;
        }

        public int getMaxPositionLogs() {
            return maxPositionLogs;
        }
    }
}
