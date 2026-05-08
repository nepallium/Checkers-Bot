package model;

public class MSEloss {
    private double prediction;
    private double expected;
    private double loss;

    public double forward(double prediction, double expected) {
        this.prediction = prediction;
        this.expected = expected;

        this.loss = Math.pow(prediction - expected, 2);
        
        return this.loss;
    }

    public double backward() {
        return 2 * (prediction - expected);
    }

    public double getLoss() {
        return this.loss;
    }

}