package game;

import Util.Tuple;
import lombok.Getter;
import model.NeuralNet;

import java.util.*;

public class Board {

    public int[][] cells; //[row index][column index], row are counted from white side to black side

    public static final int MAN_VALUE = 1;
    public static final int KING_VALUE = 2;
    public static final int DRAW_BY_REPETITION_REPEAT_AMOUNT = 3;
    public static final int DRAW_BY_NOTHING_HAPPENING_MOVE_AMOUNT = 50;

    @Getter
    private final MoveLog moveLog;
    private final PositionLog positionLog;
    @Getter
    private GameResult gameResult;
    private Coordinate forcedPieceCaptureCoordinate;

    public Board(int[][] cells, Coordinate forcedPieceCaptureCoordinate, MoveLog moveLog, PositionLog positionLog, GameResult gameResult) {
        this.cells = cells;
        this.moveLog = moveLog;
        this.positionLog = positionLog;
        this.gameResult = gameResult;
        this.forcedPieceCaptureCoordinate = forcedPieceCaptureCoordinate;
    }


    public Board() {
        this(getStartingBoard(), null, new MoveLog(), new PositionLog(Math.max(NeuralNet.CHANNELS / 4 - 4, DRAW_BY_REPETITION_REPEAT_AMOUNT)), GameResult.ONGOING);
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
                //kings in the center top/bottom (testing)
                //if (rowIdx == 0 || rowIdx == 7) {
                //    if (xIdx == 3 || xIdx == 4) {
                //        newCells[rowIdx][xIdx] = rowIdx < 3 ? KING_VALUE : -KING_VALUE;
                //    }
                //}
            }
        }

        return newCells;
    }

    /**
     * Checks for if a player has won, if yes, returns true and sets the game result accordingly
     *
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
     *
     * @return if the game is drawn
     */
    private boolean checkForDraw() {
        boolean draw = positionLog.checkForDrawByRepetition() || moveLog.getNoCaptureMoveStreak() >= DRAW_BY_NOTHING_HAPPENING_MOVE_AMOUNT;
        gameResult = draw ? GameResult.DRAW : gameResult;
        return draw;
    }

    /**
     * Checks if the game is over from the game result
     *
     * @return if the game is over
     */
    public boolean isGameOver() {
        return !gameResult.equals(GameResult.ONGOING);
    }

    /**
     * Checks if an action is valid
     *
     * @param action action to check
     * @return true if the action is valid
     */
    public boolean checkValidAction(Action action) {
        int movingPiece = getPieceAt(action.getStart());
        //If the start of the action is invalid / empty or if the destination is invalid / occupied or if there is a capture destination that is empty
        if (arePiecesNonAllied(movingPiece, isWhiteToMove() ? 1 : -1) || action.getDestination().isInvalid() || getPieceAt(action.getDestination()) != 0 || (action.getCaptureCoordinate() != null && getPieceAt(action.getCaptureCoordinate()) == 0)) {
            return false;
        }
        return (Math.abs(action.getDeltaCoordinate().getY()) == Math.abs(action.getDeltaCoordinate().getX()) && action.getDeltaCoordinate().getX() <= 2) && (Math.abs(movingPiece) == 2 || action.getDeltaCoordinate().getY() > 0 == movingPiece > 0);
    }

    /**
     * Applies a move to the game and checks for any changes in the game result
     *
     * @param move move to apply
     * @return result of move
     */
    public MoveResult applyMove(Move move) {
        if (move == null) {
            return null;
        }
        List<Action> moveActions = move.getActions();
        MoveResult moveResult = new MoveResult();
        for (int i = 0; i < moveActions.size(); i++) {
            Action action = moveActions.get(i);
            if (!checkValidAction(action)) {
                System.out.printf("INVALID ACTION: %s", action);
                return null;
            }
            ActionResult actionResult = applyAction(action, i < (moveActions.size() - 1));
            if (actionResult == null) {
                System.out.printf("COULD NOT APPLY ACTION: %s", action);
                return null;
            }
            moveResult.addAction(actionResult);
        }
        return moveResult;
    }


    /**
     * Applies an action to the game and checks for any changes in the game result
     *
     * @param action action to apply
     * @return the action result or null
     */
    public ActionResult applyAction(Action action, boolean chainIfPossible) {
        if (isGameOver()) {
            return null;
        }
        Coordinate start = action.getStart();

        int piece = getPieceAt(start);
        if (piece == 0 || piece > 0 != isWhiteToMove() || (forcedPieceCaptureCoordinate != null && !start.equals(forcedPieceCaptureCoordinate))) {
            return null;
        }
        positionLog.addPosition(cells);
        Coordinate destination = action.getDestination();
        Coordinate capturedCoordinate = action.getCaptureCoordinate();
        boolean isPromotion = (piece == 1 || piece == -1) && checkPromotion(destination, piece);
        cells[start.getY()][start.getX()] = 0;
        cells[destination.getY()][destination.getX()] = isPromotion ? piece * 2 : piece;
        if (capturedCoordinate == null) {
            forcedPieceCaptureCoordinate = null;
            ActionResult actionResult = new ActionResult(action, 0, isPromotion);
            moveLog.addActionLog(actionResult, true);
            boolean gameOver = checkForDraw();
            if (gameOver) {
                printGameOver();
            }
            return actionResult;
        }
        int capturedPiece = getPieceAt(capturedCoordinate);
        cells[capturedCoordinate.getY()][capturedCoordinate.getX()] = 0;
        boolean gameOver = checkForWinner();
        ActionResult actionResult = new ActionResult(action, capturedPiece, isPromotion);
        if (gameOver) {
            moveLog.addActionLog(actionResult,  true);
            printGameOver();
            return actionResult;
        }
        if (!chainIfPossible) {
            moveLog.addActionLog(actionResult, true);
            forcedPieceCaptureCoordinate = null;
            return actionResult;
        }
        List<Action> chainActionSpace = getPieceActionSpace(destination, piece).e1;
        forcedPieceCaptureCoordinate = chainActionSpace.isEmpty() ? null : action.getDestination();
        moveLog.addActionLog(actionResult, chainActionSpace.isEmpty());
        return actionResult;
    }

    /**
     * Gets the count of each player's pieces (regardless of type)
     *
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
            Coordinate actionDestination = action.getDestination();
            int destinationPiece = getPieceAt(actionDestination);
            if (destinationPiece == 0) {
                if (!captureAvailable) {
                    nonCaptureActions.add(action);
                }
                continue;
            }
            Coordinate captureDestination = actionDestination.addedWith(action.getDeltaCoordinate());
            if (captureDestination.isInvalid() || !arePiecesNonAllied(destinationPiece, piece) || getPieceAt(captureDestination) != 0) {
                continue;
            }
            captureAvailable = true;
            captureActions.add(new Action(action.getStart(), captureDestination));
        }
        return new Tuple<List<Action>, List<Action>>(captureActions, nonCaptureActions);
    }

    /**
     * Gets the action space for the current player at the board
     *
     * @return if it is a forced capture and map of possible actions for the current player with piece coordinates as key | null if there is no action possible (end of multi capture)
     */
    public Tuple<Boolean, Map<Coordinate, List<Action>>> getBoardActionSpace() {
        if (isGameOver()) {
            return null;
        }
        if (forcedPieceCaptureCoordinate != null) {
            List<Action> actionSpace = getPieceActionSpace(forcedPieceCaptureCoordinate, getPieceAt(forcedPieceCaptureCoordinate)).e1;
            return new Tuple<>(true, actionSpace.isEmpty() ? new HashMap<>() : new HashMap<>(Map.of(forcedPieceCaptureCoordinate, actionSpace) )) ;
        }
        Map<Coordinate, List<Action>> captureActions = new HashMap<>();
        Map<Coordinate, List<Action>> nonCaptureActions = new HashMap<>();
        boolean captureAvailable = false;
        for (int rowIdx = 0; rowIdx < 8; rowIdx++) {
            for (int xIdx = (rowIdx % 2 == 0 ? 0 : 1); xIdx < 8; xIdx += 2) {
                int piece = cells[rowIdx][xIdx];
                //if no piece or piece color is not the color of the player who moves, skip
                if (piece == 0 || (piece > 0 != isWhiteToMove())) {
                    continue;
                }
                Coordinate coordinate = new Coordinate(xIdx, rowIdx);
                Tuple<List<Action>, List<Action>> pieceActionSpace = getPieceActionSpace(coordinate, piece);
                if (!pieceActionSpace.e1.isEmpty()) {
                    captureAvailable = true;
                }
                if (captureAvailable) {
                    if (!pieceActionSpace.e1.isEmpty()) {
                        captureActions.put(coordinate , pieceActionSpace.e1);
                    }
                } else {
                    if (!pieceActionSpace.e2.isEmpty()) {
                        nonCaptureActions.put(coordinate, pieceActionSpace.e2);
                    }
                }
            }
        }
        return new Tuple<>(captureAvailable, captureAvailable ? captureActions : nonCaptureActions) ;
    }

    /**
     * Gets the move space of the piece
     *
     * @param coordinate coordinate of the piece
     * @param piece      the value of the piece (1 for man, 2 for king, positive for white, negative for black)
     * @return two lists of all possible moves for the piece (captures, non captures) | null if game is over or invalid coordinate / piece
     */
    public Tuple<List<Move>, List<Move>> getPieceMoveSpace(Coordinate coordinate, int piece) {
        return getPieceMoveSpace(coordinate, piece, new ArrayList<>());
    }

    /**
     * Gets the move space of the piece
     *
     * @param coordinate          coordinate of the piece
     * @param piece               the value of the piece (1 for man, 2 for king, positive for white, negative for black)
     * @param filteredCoordinates [used for recursion], stores the coordinates of previous captures to not backtrack | NOT NULL
     * @return two lists of all possible moves for the piece (captures, non captures) | null if game is over or invalid coordinate / piece
     */
    public Tuple<List<Move>, List<Move>> getPieceMoveSpace(Coordinate coordinate, int piece, List<Coordinate> filteredCoordinates) {
        if (isGameOver() || coordinate.isInvalid() || isPieceInvalid(piece)) {
            return null;
        }
        List<Move> captureMoveResults = new ArrayList<>();
        List<Move> nonCaptureMoveResults = new ArrayList<>();
        List<Action> pieceActions = (Math.abs(piece) == 2 ? coordinate.getPossibleKingActions() : coordinate.getPossibleManActions(piece > 0));
        for (Action action : pieceActions) {
            Coordinate moveDestination = action.getDestination();
            if (filteredCoordinates.contains(moveDestination)) {
                continue;
            }
            int destinationPiece = getPieceAt(moveDestination);
            //if there is no piece on the square, the move is valid
            if (destinationPiece == 0) {
                nonCaptureMoveResults.add(new Move(action));
                continue;
            }
            Coordinate captureDestination = action.getDestination().addedWith(action.getDeltaCoordinate());
            //if there is a piece on the square, check if it can be captured
            if (captureDestination.isInvalid() || getPieceAt(captureDestination) != 0 || !arePiecesNonAllied(destinationPiece, piece)) {
                continue;
            }

            filteredCoordinates.add(moveDestination);
            //after captures, must move again if is another capture
            Tuple<List<Move>, List<Move>> nextActionSpaces = getPieceMoveSpace(captureDestination, piece, filteredCoordinates);
            //if there are no more capture moves, add this move to the capture move results
            if (nextActionSpaces.e1.isEmpty()) {
                captureMoveResults.add(new Move(new Action(action.getStart(), captureDestination)));
            }
            for (Move chainMoves : nextActionSpaces.e1) {
                List<Action> chainActions = new LinkedList<>(chainMoves.getActions());
                if (chainActions.isEmpty()) {
                    continue;
                }
                chainActions.addFirst(new Action(action.getStart(), captureDestination));
                captureMoveResults.add(new Move(chainActions));
            }
        }
        return new Tuple<List<Move>, List<Move>>(captureMoveResults, nonCaptureMoveResults);
    }

    /**
     * Gets the move space for every peace for a player
     *
     * @return a list of every possible move for the player  | null if game is over
     */
    public List<Move> getBoardMoveSpace() {
        if (isGameOver()) {
            return new ArrayList<Move>();
        }
        List<Move> nonCaptureMoveResults = new ArrayList<>();
        List<Move> captureMoveResults = new ArrayList<>();
        boolean captureAvailable = false;
        //Assuming piece values: Man = 1, King = 2 (+ for white, - for black)
        for (int rowIdx = 0; rowIdx < 8; rowIdx++) {
            for (int xIdx = (rowIdx % 2 == 0 ? 0 : 1); xIdx < 8; xIdx += 2) {
                int piece = cells[rowIdx][xIdx];
                //if no piece or piece color is not the color of the player who moves, skip
                if (piece == 0 || (piece < 0 == isWhiteToMove())) {
                    continue;
                }
                Coordinate coordinate = new Coordinate(xIdx, rowIdx);
                Tuple<List<Move>, List<Move>> pieceMoveSpaces = getPieceMoveSpace(coordinate, piece);
                if (!pieceMoveSpaces.e1.isEmpty()) {
                    captureAvailable = true;
                }
                if (captureAvailable) {
                    captureMoveResults.addAll(pieceMoveSpaces.e1);
                } else {
                    nonCaptureMoveResults.addAll(pieceMoveSpaces.e2);
                }
            }
        }
        return captureAvailable ? captureMoveResults : nonCaptureMoveResults;
    }

    public boolean undoLastMove() {
        MoveResult undoingMoveResult = moveLog.popLastMove();
        if (undoingMoveResult == null) {
            return false;
        }

        boolean result = undoMove(undoingMoveResult);
        gameResult = GameResult.ONGOING;
        return result;
    }

    /**
     * Undoes the last move and gives the turn back to the other player
     *
     * @return if it was undone successfully
     */
    public boolean undoMove(MoveResult moveResult) {
        for (int i = moveResult.getActionResults().size() - 1; i >= 0; i--) {
            if (!undoActionResult(moveResult.getActionResults().get(i))) {
                System.out.printf("Could not undo move: %s", moveResult.getActionResults().get(i));
                return false;
            }
        }
        return true;
    }

    /**
     * Undoes the action if possible
     *
     * @param action action result to undo
     * @return if action was undone successfully
     */
    public boolean undoActionResult(ActionResult action) {
        int piece = getPieceAt(action.getDestination());
        if (piece == 0) {
            System.out.printf("NO PIECE AT ACTION DESTINATION: %s", action);
            return false;
        }
        cells[action.getDestination().getY()][action.getDestination().getX()] = 0;
        cells[action.getStart().getY()][action.getStart().getX()] = action.isPromotion() ? piece / 2 : piece;
        Coordinate captureCoordinate = action.getCaptureCoordinate();
        if (captureCoordinate != null) {
            cells[captureCoordinate.getY()][captureCoordinate.getX()] = action.getCapturedPiece();
        }
        forcedPieceCaptureCoordinate = captureCoordinate == null ? null : action.getStart();
        return true;
    }

    public int getPieceAt(Coordinate coordinate) {
        if (coordinate.isInvalid()) {
            return 0;
        }
        return cells[coordinate.getY()][coordinate.getX()];
    }

    /**
     * Checks if pieces are not allies
     *
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
                    board[whiteAsAlly ? 2 : 0][rowIdx][xIdx] = 1;
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
            double[][][] positionChannels = splitPositionChannels(position, isWhiteToMove());
            for (int j = 0; j < 4; j++) {
                boardChannels[i * 4 + j] = positionChannels[j];
            }
        }
        return boardChannels;
    }


    @Override
    //The array string is flipped so the board can be perceived through the white player's point of view
    public String toString() {
        String str = gameResult == GameResult.ONGOING ? String.format("%s to move \n", isWhiteToMove() ? "White" : "Black") : String.format("Result: %s [Would be %s to move]\n", gameResult.name(), isWhiteToMove() ? "White" : "Black");
        for (int rowIdx = 7; rowIdx >= 0; rowIdx--) {
            for (int xIdx = 0; xIdx < 8; xIdx++) {
                str += String.format("[%4s]", cells[rowIdx][xIdx]);
            }
            str += rowIdx == 0 ? (forcedPieceCaptureCoordinate == null ? "" : String.format("Forced to capture with piece at: %s", forcedPieceCaptureCoordinate)) : "\n";
        }
        str += moveLog == null ? "\n[no move logs]" : String.format("\nLOGS\n%s", moveLog.toString());
        return str;
    }

    /**
     * Check if a piece should be promoted
     *
     * @param destination where the piece arrived
     * @param piece       the piece
     * @return if the piece should be promoted
     */
    private boolean checkPromotion(Coordinate destination, int piece) {
        return (destination.getY() == 7 && piece == 1) || (destination.getY() == 0 && piece == -1);
    }

    public Boolean isWhiteToMove() {
        return moveLog.isWhiteTurn();
    }

    private void printGameOver() {
        //System.out.println("GAME OVER");
    }

    /**
     * Gets the duplicate of the board position
     *
     * @param invertColors to invert white and black's values or not
     * @return the duplicate (inverted?) board
     */
    public int[][] getCellsDuplicate(boolean invertColors) {
        int[][] duplicate = new int[8][8];
        if (!invertColors) {
            for (int i = 0; i < 8; i++) {
                duplicate[i] = cells[i].clone();
            }
            return duplicate;
        }
        for (int rowIdx = 0; rowIdx < 8; rowIdx++) {
            for (int xIdx = (rowIdx % 2 == 0 ? 0 : 1); xIdx < 8; xIdx += 2) {
                duplicate[rowIdx][xIdx] = -cells[rowIdx][xIdx];
            }
        }
        return duplicate;
    }

    /**
     * Gets a copy of this board object | no move logs
     *
     * @return copy of board object
     */
    public Board getBoardDuplicate() {
        return new Board(getCellsDuplicate(false), forcedPieceCaptureCoordinate, moveLog.getDuplicate(false), positionLog.getDuplicate(false), gameResult);
    }

    /**
     * Gets a copy of this board object where the player to move is of the color of the argument
     *
     * @param asWhiteToMove color of the player that has the move
     * @return copy of board object with (inverted?) colors
     */
    public Board getBoardWithColorOverride(boolean asWhiteToMove) {
        boolean invertColors = asWhiteToMove != isWhiteToMove();
        return new Board(getCellsDuplicate(invertColors), forcedPieceCaptureCoordinate, moveLog.getDuplicate(invertColors), positionLog.getDuplicate(invertColors), invertColors ? gameResult.colorInverted() : gameResult);
    }

    public GameResult getGameResult() {
        return gameResult;
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
