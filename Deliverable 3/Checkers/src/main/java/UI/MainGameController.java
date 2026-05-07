package UI;

import Util.Tuple;
import game.*;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import main.App;
import mcts.MCTS;
import model.NeuralNet;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MainGameController {
    private static final int GRID_ENTRY_SIDE_LENGTH = 100;
    public Board board = new Board();

    private final boolean playerPlaysAsWhite = true;
    private boolean inputsDisabled = false;
    private Coordinate selectedPieceCoordinate = null;
    Tuple<Boolean, Map<Coordinate, List<Action>>> boardActionSpaceState = board.getBoardActionSpace();
    HashMap<Coordinate, ImageView> pieceUIMap = new HashMap<>();

    private static final ColorAdjust PIECE_FORCED_ACTION_EFFECT = new ColorAdjust(0, 0, .15, 0.85);
    private static final ColorAdjust PIECE_SELECT_EFFECT = new ColorAdjust(0, 0, -0.15, 0.5);
    private static final ColorAdjust PIECE_DEFAULT_EFFECT = new ColorAdjust();

    private final ImageView[] targetIcons = new ImageView[4];
    public Pane boardPane;
    public Pane boardParentPane;
    private Stack<ImageView> capturedPieceUIs = new Stack<>();


    public ImageView homeImg;
    public ImageView undoImg;

    private NeuralNet neuralNet;
    private MCTS mcts;

    private void onUndoButtonPressed() throws InterruptedException {
        if (inputsDisabled) {
            return;
        }
        inputsDisabled = true;
        Runnable undoSecondMove = () -> {
            try {
                boolean success = undoLastMove(() -> {
                    inputsDisabled = false;
                    boardActionSpaceState = board.getBoardActionSpace();
                    showPieceActions();
                    System.out.println(board);
                });
                if (!success) {
                    inputsDisabled = false;
                }
            } catch (InterruptedException e) {
                inputsDisabled = false;
                throw new RuntimeException(e);
            }
        };
        if (!undoLastMove(undoSecondMove)) {
            inputsDisabled = false;
        }
    }
    private void undoActionResult(ActionResult actionResult, Runnable onActionUndone) {
        Coordinate start = actionResult.getStart();
        Coordinate destination = actionResult.getDestination();

        ImageView pieceUI = pieceUIMap.get(destination);

        pieceUIMap.remove(destination);
        pieceUIMap.put(start, pieceUI);
        showPieceMoving(destination, start, pieceUI, actionResult.isPromotion(), onActionUndone, App.PIECE_TAKEBACK_DURATION);

        Coordinate capturedCoordinate = actionResult.getCaptureCoordinate();
        if (capturedCoordinate != null) {
            ImageView capturedPieceImg = capturedPieceUIs.pop();
            pieceUIMap.put(capturedCoordinate, capturedPieceImg);
            // restore visibility
            capturedPieceImg.setVisible(true);

            ScaleTransition scaleTransition = new ScaleTransition(
                    Duration.seconds(App.PIECE_TAKEBACK_DURATION * 0.5),
                    capturedPieceImg
            );
            scaleTransition.setToX(1);
            scaleTransition.setToY(1);
            scaleTransition.play();
        }
    }


    @FXML
    private void initialize() {
        initializeSideBar();
        initializeBoard();

        neuralNet = new NeuralNet(12);
        try {
            neuralNet.load("checkersModel.bin");
            System.out.println("Model loaded!!");
        } catch (IOException e) {
            System.out.println("No saved model found, using random weights");
        }
        mcts = new MCTS(neuralNet);
        mcts.setSIMULATIONS(App.getMctsSimulations());

        System.out.println("Simulations amount set to " + App.getMctsSimulations());
        undoImg.setOnMouseClicked(e -> {
            try {
                onUndoButtonPressed();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    private void initializeSideBar() {
//        Image undo = new Image(getClass().getResource(App.IMAGES_FILE_PATH_PREFIX + App.UNDO_IMAGE_PATH).toExternalForm());
//        undoImg.setImage(undo);
//
//        Image home = new Image(getClass().getResource(App.IMAGES_FILE_PATH_PREFIX + App.HOME_IMAGE_PATH).toExternalForm());
//        homeImg.setImage(home);
    }

    private void initializeBoard() {
        boardPane.setPrefSize(8 * GRID_ENTRY_SIDE_LENGTH, 8 * GRID_ENTRY_SIDE_LENGTH);
        for (int x = 0; x < 8; x++) {
            for (int y = x % 2 == 0 ? 1 : 0; y < 8; y += 2) { //for invalid coordinates, just put colored squares
                Button gridBtn = createGridButton(false);
                gridBtn.setOnAction((ActionEvent e) -> {
                    selectCoordinate(null);
                });
                boardPane.getChildren().add(gridBtn);
                positionElementAtBoardCoordinate(gridBtn, new Coordinate(x, y));
            }


            for (int y = x % 2 == 0 ? 0 : 1; y < 8; y += 2) { //for valid coordinates, put squares, handle actions, put pieces (if there are)
                Button gridBtn = createGridButton(true);
                Coordinate coordinate = new Coordinate(x, y);
                gridBtn.setOnAction((ActionEvent e) -> {
                    selectCoordinate(coordinate);
                });
                boardPane.getChildren().add(gridBtn);
                positionElementAtBoardCoordinate(gridBtn, coordinate);
                int piece = board.cells[y][x];
                if (piece != 0) {
                    ImageView imgView = createPieceImgView(piece);
                    positionElementAtBoardCoordinate(imgView, coordinate);
                    pieceUIMap.put(coordinate, imgView);
                    boardParentPane.getChildren().add(imgView);
                }
            }
        }

        Image targetImage = new Image(App.class.getResource(App.IMAGES_FILE_PATH_PREFIX + App.TARGET_ICON_IMAGE_PATH).toExternalForm());
        for (int i = 0; i < targetIcons.length; i++) {
            targetIcons[i] = new ImageView(targetImage);
            targetIcons[i].setFitHeight(GRID_ENTRY_SIDE_LENGTH);
            targetIcons[i].setFitWidth(GRID_ENTRY_SIDE_LENGTH);
            targetIcons[i].setVisible(false);
            targetIcons[i].setMouseTransparent(true);
        }
        boardParentPane.getChildren().addAll(targetIcons);

    }


    private void positionElementAtBoardCoordinate(Node node, Coordinate coordinate) {
        Tuple<Double, Double> layoutXY = getLayoutAtBoardCoordinate(coordinate);
        node.setLayoutX(layoutXY.e1);
        node.setLayoutY(layoutXY.e2);
    }

    private Tuple<Double, Double> getLayoutAtBoardCoordinate(Coordinate coordinate) {
        double boardPaneLayoutX = boardPane.getLayoutX(), boardPaneLayoutY = boardPane.getLayoutY();
        //Flip the board vertically so white is shown at the bottom
        return new Tuple<>(boardPaneLayoutX + coordinate.getX() * GRID_ENTRY_SIDE_LENGTH, boardPaneLayoutY + (7 - coordinate.getY()) * GRID_ENTRY_SIDE_LENGTH);
    }

    private void selectCoordinate(Coordinate coordinate) {
        System.out.println("Inputs disabled :" + inputsDisabled);
        if (inputsDisabled) {
            return;
        }
        if (board.isGameOver()) {
            System.out.println("Game over, no more moves can be made");
            return;
        }

        if (selectedPieceCoordinate != null) {
            //Check if can make the action
            Action requestedAction = null;
            List<Action> actions = boardActionSpaceState.e2.get(selectedPieceCoordinate);
            if (actions != null && !actions.isEmpty()) {
                for (Action action : actions) {
                    if (action.getDestination().equals(coordinate)) {
                        requestedAction = action;
                        break;
                    }
                }
            }
            //Unselect piece
            ImageView prevSelectedPieceUI = pieceUIMap.get(selectedPieceCoordinate);
            if (prevSelectedPieceUI != null) {
                prevSelectedPieceUI.setEffect(PIECE_DEFAULT_EFFECT);
            }
            //Apply action if possible
            if (requestedAction != null) {
                applyActionToBoard(requestedAction);
            }
            setSelectedPieceCoordinate(null);
            return;
        }
        setSelectedPieceCoordinate(coordinate);
    }


    private void applyActionToBoard(Action action) {
        ActionResult actionResult = board.applyAction(action, true);
        if (actionResult == null) {
            System.out.println("INVALID ACTION TO APPLY TO BOARD: " + action);
            return;
        }
        boolean canStillPlay = board.hasForcedPieceCaptureCoordinate();
        //Update values relative to board
        boardActionSpaceState = board.getBoardActionSpace();

        ImageView pieceUI = pieceUIMap.remove(action.getStart());
        pieceUIMap.put(action.getDestination(), pieceUI);
        inputsDisabled = true;
        showAction(actionResult, pieceUI, () -> {inputsDisabled = !canStillPlay;});
        showPieceActions();
        //Check if is AI turn
        if (board.isGameOver() || (board.isWhiteToMove() == playerPlaysAsWhite)) {
            return;
        }
        inputsDisabled = true;
        //Delay thread (halting thread cancels transitions)
        ScheduledExecutorService AIScheduler = Executors.newScheduledThreadPool(1);
        AIScheduler.schedule((() -> {
            javafx.application.Platform.runLater(this::doAIMove);
            AIScheduler.close();
        }), 500, TimeUnit.MILLISECONDS);
    }

    private void applyMoveToBoard(Move move) throws InterruptedException {
        MoveResult moveResult = board.applyMove(move);
        if (moveResult == null) {
            System.out.println("INVALID MOVE TO APPLY TO BOARD " + move);
            return;
        }
        boardActionSpaceState = board.getBoardActionSpace();
        inputsDisabled = true;
        ImageView pieceUI = pieceUIMap.remove(moveResult.getStart());
        pieceUIMap.put(move.getDestination(), pieceUI);

        ScheduledExecutorService actionScheduler = Executors.newScheduledThreadPool(1);
        Consumer<Integer> actionApplication = new Consumer<Integer>() {
            @Override
            public void accept(Integer actionIdx) {
                ActionResult actionResult = actionIdx < moveResult.getActionResults().size() ? moveResult.getActionResults().get(actionIdx) : null;

                Runnable application = actionResult != null ? ()  -> {
                    Coordinate captureCoordinate = actionResult.getCaptureCoordinate();
                    showAction(actionResult, pieceUI, () -> {
                        if (actionIdx < moveResult.getActionResults().size() - 1) {
                            return;
                        }
                        inputsDisabled = false;
                    });
                    if (actionIdx == moveResult.getActionResults().size() - 1) {
                        showPieceActions();
                    }
                    this.accept(actionIdx + 1);

                } : actionScheduler::close;

                actionScheduler.schedule(application, 500, TimeUnit.MILLISECONDS);
            }
        };
        actionApplication.accept(0);
    }
    private void showPieceMoving(
            Coordinate start,
            Coordinate destination,
            ImageView movingPieceImg,
            boolean isPromotion,
            Runnable onFinished,
            double moveDuration
    ) {
        Tuple<Double, Double> layoutXY1 = getLayoutAtBoardCoordinate(start);
        Tuple<Double, Double> layoutXY2 = getLayoutAtBoardCoordinate(destination);

        double dx = layoutXY2.e1 - layoutXY1.e1;
        double dy = layoutXY2.e2 - layoutXY1.e2;

        TranslateTransition moveTrans = new TranslateTransition(
                Duration.seconds(moveDuration),
                movingPieceImg
        );

        moveTrans.setByX(layoutXY2.e1 - layoutXY1.e1);
        moveTrans.setByY(layoutXY2.e2 - layoutXY1.e2);

        moveTrans.setOnFinished(e -> {
            if (isPromotion) {
                movingPieceImg.setImage(
                        new Image(App.getPieceImagePath(board.getPieceAt(destination)))
                );
            }

            if (onFinished != null) {
                onFinished.run();
            }
        });

        moveTrans.play();
    }

    private void showAction(ActionResult actionResult, ImageView pieceUI, Runnable onActionDone) {
        Coordinate start = actionResult.getStart();
        Coordinate destination = actionResult.getDestination();

        ImageView capturedPieceImg = actionResult.getCaptureCoordinate() == null
                ? null
                : pieceUIMap.remove(actionResult.getCaptureCoordinate());
        if (capturedPieceImg != null) {
            capturedPieceUIs.add(capturedPieceImg);
        }

        pieceUIMap.remove(start);
        pieceUIMap.put(destination, pieceUI);

        showPieceMoving(start, destination, pieceUI, actionResult.isPromotion(), onActionDone, App.PIECE_MOVE_DURATION);

        // Handle capture animation
        if (capturedPieceImg == null) {
            return;
        }

        pieceUIMap.put(actionResult.getCaptureCoordinate(), capturedPieceImg);

        ScaleTransition shrinkTrans = new ScaleTransition(Duration.seconds(App.PIECE_MOVE_DURATION * 0.5), capturedPieceImg);

        shrinkTrans.setToX(0);
        shrinkTrans.setToY(0);

        shrinkTrans.setOnFinished(e -> {
            capturedPieceImg.setVisible(false);
        });

        shrinkTrans.play();
    }
    private void doAIMove() {
        Consumer<Move> playMove = (Move move ) -> {
            javafx.application.Platform.runLater(() -> {
                try {
                    applyMoveToBoard(move);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        };
        // run MCTS on a background thread so UI doesn't freeze
        Thread aiThread = new Thread(() -> {
            inputsDisabled = true;
            double[][][] boardState = board.splitBoardChannels();
            Tuple<double[], List<Move>> output = mcts.run(board);

            int best = 0;

            for (int i = 0; i < output.e1.length; i++) {
                if (output.e1[i] > output.e1[best]) {
                    best = i;
                }
            }

            Move bestMove = output.e2.get(best);
            playMove.accept(bestMove);

        });
        aiThread.setDaemon(true);
        aiThread.start();
    }

    private void showPieceActions() {
        for (Coordinate coordinate : pieceUIMap.keySet()) {
            ImageView pieceImg = pieceUIMap.get(coordinate);
            if (pieceImg == null) {
                continue;
            }
            pieceImg.setEffect(boardActionSpaceState.e2.get(coordinate) == null || !boardActionSpaceState.e1 ? PIECE_DEFAULT_EFFECT : PIECE_FORCED_ACTION_EFFECT);
        }
    }

    private void setSelectedPieceCoordinate(Coordinate coordinate) {
        if (coordinate != null && (boardActionSpaceState.e2.get(coordinate) == null || boardActionSpaceState.e2.get(coordinate) == null)) {
            coordinate = null;
        }

        selectedPieceCoordinate = coordinate;
        showPossibleActions(coordinate);
        if (coordinate != null) {
            ImageView selectedPieceUI = pieceUIMap.get(coordinate);
            if (selectedPieceUI != null) {
                selectedPieceUI.setEffect(PIECE_SELECT_EFFECT);
            }
            return;
        }
        showPieceActions();
    }


    private void showPossibleActions(Coordinate atCoordinate) {
        List<Action> possibleActions = boardActionSpaceState.e2.get(atCoordinate);
        if (possibleActions == null || possibleActions.isEmpty()) {
            Arrays.stream(targetIcons).forEach(targetIcon -> {
                targetIcon.setVisible(false);
            });
            return;
        }
        for (int i = 0; i < possibleActions.size(); i++) {
            Action action = possibleActions.get(i);
            Coordinate destination = action.getDestination();
            positionElementAtBoardCoordinate(targetIcons[i], destination);
            targetIcons[i].setVisible(true);
        }
    }

    /**
     * Undoes the last move if possible
     * @param onLastActionUndone runnable to call when the last action is done
     * @return if it was successful or not (false if no actions to undo found)
     * @throws InterruptedException if the application quits
     */
    private boolean undoLastMove(Runnable onLastActionUndone) throws InterruptedException {

        Tuple<Boolean, MoveResult> undoQuery = board.undoLastMove();
        if (!undoQuery.e1 || undoQuery.e2 == null) {
            return false;
        }
        MoveResult moveResult = undoQuery.e2;

        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        double durationMs = App.PIECE_TAKEBACK_DURATION * 1000;

        int size = moveResult.getActionResults().size();

        for (int i = size - 1; i >= 0; i--) {
            final int index = i;
            scheduler.schedule(() -> {
                javafx.application.Platform.runLater(() -> {
                    undoActionResult(moveResult.getActionResults().get(index), onLastActionUndone);
                });
            }, (long) (durationMs * (size - 1 - index)), TimeUnit.MILLISECONDS);
        }

        scheduler.schedule(scheduler::shutdown, (long) (durationMs * size), TimeUnit.MILLISECONDS);

        return true;
    }




    private static Button createGridButton(boolean isWhite) {
        Button gridBtn = new Button("");
        gridBtn.setStyle("-fx-background-color: " + (isWhite ? App.WHITE_GRID_COLOR : App.BLACK_GRID_COLOR));
        gridBtn.setPrefSize(GRID_ENTRY_SIDE_LENGTH, GRID_ENTRY_SIDE_LENGTH);
        return gridBtn;
    }

    private static ImageView createPieceImgView(int piece) {
        Image image = new Image(App.getPieceImagePath(piece));
        ImageView imgView = new ImageView(image);
        imgView.setFitWidth(GRID_ENTRY_SIDE_LENGTH);
        imgView.setFitHeight(GRID_ENTRY_SIDE_LENGTH);
        imgView.setMouseTransparent(true);
        return imgView;
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handlePreferences() {
        App.setScene("preferences.fxml");
    }

}
