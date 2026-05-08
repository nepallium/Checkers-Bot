package training;

import java.util.Collections;
import java.util.List;

import game.Board;
import game.Move;
import model.NeuralNet;

final class TrainingTestSupport {
    static final double[][][] EMPTY_STATE = new double[NeuralNet.CHANNELS][8][8];

    private TrainingTestSupport() {
    }

    static TrainingExample exampleWithValue(double z) {
        TrainingExample example = new TrainingExample(EMPTY_STATE, new double[]{1.0}, true);
        example.z = z;
        return example;
    }

    static TrainingExample exampleFromBoard(Board board) {
        List<Move> legalMoves = board.getBoardMoveSpace();
        if (legalMoves.isEmpty()) {
            throw new IllegalStateException("Expected the board to have at least one legal move");
        }

        Move chosenMove = legalMoves.get(0);
        TrainingExample example = new TrainingExample(
                board.splitBoardChannels(),
                new double[]{1.0},
                Collections.singletonList(chosenMove),
                board.isWhiteToMove()
        );
        example.z = board.isWhiteToMove() ? 1.0 : -1.0;
        return example;
    }
}