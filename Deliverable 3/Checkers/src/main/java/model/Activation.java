package model;

public class Activation {

    /**
     * Applies rectified linear unit (ReLU) to the value x to bound the value between (0, +inf)
     * Non-linearity activation function used in the hidden layers
     * @param x the value to bound
     * @return the bounded value
     */
    public static double relu(double x) {
        return Math.max(0, x);
    }
}
