package model;
import model.ConvolutionalLayer;

public class ResidualBlock {
    ConvolutionalLayer layer1;
    ConvolutionalLayer layer2;

    public ResidualBlock(ConvolutionalLayer layer1, ConvolutionalLayer layer2) {
        this.layer1 = layer1;
        this.layer2 = layer2;
    }

    public double[][][] forward(double[][][] input) {
        double[][][] x;
        x = layer1.forwardWithActivation(input);
        x = layer2.forwardNoActivation(x);
        double[][][] out = add3DArrays(x, input);
        for (int i = 0; i < out.length; i++) {
            for(int j = 0; j < out[i].length; j++) {
                for (int k = 0; k < out[i][j].length; k++) {
                    out[i][j][k] = Activation.relu(out[i][j][k]);
                }
            }
        }

        return out;
    }

    public double[][][] add3DArrays(double[][][] x, double[][][] y) {
        double[][][] sum = new double[x.length][x[0].length][x[0][0].length];

        for (int i = 0; i < x.length; i++) {
            for(int j = 0; j < x[i].length; j++) {
                for (int k = 0; k < x[i][j].length; k++) {
                    sum[i][j][k] = x[i][j][k] + y[i][j][k];
                }
            }
        }

        return sum;
    }

}