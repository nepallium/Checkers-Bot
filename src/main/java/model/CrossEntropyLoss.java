package model;

import java.util.List;

import game.Move;
import lombok.Getter;

public class CrossEntropyLoss {
    private double[] prediction;
    private double[] target;
    private List<Move> legalMovesForTarget;
    @Getter
    private double loss;

    // pred should already be softmax output (probabilities that sum to 1)
    public double forward(double[] prediction, double[] target, List<Move> legalMovesForTarget) {
        this.prediction = prediction;
        this.target = target;
        this.legalMovesForTarget = legalMovesForTarget;

        double sum = 0;
        for (int i = 0; i < target.length; i++) {
            int idx = getGlobalMoveIndex(legalMovesForTarget.get(i));
            double clipped = Math.max(prediction[idx], 1e-15);
            sum += target[i] * Math.log(clipped);
        }

        this.loss = -sum;
        return this.loss;
    }

    private int getGlobalMoveIndex(Move move) {
        for (int i = 0; i < Move.GLOBAL_MOVE_SPACE_SIZE; i++) {
            if (move.equals(Move.GLOBAL_MOVE_SPACE[i])) {
                return i;
            }
        }
        throw new IllegalStateException("Move not found in global move space: " + move);
    }

    // Combined gradient of Softmax + CrossEntropy: dL/dz[i] = pred[i] - target[i]
    public double[] backward() {
        return Activation.softmaxDeriv(prediction, target, legalMovesForTarget);
    }
}