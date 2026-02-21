package model;

public class MSEloss {
    private double predictions;
    private double expected;
    private double loss;
    
    public MSEloss() {

    }

    public double forward(double predictions, double expected) {
        this.predictions = predictions;
        this.expected = expected;

        this.loss = Math.pow(predictions - expected, 2);
        
        return this.loss;
    }

<<<<<<< Updated upstream:Deliverable 3/Checkers/src/main/java/model/MSEloss.java
    public double[] backward() {
        double [] gradient = new double[predictions.length];

        for (int i = 0; i < gradient.length; i++) {
            gradient[i] = 2*(predictions[i] - expected[i])/predictions.length;
        }

        return gradient;
=======
    public double backward() {
        return return 2 * (prediction - expected);
>>>>>>> Stashed changes:Deliverable 3/Checkers/src/main/java/model/MSEloss
    }

    public double getLoss() {
        return this.loss;
    }

}