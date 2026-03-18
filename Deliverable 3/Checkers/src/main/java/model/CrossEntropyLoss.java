package model;

import game.Move;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

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

        double[] indexes = new double[target.length];

        for (int a = 0; a < target.length; a++) {
            for (int b = 0; b < Move.GLOBAL_MOVE_SPACE_SIZE; b++) {
                if (legalMovesForTarget.get(a) == Move.GLOBAL_MOVE_SPACE[b]) {
                    indexes[a] = b;
                    break;
                }
            }
        }

        double sum = 0;
        for (int i = 0; i < prediction.length; i++) {
            for (int j = 0; j < indexes.length; j++) {
                if (indexes[j] == i) {
                    double clipped = Math.max(prediction[i], 1e-15);
                    sum += target[j] * Math.log(clipped);
                    break;
                }
            }
        }

        this.loss = -sum;
        return this.loss;
    }

    // Combined gradient of Softmax + CrossEntropy: dL/dz[i] = pred[i] - target[i]
    public double[] backward() {
        return Activation.softmaxDeriv(prediction, target, legalMovesForTarget);
    }
}