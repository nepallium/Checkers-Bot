package model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ResidualBlockTest {

    @Test
    public void forwardWithZeroKernelsBehavesLikeReluIdentity() {
        ConvolutionalLayer layer1 = new ConvolutionalLayer(1, 1, 3, 3);
        ConvolutionalLayer layer2 = new ConvolutionalLayer(1, 1, 3, 3);
        zeroKernels(layer1);
        zeroKernels(layer2);

        ResidualBlock block = new ResidualBlock(layer1, layer2);

        double[][][] input = new double[1][8][8];
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                input[0][r][c] = 1.0;

        double[][][] output = block.forward(input);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                assertEquals(1.0, output[0][r][c], 1e-12);
            }
        }
    }

    @Test
    public void backwardWithZeroKernelsReturnsGradientFromSkipPath() {
        ConvolutionalLayer layer1 = new ConvolutionalLayer(1, 1, 3, 3);
        ConvolutionalLayer layer2 = new ConvolutionalLayer(1, 1, 3, 3);
        zeroKernels(layer1);
        zeroKernels(layer2);

        ResidualBlock block = new ResidualBlock(layer1, layer2);

        double[][][] input = new double[1][8][8];
        double[][][] gradFromNext = new double[1][8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                input[0][r][c] = 1.0;
                gradFromNext[0][r][c] = 1.0;
            }
        }

        block.forward(input);
        double[][][] grad = block.backward(gradFromNext);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                assertEquals(1.0, grad[0][r][c], 1e-12);
            }
        }
    }

    private static void zeroKernels(ConvolutionalLayer layer) {
        for (int f = 0; f < layer.kernels.length; f++)
            for (int ch = 0; ch < layer.kernels[f].length; ch++)
                for (int r = 0; r < layer.kernels[f][ch].length; r++)
                    for (int c = 0; c < layer.kernels[f][ch][r].length; c++)
                        layer.kernels[f][ch][r][c] = 0.0;
    }
}