package model;

import java.util.Arrays;

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
        System.out.println("POSSIBLY RAW POLICY: " + Arrays.toString(x));

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

        System.out.println("PROBABILITY DISTRIBUTION: " + Arrays.toString(out));
        return out;
    }
}