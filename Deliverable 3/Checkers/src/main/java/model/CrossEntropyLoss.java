package model;

import lombok.Getter;

public class CrossEntropyLoss {
    private double[] prediction;
    private double[] target;
    @Getter
    private double loss;

    // pred should already be softmax output (probabilities that sum to 1)
    public double forward(double[] prediction, double[] target) {
        this.prediction = prediction;
        this.target = target;

        double sum = 0;
        for (int i = 0; i < prediction.length; i++) {
            // clip to avoid log(0)
            double clipped = Math.max(prediction[i], 1e-15);
            sum += target[i] * Math.log(clipped);
        }

        this.loss = -sum;
        return this.loss;
    }

    // Combined gradient of Softmax + CrossEntropy: dL/dz[i] = pred[i] - target[i]
    public double[] backward() {
        return Activation.softmaxDeriv(prediction, target);
    }
}