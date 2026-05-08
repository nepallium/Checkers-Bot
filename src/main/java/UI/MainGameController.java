package UI;

import Util.Tuple;
import game.*;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
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

    private static final Map<Integer, Color> DEFAULT_PIECE_COLORS = Map.of(
            1,  Color.web("#f0f0f0"),   // white man
            2,  Color.web("#ffe066"),   // white king
            -1, Color.web("#222222"),   // black man
            -2, Color.web("#cc3333")    // black king
    );

    private static final ColorAdjust PIECE_FORCED_ACTION_EFFECT = new ColorAdjust(0, 0, .15, 0.85);
    private static final ColorAdjust PIECE_SELECT_EFFECT        = new ColorAdjust(0, 0, -0.15, 0.5);
    private static final ColorAdjust PIECE_DEFAULT_EFFECT       = new ColorAdjust();

    public Pane    boardPane;
    public Pane    boardParentPane;
    public ImageView homeImg;
    public ImageView undoImg;

    public Board board = new Board();

    private final boolean playerPlaysAsWhite = true;
    private boolean inputsDisabled           = false;
    private Coordinate selectedPieceCoordinate = null;

    Tuple<Boolean, Map<Coordinate, List<Action>>> boardActionSpaceState = board.getBoardActionSpace();
    HashMap<Coordinate, ImageView> pieceUIMap = new HashMap<>();

    private final ImageView[] targetIcons      = new ImageView[4];
    private Stack<ImageView>  capturedPieceUIs = new Stack<>();

    private NeuralNet neuralNet;
    private MCTS      mcts;

    private final AppPreferences prefs = new AppPreferences();

    @FXML
    private void initialize() {
        Move.init();
        initializeBoard();

        neuralNet = new NeuralNet(12);
        try {
            neuralNet.load(App.NEURAL_NET_PATH);
            System.out.println("Model loaded!!");
        } catch (IOException e) {
            System.out.println("No saved model found, using random weights");
        }
        mcts = new MCTS(neuralNet);
        mcts.setSIMULATIONS(App.getMctsSimulations());
        System.out.println("Simulations amount set to " + App.getMctsSimulations());

        undoImg.setOnMouseClicked(e -> {
            try { onUndoButtonPressed(); }
            catch (InterruptedException ex) { throw new RuntimeException(ex); }
        });
    }

    private void initializeBoard() {
        boardPane.setPrefSize(8 * GRID_ENTRY_SIDE_LENGTH, 8 * GRID_ENTRY_SIDE_LENGTH);

        for (int x = 0; x < 8; x++) {
            for (int y = x % 2 == 0 ? 1 : 0; y < 8; y += 2) {
                Button gridBtn = createGridButton(false, prefs);
                gridBtn.setOnAction((ActionEvent e) -> selectCoordinate(null));
                boardPane.getChildren().add(gridBtn);
                positionElementAtBoardCoordinate(gridBtn, new Coordinate(x, y));
            }

            for (int y = x % 2 == 0 ? 0 : 1; y < 8; y += 2) {
                Button gridBtn = createGridButton(true, prefs);
                Coordinate coordinate = new Coordinate(x, y);
                gridBtn.setOnAction((ActionEvent e) -> selectCoordinate(coordinate));
                boardPane.getChildren().add(gridBtn);
                positionElementAtBoardCoordinate(gridBtn, coordinate);

                int piece = board.cells[y][x];
                if (piece != 0) {
                    ImageView imgView = createPieceImgView(piece, prefs);
                    positionElementAtBoardCoordinate(imgView, coordinate);
                    pieceUIMap.put(coordinate, imgView);
                    boardParentPane.getChildren().add(imgView);
                }
            }
        }

        Image targetImage = loadTargetImage(prefs);
        for (int i = 0; i < targetIcons.length; i++) {
            targetIcons[i] = new ImageView(targetImage);
            targetIcons[i].setFitHeight(GRID_ENTRY_SIDE_LENGTH);
            targetIcons[i].setFitWidth(GRID_ENTRY_SIDE_LENGTH);
            targetIcons[i].setVisible(false);
            targetIcons[i].setMouseTransparent(true);
        }
        boardParentPane.getChildren().addAll(targetIcons);
    }

    @FXML
    private void onExitMenuItemClicked() {
        System.exit(0);
    }

    @FXML
    private void onPreferencesMenuItemClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/View/preferences.fxml")
            );
            Parent root = loader.load();

            PreferencesController controller = loader.getController();
            controller.setPreferences(prefs);
            controller.setOnApply(this::rebuildBoard);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Preferences");
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tears down and rebuilds the board visuals using the current AppPreferences.
     * Piece positions are preserved from the live board state.
     */
    private void rebuildBoard() {
        // Clear old visuals
        boardPane.getChildren().clear();
        boardParentPane.getChildren().clear();
        pieceUIMap.clear();

        for (int x = 0; x < 8; x++) {
            for (int y = x % 2 == 0 ? 1 : 0; y < 8; y += 2) {
                Button gridBtn = createGridButton(false, prefs);
                gridBtn.setOnAction((ActionEvent e) -> selectCoordinate(null));
                boardPane.getChildren().add(gridBtn);
                positionElementAtBoardCoordinate(gridBtn, new Coordinate(x, y));
            }

            for (int y = x % 2 == 0 ? 0 : 1; y < 8; y += 2) {
                Button gridBtn = createGridButton(true, prefs);
                Coordinate coordinate = new Coordinate(x, y);
                gridBtn.setOnAction((ActionEvent e) -> selectCoordinate(coordinate));
                boardPane.getChildren().add(gridBtn);
                positionElementAtBoardCoordinate(gridBtn, coordinate);

                int piece = board.cells[y][x];
                if (piece != 0) {
                    ImageView imgView = createPieceImgView(piece, prefs);
                    positionElementAtBoardCoordinate(imgView, coordinate);
                    pieceUIMap.put(coordinate, imgView);
                    boardParentPane.getChildren().add(imgView);
                }
            }
        }

        Image targetImage = loadTargetImage(prefs);
        for (int i = 0; i < targetIcons.length; i++) {
            targetIcons[i] = new ImageView(targetImage);
            targetIcons[i].setFitHeight(GRID_ENTRY_SIDE_LENGTH);
            targetIcons[i].setFitWidth(GRID_ENTRY_SIDE_LENGTH);
            targetIcons[i].setVisible(false);
            targetIcons[i].setMouseTransparent(true);
        }
        boardParentPane.getChildren().addAll(targetIcons);

        boardParentPane.getChildren().add(0, boardPane);

        showPieceActions();
    }

    private Button createGridButton(boolean isDark, AppPreferences prefs) {
        Button gridBtn = new Button("");
        String color = isDark ? prefs.getDarkSquareColor() : prefs.getLightSquareColor();
        gridBtn.setStyle("-fx-background-color: " + color + ";");
        gridBtn.setPrefSize(GRID_ENTRY_SIDE_LENGTH, GRID_ENTRY_SIDE_LENGTH);
        return gridBtn;
    }

    /**
     * Creates a piece ImageView (DOG style) or a colored rectangle (DEFAULT style).
     * Both are returned as ImageView for uniform handling; the rectangle is
     * rendered into a WritableImage snapshot when DEFAULT is selected.
     */
    private ImageView createPieceImgView(int piece, AppPreferences prefs) {
        if (prefs.getPieceStyle() == AppPreferences.PieceStyle.DOG) {
            Image image = new Image(App.getPieceImagePath(piece));
            ImageView imgView = new ImageView(image);
            imgView.setFitWidth(GRID_ENTRY_SIDE_LENGTH);
            imgView.setFitHeight(GRID_ENTRY_SIDE_LENGTH);
            imgView.setMouseTransparent(true);
            return imgView;
        } else {
            Color color = DEFAULT_PIECE_COLORS.getOrDefault(piece, Color.GRAY);

            javafx.scene.canvas.Canvas canvas =
                    new javafx.scene.canvas.Canvas(GRID_ENTRY_SIDE_LENGTH, GRID_ENTRY_SIDE_LENGTH);
            javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

            gc.clearRect(0, 0, GRID_ENTRY_SIDE_LENGTH, GRID_ENTRY_SIDE_LENGTH);

            double margin = 10;
            gc.setFill(color);
            gc.fillOval(margin, margin,
                    GRID_ENTRY_SIDE_LENGTH - 2 * margin,
                    GRID_ENTRY_SIDE_LENGTH - 2 * margin);

            boolean isKing = (piece == 2 || piece == -2);
            if (isKing) {
                gc.setStroke(Color.GOLD);
                gc.setLineWidth(4);
                gc.strokeOval(margin + 6, margin + 6,
                        GRID_ENTRY_SIDE_LENGTH - 2 * (margin + 6),
                        GRID_ENTRY_SIDE_LENGTH - 2 * (margin + 6));
            }

            javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
            params.setFill(Color.TRANSPARENT);

            javafx.scene.image.WritableImage wi =
                    new javafx.scene.image.WritableImage(GRID_ENTRY_SIDE_LENGTH, GRID_ENTRY_SIDE_LENGTH);
            canvas.snapshot(params, wi);

            ImageView imgView = new ImageView(wi);
            imgView.setFitWidth(GRID_ENTRY_SIDE_LENGTH);
            imgView.setFitHeight(GRID_ENTRY_SIDE_LENGTH);
            imgView.setMouseTransparent(true);
            return imgView;
        }
    }

    private Image loadTargetImage(AppPreferences prefs) {
        String path = prefs.getTargetIcon() == AppPreferences.TargetIcon.CIRCLE
                ? App.TARGET_ICON_IMAGE_PATH
                : App.TARGET_X_IMAGE_PATH;
        return new Image(App.class.getResource(App.IMAGES_FILE_PATH_PREFIX + path).toExternalForm());
    }

    private void onUndoButtonPressed() throws InterruptedException {
        if (inputsDisabled) return;
        inputsDisabled = true;
        Runnable undoSecondMove = () -> {
            try {
                boolean success = undoLastMove(() -> {
                    inputsDisabled = false;
                    updateBoardState();
                    showPieceActions();
                    System.out.println(board);
                });
                if (!success) inputsDisabled = false;
            } catch (InterruptedException e) {
                inputsDisabled = false;
                throw new RuntimeException(e);
            }
        };
        if (!undoLastMove(undoSecondMove)) inputsDisabled = false;
    }

    private void undoActionResult(ActionResult actionResult, Runnable onActionUndone) {
        Coordinate start       = actionResult.getStart();
        Coordinate destination = actionResult.getDestination();

        ImageView pieceUI = pieceUIMap.get(destination);
        pieceUIMap.remove(destination);
        pieceUIMap.put(start, pieceUI);
        showPieceMoving(destination, start, pieceUI, actionResult.isPromotion(), onActionUndone, App.PIECE_TAKEBACK_DURATION);

        Coordinate capturedCoordinate = actionResult.getCaptureCoordinate();
        if (capturedCoordinate != null) {
            ImageView capturedPieceImg = capturedPieceUIs.pop();
            pieceUIMap.put(capturedCoordinate, capturedPieceImg);
            capturedPieceImg.setVisible(true);

            ScaleTransition st = new ScaleTransition(
                    Duration.seconds(App.PIECE_TAKEBACK_DURATION * 0.5), capturedPieceImg);
            st.setToX(1);
            st.setToY(1);
            st.play();
        }
    }

    private void positionElementAtBoardCoordinate(Node node, Coordinate coordinate) {
        Tuple<Double, Double> layoutXY = getLayoutAtBoardCoordinate(coordinate);
        node.setLayoutX(layoutXY.e1);
        node.setLayoutY(layoutXY.e2);
    }

    private Tuple<Double, Double> getLayoutAtBoardCoordinate(Coordinate coordinate) {
        double bx = boardPane.getLayoutX(), by = boardPane.getLayoutY();
        return new Tuple<>(bx + coordinate.getX() * GRID_ENTRY_SIDE_LENGTH,
                by + (7 - coordinate.getY()) * GRID_ENTRY_SIDE_LENGTH);
    }

    private void selectCoordinate(Coordinate coordinate) {
        if (inputsDisabled) return;
        if (board.isGameOver()) { System.out.println("Game over"); return; }

        if (selectedPieceCoordinate != null) {
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
            ImageView prevSelectedPieceUI = pieceUIMap.get(selectedPieceCoordinate);
            if (prevSelectedPieceUI != null) prevSelectedPieceUI.setEffect(PIECE_DEFAULT_EFFECT);
            if (requestedAction != null) applyActionToBoard(requestedAction);
            setSelectedPieceCoordinate(null);
            return;
        }
        setSelectedPieceCoordinate(coordinate);
    }

    private void updateBoardState() {
        boardActionSpaceState = board.getBoardActionSpace();
        if (boardActionSpaceState == null) {
            inputsDisabled = true;
            Arrays.stream(targetIcons).forEach(t -> t.setVisible(false));
            pieceUIMap.forEach((c, ui) -> ui.setEffect(PIECE_DEFAULT_EFFECT));
            GameResult gameResult = board.getGameResult();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("GAME OVER");
            alert.setHeaderText(gameResult.getMessageText());
            alert.setContentText("Good game");
            alert.showAndWait();
        }
    }

    private void applyActionToBoard(Action action) {
        ActionResult actionResult = board.applyAction(action, true);
        if (actionResult == null) { System.out.println("INVALID ACTION: " + action); return; }
        updateBoardState();
        boolean canStillPlay = board.hasForcedPieceCaptureCoordinate();

        ImageView pieceUI = pieceUIMap.remove(action.getStart());
        pieceUIMap.put(action.getDestination(), pieceUI);
        inputsDisabled = true;
        showAction(actionResult, pieceUI, () -> { inputsDisabled = !canStillPlay; });
        showPieceActions();

        if (board.isGameOver() || (board.isWhiteToMove() == playerPlaysAsWhite)) return;
        inputsDisabled = true;
        ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
        sched.schedule(() -> {
            javafx.application.Platform.runLater(this::doAIMove);
            sched.close();
        }, 500, TimeUnit.MILLISECONDS);
    }

    private void applyMoveToBoard(Move move) throws InterruptedException {
        MoveResult moveResult = board.applyMove(move);
        if (moveResult == null) { System.out.println("INVALID MOVE: " + move); return; }
        updateBoardState();
        inputsDisabled = true;

        ImageView pieceUI = pieceUIMap.remove(moveResult.getStart());
        pieceUIMap.put(move.getDestination(), pieceUI);

        ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
        Consumer<Integer> apply = new Consumer<>() {
            @Override
            public void accept(Integer idx) {
                ActionResult ar = idx < moveResult.getActionResults().size()
                        ? moveResult.getActionResults().get(idx) : null;
                Runnable step = ar != null ? () -> {
                    showAction(ar, pieceUI, () -> {
                        if (idx < moveResult.getActionResults().size() - 1) return;
                        inputsDisabled = false;
                    });
                    if (idx == moveResult.getActionResults().size() - 1) showPieceActions();
                    this.accept(idx + 1);
                } : sched::close;
                sched.schedule(step, 500, TimeUnit.MILLISECONDS);
            }
        };
        apply.accept(0);
    }

    private void showPieceMoving(Coordinate start, Coordinate dest, ImageView pieceImg,
                                 boolean isPromotion, Runnable onFinished, double moveDuration) {
        Tuple<Double, Double> xy1 = getLayoutAtBoardCoordinate(start);
        Tuple<Double, Double> xy2 = getLayoutAtBoardCoordinate(dest);

        TranslateTransition trans = new TranslateTransition(Duration.seconds(moveDuration), pieceImg);
        trans.setByX(xy2.e1 - xy1.e1);
        trans.setByY(xy2.e2 - xy1.e2);
        trans.setOnFinished(e -> {
            if (isPromotion) {
                if (prefs.getPieceStyle() == AppPreferences.PieceStyle.DOG) {
                    pieceImg.setImage(new Image(App.getPieceImagePath(board.getPieceAt(dest))));
                } else {
                    // Rebuild the colored circle for the promoted piece
                    ImageView promoted = createPieceImgView(board.getPieceAt(dest), prefs);
                    pieceImg.setImage(promoted.getImage());
                }
            }
            if (onFinished != null) onFinished.run();
        });
        trans.play();
    }

    private void showAction(ActionResult actionResult, ImageView pieceUI, Runnable onActionDone) {
        Coordinate start = actionResult.getStart();
        Coordinate dest  = actionResult.getDestination();

        ImageView capturedImg = actionResult.getCaptureCoordinate() == null
                ? null : pieceUIMap.remove(actionResult.getCaptureCoordinate());
        if (capturedImg != null) capturedPieceUIs.add(capturedImg);

        pieceUIMap.remove(start);
        pieceUIMap.put(dest, pieceUI);
        showPieceMoving(start, dest, pieceUI, actionResult.isPromotion(), onActionDone, App.PIECE_MOVE_DURATION);

        if (capturedImg == null) return;
        pieceUIMap.put(actionResult.getCaptureCoordinate(), capturedImg);

        ScaleTransition shrink = new ScaleTransition(
                Duration.seconds(App.PIECE_MOVE_DURATION * 0.5), capturedImg);
        shrink.setToX(0);
        shrink.setToY(0);
        shrink.setOnFinished(e -> capturedImg.setVisible(false));
        shrink.play();
    }

    private void doAIMove() {
        Consumer<Move> playMove = move ->
                javafx.application.Platform.runLater(() -> {
                    try { applyMoveToBoard(move); }
                    catch (InterruptedException e) { throw new RuntimeException(e); }
                });

        Thread aiThread = new Thread(() -> {
            inputsDisabled = true;
            board.splitBoardChannels();
            Tuple<double[], List<Move>> output = mcts.run(board);
            int best = 0;
            for (int i = 0; i < output.e1.length; i++)
                if (output.e1[i] > output.e1[best]) best = i;
            playMove.accept(output.e2.get(best));
        });
        aiThread.setDaemon(true);
        aiThread.start();
    }

    private void showPieceActions() {
        for (Coordinate coordinate : pieceUIMap.keySet()) {
            ImageView pieceImg = pieceUIMap.get(coordinate);
            if (pieceImg == null) continue;
            pieceImg.setEffect(
                    boardActionSpaceState.e2.get(coordinate) == null || !boardActionSpaceState.e1
                            ? PIECE_DEFAULT_EFFECT : PIECE_FORCED_ACTION_EFFECT);
        }
    }

    private void setSelectedPieceCoordinate(Coordinate coordinate) {
        if (coordinate != null && boardActionSpaceState.e2.get(coordinate) == null)
            coordinate = null;

        selectedPieceCoordinate = coordinate;
        showPossibleActions(coordinate);
        if (coordinate != null) {
            ImageView selectedPieceUI = pieceUIMap.get(coordinate);
            if (selectedPieceUI != null) selectedPieceUI.setEffect(PIECE_SELECT_EFFECT);
            return;
        }
        showPieceActions();
    }

    private void showPossibleActions(Coordinate atCoordinate) {
        List<Action> possibleActions = boardActionSpaceState.e2.get(atCoordinate);
        if (possibleActions == null || possibleActions.isEmpty()) {
            Arrays.stream(targetIcons).forEach(t -> t.setVisible(false));
            return;
        }
        for (int i = 0; i < possibleActions.size(); i++) {
            positionElementAtBoardCoordinate(targetIcons[i], possibleActions.get(i).getDestination());
            targetIcons[i].setVisible(true);
        }
    }

    private boolean undoLastMove(Runnable onLastActionUndone) throws InterruptedException {
        Tuple<Boolean, MoveResult> undoQuery = board.undoLastMove();
        if (!undoQuery.e1 || undoQuery.e2 == null) return false;

        MoveResult moveResult = undoQuery.e2;
        ScheduledExecutorService sched = Executors.newSingleThreadScheduledExecutor();
        double durationMs = App.PIECE_TAKEBACK_DURATION * 1000;
        int size = moveResult.getActionResults().size();

        for (int i = size - 1; i >= 0; i--) {
            final int index = i;
            sched.schedule(() ->
                            javafx.application.Platform.runLater(() ->
                                    undoActionResult(moveResult.getActionResults().get(index), onLastActionUndone)),
                    (long)(durationMs * (size - 1 - index)), TimeUnit.MILLISECONDS);
        }
        sched.schedule(sched::shutdown, (long)(durationMs * size), TimeUnit.MILLISECONDS);
        return true;
    }
}