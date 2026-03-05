package UI;

import Util.Tuple;
import game.Board;
import game.Coordinate;
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
import main.App;

import java.util.HashMap;

public class MainGameController {
    private static final int GRID_ENTRY_SIDE_LENGTH = 100;
    public Board board = new Board();
    public Coordinate selectedPieceCoordinate = null;
    HashMap<Coordinate, ImageView> pieceUIMap = new HashMap<>();

    private static final ColorAdjust PIECE_SELECT_EFFECT = new ColorAdjust(0, 0, -0.15, 0.5);
    private static final ColorAdjust DEFAULT_EFFECT = new ColorAdjust();

    public Pane boardPane;

    @FXML
    private void initialize() {
        boardPane.setPrefSize(8 * GRID_ENTRY_SIDE_LENGTH, 8 * GRID_ENTRY_SIDE_LENGTH);

        for (int rowIdx = 0; rowIdx < 8; rowIdx++) {
            for (int xIdx = (rowIdx % 2 == 0 ? 0 : 1); xIdx < 8; xIdx += 2) {

            }
        }
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
                    onCoordinateClicked(coordinate);
                });

                boardPane.getChildren().add(gridBtn);

                positionElementAtBoardCoordinate(gridBtn, coordinate);

                int piece = board.cells[y][x];
                if (piece != 0) {
                    ImageView imgView = createPieceImgView(piece);
                    positionElementAtBoardCoordinate(imgView, coordinate);
                    boardPane.getChildren().add(imgView);
                    pieceUIMap.put(coordinate, imgView);
                }
            }
        }
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

    private void onCoordinateClicked(Coordinate coordinate) {
        selectCoordinate(coordinate);
    }

    private void selectCoordinate(Coordinate coordinate) {
        if (selectedPieceCoordinate != null) {
            ImageView prevSelectedPieceUI = pieceUIMap.get(selectedPieceCoordinate);
            if (prevSelectedPieceUI != null) {
                prevSelectedPieceUI.setEffect(DEFAULT_EFFECT);
            }
        }
        ImageView selectedPieceUI = pieceUIMap.get(coordinate);
        if (selectedPieceUI == null) {
            return;
        }
        selectedPieceUI.setEffect(PIECE_SELECT_EFFECT);
        selectedPieceCoordinate = coordinate;

    }

    private static Button createGridButton(boolean isWhite) {
        Button gridBtn = new Button("");
        gridBtn.setStyle("-fx-background-color: " + (isWhite ? App.WHITE_GRID_COLOR : App.BLACK_GRID_COLOR));
        gridBtn.setPrefSize(GRID_ENTRY_SIDE_LENGTH, GRID_ENTRY_SIDE_LENGTH);
        return gridBtn;
    }

    private static ImageView createPieceImgView(int piece) {
        Image image = new Image(App.getPieceImagePath(piece));
        ;
        ImageView imgView = new ImageView(image);
        imgView.setFitWidth(GRID_ENTRY_SIDE_LENGTH);
        imgView.setFitHeight(GRID_ENTRY_SIDE_LENGTH);
        imgView.setMouseTransparent(true);
        return imgView;
    }
}
