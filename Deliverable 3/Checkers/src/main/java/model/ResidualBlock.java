package model;

import model.ConvolutionalLayer;

public class ResidualBlock {
    ConvolutionalLayer layer1;
    ConvolutionalLayer layer2;

    public double[][][] inputToBlock;

    private double[][][] postActivationOutput2;

    public ResidualBlock(ConvolutionalLayer layer1, ConvolutionalLayer layer2) {
        this.layer1 = layer1;
        this.layer2 = layer2;
    }

    public double[][][] forward(double[][][] input) {
        this.inputToBlock = input;

        double[][][] x;
        x = layer1.forwardWithActivation(input);
        x = layer2.forwardNoActivation(x);
        double[][][] out = add3DArrays(x, input);
        for (int i = 0; i < out.length; i++) {
            for (int j = 0; j < out[i].length; j++) {
                for (int k = 0; k < out[i][j].length; k++) {
                    out[i][j][k] = Activation.relu(out[i][j][k]);
                }
            }
        }

        this.postActivationOutput2 = out;
        layer2.setPostActivationOutput(out);
        return out;
    }

    public double[][][] backward(double[][][] gradientFromNext) {

        double[][][] gradAfterRelu = new double[gradientFromNext.length][8][8];
        for (int i = 0; i < gradAfterRelu.length; i++)
            for (int j = 0; j < 8; j++)
                for (int k = 0; k < 8; k++)
                    gradAfterRelu[i][j][k] = postActivationOutput2[i][j][k] > 0
                            ? gradientFromNext[i][j][k] : 0;

        double[][][] grad2 = layer2.backwardNoActivation(gradAfterRelu, layer1.getPostActOutput());

        double[][][] grad1 = layer1.backwardWithActivation(grad2, inputToBlock);

        return add3DArrays(grad1, gradAfterRelu);
    }

    public double[][][] add3DArrays(double[][][] x, double[][][] y) {
        double[][][] sum = new double[x.length][x[0].length][x[0][0].length];

        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                for (int k = 0; k < x[i][j].length; k++) {
                    sum[i][j][k] = x[i][j][k] + y[i][j][k];
                }
            }
        }

        return sum;
    }

    public ConvolutionalLayer getLayer1() {
        return this.layer1;
    }

    public ConvolutionalLayer getLayer2() {
        return this.layer2;
    }

    public double[][][] getPostActOutput1() {
        return this.layer1.getPostActOutput();
    }

    public double[][][] getPostActOutput2() {
        return this.postActivationOutput2;
    }
}
