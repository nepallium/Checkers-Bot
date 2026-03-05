package UI;

import Util.Tuple;
import game.Action;
import game.ActionResult;
import game.Board;
import game.Coordinate;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import main.App;

import java.util.*;

public class MainGameController {
    private static final int GRID_ENTRY_SIDE_LENGTH = 100;
    public Board board = new Board();

    private Coordinate selectedPieceCoordinate = null;
    Map<Coordinate, List<Action>> boardActionSpace = board.getBoardActionSpace();
    HashMap<Coordinate, ImageView> pieceUIMap = new HashMap<>();


    private static final ColorAdjust PIECE_SELECT_EFFECT = new ColorAdjust(0, 0, -0.15, 0.5);
    private static final ColorAdjust DEFAULT_EFFECT = new ColorAdjust();

    private final ImageView[] targetIcons = new ImageView[4];
    public Pane boardPane;
    public Pane boardParentPane;
    @FXML
    private void initialize() {
        boardPane.setPrefSize(8 * GRID_ENTRY_SIDE_LENGTH, 8 * GRID_ENTRY_SIDE_LENGTH);
        for (int x = 0; x < 8; x++) {
            for (int y = x % 2 == 0 ? 1 : 0; y < 8; y += 2) { //for invalid coordinates, just put colored squares
                Button gridBtn = createGridButton(false);
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
        if (selectedPieceCoordinate != null) {
            //Check if can make the action
            Action requestedAction = null;
            List<Action> actions = boardActionSpace.get(selectedPieceCoordinate);
            if (actions != null && !actions.isEmpty()) {

                for (Action action : actions) {
                    if (action.getDestination().equals(coordinate)) {
                        requestedAction = action;
                        break;
                    }
                }
            }
            if (requestedAction != null) {
                applyActionToBoard(requestedAction);
            }
            //Unselect
            ImageView prevSelectedPieceUI = pieceUIMap.get(selectedPieceCoordinate);
            if (prevSelectedPieceUI != null) {
                prevSelectedPieceUI.setEffect(DEFAULT_EFFECT);
                setSelectedPieceCoordinate(null);
            }
        }
        ImageView selectedPieceUI = pieceUIMap.get(coordinate);
        if (selectedPieceUI == null) {
            setSelectedPieceCoordinate(null);
            return;
        }
        selectedPieceUI.setEffect(PIECE_SELECT_EFFECT);
        setSelectedPieceCoordinate(coordinate);
    }

    private void applyActionToBoard(Action action) {
        ActionResult actionResult = board.applyAction(action, true);
        boardActionSpace = board.getBoardActionSpace();
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
    }

    private void setSelectedPieceCoordinate(Coordinate coordinate) {
        selectedPieceCoordinate = coordinate;
        showPossibleActions(coordinate);
    }

    private void showPossibleActions(Coordinate atCoordinate) {
        List<Action> possibleActions = boardActionSpace.get(atCoordinate);
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
