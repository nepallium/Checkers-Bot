package game;

import Util.Tuple;
import model.NeuralNet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Board {

    public int[][] cells; //[row index][column index], row are counted from white side to black side

    public static final int MAN_VALUE = 1;
    public static final int KING_VALUE = 2;
    public static final int DRAW_BY_REPETITION_REPEAT_AMOUNT = 3;

    private MoveLog moveLog;
    private PositionLog positionLog;
    private GameResult gameResult;
    private Coordinate forcedPieceCaptureCoordinate = null;
    private boolean whiteToMove;

    public Board(int[][] cells, boolean whiteToMove) {
        this.cells = cells;
        this.whiteToMove = whiteToMove;
        this.gameResult = GameResult.ONGOING;
    }


    public Board() {
        this.cells = getStartingBoard();
        this.whiteToMove = true;
        this.moveLog = new MoveLog();
        this.positionLog = new PositionLog(Math.max(NeuralNet.CHANNELS / 4 - 4, DRAW_BY_REPETITION_REPEAT_AMOUNT)); //save up to 3 positions for the CNN
        this.gameResult = GameResult.ONGOING;
    }

    /**
     * Generates an array for the starting board
     *
     * @return 8x8 array of the starting board
     */
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

    /**
     * Checks for if a player has won, if yes, returns true and sets the game result accordingly
     * @return if a player has won or not
     */
    private boolean checkForWinner() {
        Tuple<Integer, Integer> playerPiecesCount = getPlayerPiecesCount();
        if (playerPiecesCount.e1 == 0) {
            gameResult = GameResult.BLACK_WIN;
            return true;
        }
        if (playerPiecesCount.e2 == 0) {
            gameResult = GameResult.WHITE_WIN;
            return true;
        }
        return false;
    }

    /**
     * Checks for a draw by repetition and changes game state accordingly
     * @return if the game is drawn
     */
    private boolean checkForDraw() {
        boolean draw = positionLog.checkForDrawByRepetition();
        gameResult = draw ? GameResult.DRAW : gameResult;
        return draw;
    }

    /**
     * Checks if the game is over from the game result
     * @return if the game is over
     */
    public boolean isGameOver() {
        return !gameResult.equals(GameResult.ONGOING);
    }

    /**
     * Checks if an action is valid
     * @param action action to check
     * @return true if the action is valid
     */
    public boolean checkValidAction(Action action) {
        int movingPiece = getPieceAt(action.getStart());
        //If the start of the action is invalid / empty or if the destination is invalid / occupied or if there is a capture destination that is empty
        if (arePiecesNonAllied(movingPiece, whiteToMove ? 1 : -1)|| action.getDestination().isInvalid() || getPieceAt(action.getDestination()) != 0 || (action.getCaptureCoordinate() != null && getPieceAt(action.getCaptureCoordinate()) == 0)) {
            return false;
        }
        return (Math.abs(action.getDeltaCoordinate().getY()) == Math.abs(action.getDeltaCoordinate().getX()) && action.getDeltaCoordinate().getX() <= 2) && (Math.abs(movingPiece) == 2 || action.getDeltaCoordinate().getY() > 0 == movingPiece > 0);
    }

    /**
     * Applies a move to the game and checks for any changes in the game result
     * @param move move to apply
     * @return if applied successfully
     */
    public boolean applyMove(Move move) {
        for (Action action : move.getActions()) {
            if (!checkValidAction(action)) {
                System.out.printf("INVALID ACTION: %s", action);
                return false;
            }
            applyAction(action);
        }
        return true;
    }


    /**
     * Applies an action to the game and checks for any changes in the game result
     * @param action action to apply
     */
    public void applyAction(Action action) {
        if (isGameOver()) {
            return;
        }
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
            boolean gameOver = checkForDraw();
            if (gameOver) {
                printGameOver();
            }
            return;
        }
        cells[capturedCoordinate.getY()][capturedCoordinate.getX()] = 0;
        boolean gameOver = checkForWinner();
        if (gameOver) {
            printGameOver();
            whiteToMove = !whiteToMove;
            return;
        }
        List<Action> chainActionSpace = getPieceActionSpace(destination, piece).e1;

        forcedPieceCaptureCoordinate = chainActionSpace.isEmpty() ? null : action.getDestination();
        moveLog.addActionLog(action, chainActionSpace.isEmpty());
        whiteToMove = chainActionSpace.isEmpty() != whiteToMove;
    }

    /**
     * Gets the count of each player's pieces (regardless of type)
     * @return (White pieces count, Black pieces count)
     */
    public Tuple<Integer, Integer> getPlayerPiecesCount() {
        Tuple<Integer, Integer> playerPiecesCount = new Tuple<>(0, 0);
        for (int rowIdx = 0; rowIdx < 8; rowIdx++) {
            for (int xIdx = (rowIdx % 2 == 0 ? 0 : 1); xIdx < 8; xIdx += 2) {
                int piece = cells[rowIdx][xIdx];
                playerPiecesCount.e1 += piece > 0 ? 1 : 0;
                playerPiecesCount.e2 += piece < 0 ? 1 : 0;
            }
        }
        return playerPiecesCount;
    }

    /**
     * Gets the action space of the piece
     *
     * @param coordinate coordinate of the piece
     * @param piece      the value of the piece (1 for man, 2 for king, positive for white, negative for black)
     * @return two lists of all possible actions for the piece with no captures and captures (captures, non captures) | null if game is over or if coordinate / piece is invalid
     */
    public Tuple<List<Action>, List<Action>> getPieceActionSpace(Coordinate coordinate, int piece) {
        if (isGameOver() || coordinate.isInvalid() || isPieceInvalid(piece)) {
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
            if (arePiecesNonAllied(destinationPiece, piece) && getPieceAt(captureDestination) == 0) {
                captureAvailable = true;
                captureActions.add(new Action(action.getStart(), captureDestination));
            }
        }
        return new Tuple<List<Action>, List<Action>>(captureActions, nonCaptureActions);
    }

    /**
     * Gets the action space for the current player at the board
     * @return list of possible actions for the current player | null if game is over
     */
    public List<Action> getBoardActionSpace() {
        if (isGameOver()) {
            return null;
        }
        if (forcedPieceCaptureCoordinate != null) {
            List<Action> actionSpace = getPieceActionSpace(forcedPieceCaptureCoordinate, getPieceAt(forcedPieceCaptureCoordinate)).e1;
            return actionSpace.isEmpty() ? new ArrayList<>(List.of(new Action(forcedPieceCaptureCoordinate, forcedPieceCaptureCoordinate))) : actionSpace;
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
        return captureAvailable ? captureActions : nonCaptureActions;
    }

    /**
     * Gets the move space of the piece
     *
     * @param coordinate coordinate of the piece
     * @param piece      the value of the piece (1 for man, 2 for king, positive for white, negative for black)
     *                   //* @param previousCaptures [used for recursion], stores the previous captures made by the piece (if the move follows a capture)
     * @return two lists of all possible moves for the piece (captures, non captures) | null if game is over or invalid coordinate / piece
     */
    public Tuple<List<Move>, List<Move>> getPieceMoveSpace(Coordinate coordinate, int piece) {
        if (isGameOver() || coordinate.isInvalid() || isPieceInvalid(piece)) {
            return null;
        }
        List<Move> captureMoves = new ArrayList<>();
        List<Move> nonCaptureMoves = new ArrayList<>();
        boolean captureAvailable = false;
        List<Action> pieceActions = (Math.abs(piece) == 2 ? coordinate.getPossibleKingActions() : coordinate.getPossibleManActions(piece > 0));
        for (Action action : pieceActions) {
            Coordinate moveDestination = action.getDestination();
            int destinationPiece = getPieceAt(moveDestination);
            //if there is no piece on the square, the move is valid
            if (destinationPiece == 0) {
                nonCaptureMoves.add(new Move(action));
                continue;
            }
            Coordinate captureDestination = action.getDestination().addedWith(action.getDeltaCoordinate());
            //if there is a piece on the square, check if it can be captured
            if (!captureDestination.isInvalid() && getPieceAt(captureDestination) == 0 && arePiecesNonAllied(destinationPiece, piece)) {
                captureAvailable = true;
                captureMoves.add(new Move(action.getStart(), captureDestination));
                //after captures, can move again if is another capture
                Tuple<List<Move>, List<Move>> nextActionSpaces = getPieceMoveSpace(captureDestination, piece);
                for (Move captureMove : nextActionSpaces.e1) {
                    List<Action> moveActions = new ArrayList<>(captureMove.getActions());
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
     * @return a list of every possible move for the player | null if game is over
     */
    public List<Move> getBoardMoveSpace() {
        if (isGameOver()) {
            return null;
        }
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

    /**
     * Checks if pieces are not allies
     * @param piece1 piece 1
     * @param piece2 piece 2
     * @return false if piece 1 and piece 2 are in the same team, true if not or if one (or both) are not pieces
     */
    public static boolean arePiecesNonAllied(int piece1, int piece2) {
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
    public static double[][][] splitPositionChannels(int[][] cells, boolean whiteAsAlly) {
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
     * Splits the board cells into X * 4 channels [X is the amount of positions (current + Y most recent positions)]:
     * 0: ALLY men
     * 1: ALLY king
     * 2: OPPONENT men
     * 3: OPPONENT king
     * <p>
     * Ally / Opponent assumes that it is from the player to move's POV
     *
     * @return the board with pieces split into channels [Ally men, Ally king, Enemy men, Enemy king] for X positions
     */
    public double[][][] splitBoardChannels() {
        List<int[][]> positionLogs = new LinkedList<>(positionLog.getRecentPositions());
        positionLogs.addFirst(cells);
        double[][][] boardChannels = new double[NeuralNet.CHANNELS][8][8];

        for (int i = 0; i < NeuralNet.CHANNELS / 4; i++) {
            int[][] position = positionLogs.size() > i ? positionLogs.get(i) : STARTING_BOARD;
            double[][][] positionChannels = splitPositionChannels(position, whiteToMove);
            for (int j = 0; j < 4; j++) {
                boardChannels[i * 4 + j] = positionChannels[j];
            }
        }
        return boardChannels;
    }


    @Override
    //The array string is flipped so the board can be perceived through the white player's point of view
    public String toString() {
        String str = gameResult == GameResult.ONGOING ? String.format("%s to move \n", whiteToMove ? "White" : "Black") : String.format("Result: %s\n, Sorry %s, no turn for you.", gameResult.name(), whiteToMove ? "White" : "Black");
        for (int rowIdx = 7; rowIdx >= 0; rowIdx--) {
            for (int xIdx = 0; xIdx < 8; xIdx++) {
                str += String.format("[%4s]", cells[rowIdx][xIdx]);
            }
            str += "\n";
        }
        str += String.format("\nLOGS\n%s", moveLog.toString());
        return str;
    }


    public GameResult getGameResult() {
        return gameResult;
    }

    private void printGameOver() {
        System.out.printf("GAME OVER:\n%s", this);
    }

    /**
     * Array for the starting board.
     * NOT IMMUTABLE. HANDLE WITH CARE
     */
    public static final int[][] STARTING_BOARD = new int[][]{
            new int[]{1, 0, 1, 0, 1, 0, 1, 0,},
            new int[]{0, 1, 0, 1, 0, 1, 0, 1,},
            new int[]{1, 0, 1, 0, 1, 0, 1, 0,},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0,},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0,},
            new int[]{0, -1, 0, -1, 0, -1, 0, -1,},
            new int[]{-1, 0, -1, 0, -1, 0, -1, 0,},
            new int[]{0, -1, 0, -1, 0, -1, 0, -1,},
    };
}
