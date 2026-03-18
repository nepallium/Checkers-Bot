package UI;

import Util.Tuple;
import game.*;
import javafx.animation.TranslateTransition;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainGameController {
    private static final int GRID_ENTRY_SIDE_LENGTH = 100;
    public Board board = new Board();

    private Coordinate selectedPieceCoordinate = null;
    Tuple<Boolean, Map<Coordinate, List<Action>>> boardActionSpaceState = board.getBoardActionSpace();
    HashMap<Coordinate, ImageView> pieceUIMap = new HashMap<>();

    private static final ColorAdjust PIECE_FORCED_ACTION_EFFECT = new ColorAdjust(0, 0, .15, 0.85);
    private static final ColorAdjust PIECE_SELECT_EFFECT = new ColorAdjust(0, 0, -0.15, 0.5);
    private static final ColorAdjust PIECE_DEFAULT_EFFECT = new ColorAdjust();

    private final ImageView[] targetIcons = new ImageView[4];
    public Pane boardPane;
    public Pane boardParentPane;

    public ImageView homeImg;
    public ImageView undoImg;

    private NeuralNet neuralNet;
    private MCTS mcts;
    private boolean isModelTurn = false;

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
        if (isModelTurn) return;
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
                applyActionToBoard(requestedAction, true);
            }
            setSelectedPieceCoordinate(null);
            return;
        }
        setSelectedPieceCoordinate(coordinate);
    }

    private void applyActionToBoard(Action action, boolean isHumanTurn) {
        ActionResult actionResult = board.applyAction(action, true);
        //Update values relative to board
        boardActionSpaceState = board.getBoardActionSpace();
        //Show the action being done
        ImageView movingPieceImg = pieceUIMap.get(action.getStart());
        TranslateTransition moveTrans = new TranslateTransition(Duration.seconds(App.PIECE_MOVE_DURATION));
        moveTrans.setNode(movingPieceImg);
        Tuple<Double, Double> layoutXY1 = getLayoutAtBoardCoordinate(action.getStart());
        Tuple<Double, Double> layoutXY2 = getLayoutAtBoardCoordinate(action.getDestination());
        moveTrans.setByX(layoutXY2.e1 - layoutXY1.e1);
        moveTrans.setByY(layoutXY2.e2 - layoutXY1.e2);
        moveTrans.play();
        ImageView pieceUI = pieceUIMap.remove(action.getStart());
        pieceUIMap.put(action.getDestination(), pieceUI);
        Coordinate captureCoordinate = action.getCaptureCoordinate();
        if (captureCoordinate != null) {
            pieceUIMap.remove(captureCoordinate).setVisible(false);
        }
        if (actionResult.isPromotion()) {
            pieceUI.setImage(new Image(App.getPieceImagePath(board.getPieceAt(actionResult.getDestination()))));
        }

        if (!board.isGameOver() && isHumanTurn) {
            isModelTurn = true;
            javafx.application.Platform.runLater(this::doAIMove);
        }
    }

    private void doAIMove() {
        // run MCTS on a background thread so UI doesn't freeze
        Thread aiThread = new Thread(() -> {
            double[][][] boardState = board.splitBoardChannels();
            Tuple<double[], List<Move>> output = mcts.run(board);

            int best = 0;

            for (int i = 0; i < output.e1.length; i++) {
                if (output.e1[i] > output.e1[best]) {
                    best = i;
                }
            }

            Move bestMove = output.e2.get(best);

            javafx.application.Platform.runLater(() -> {
                for (Action action : bestMove.getActions()) {
                    applyActionToBoard(action, false);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        System.out.println("Error in sleep: " + e.getMessage());;
                    }
                }
                isModelTurn = false;
            });
        });
        aiThread.setDaemon(true);
        aiThread.start();
    }

    private void showPieceActions() {
        for (Coordinate coordinate : pieceUIMap.keySet()) {
            ImageView pieceImg = pieceUIMap.get(coordinate);
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

}
