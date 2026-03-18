package model;

import game.Move;

import java.util.Arrays;
import java.util.List;

public class Activation {

    public static double relu(double x) {
        return Math.max(0, x);
    }

    public static double reluDeriv(double x) {
        return x > 0 ? 1.0 : 0.0;
    }

    public static double tanh(double x) {
        return Math.tanh(x);
    }

    public static double tanhDeriv(double x) {
        double t = Math.tanh(x);
        return 1.0 - t * t;
    }


    public static double[] softmax(double[] x) {
//        System.out.println("POSSIBLY RAW POLICY: " + Arrays.toString(x));

        double max = Double.NEGATIVE_INFINITY;
        for (double v : x) max = Math.max(max, v);

        double sum = 0.0;
        double[] out = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            out[i] = Math.exp(x[i] - max);
            sum += out[i];
        }

        for (int i = 0; i < x.length; i++) {
            out[i] /= sum;
        }

//        System.out.println("PROBABILITY DISTRIBUTION: " + Arrays.toString(out));
        return out;
    }

    /**
     * Derivative of softmax wrt pre-activation input
     * @param pred predicted values
     * @param target actual values
     * @return update strengths
     */
    public static double[] softmaxDeriv(double[] pred, double[] target, List<Move> legalMovesForTarget) {
        double[] grad = new double[pred.length];

        double[] indexes = new double[target.length];

        for (int a = 0; a < target.length; a++) {
            for (int b = 0; b < Move.GLOBAL_MOVE_SPACE_SIZE; b++) {
                if (legalMovesForTarget.get(a) == Move.GLOBAL_MOVE_SPACE[b]) {
                    indexes[a] = b;
                    break;
                }
            }
        }

        for (int i = 0; i < pred.length; i++) {
            boolean check = false;

            for (int j = 0; j < indexes.length; j++) {
                if (indexes[j] == i) {
                    grad[i] = pred[i] - target[j];
                    check = true;
                    break;
                }
            }

            if (check) continue;

            grad[i] = pred[i] - 0;
        }
        return grad;
    }
}
