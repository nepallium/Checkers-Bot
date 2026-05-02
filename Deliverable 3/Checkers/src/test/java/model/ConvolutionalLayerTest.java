package model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ConvolutionalLayerTest {

    @Test
    public void forwardWithActivationMatchesNeighborhoodSum() {
        ConvolutionalLayer layer = new ConvolutionalLayer(1, 1, 3, 3);
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                layer.kernels[0][0][i][j] = 1.0;
        layer.bias[0] = 0.0;

        double[][][] board = new double[1][8][8];
        board[0][3][3] = 1.0;

        double[][][] output = layer.forwardWithActivation(board);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                double expected = (Math.abs(r - 3) <= 1 && Math.abs(c - 3) <= 1) ? 1.0 : 0.0;
                assertEquals(expected, output[0][r][c], 1e-12);
            }
        }
    }

    @Test
    public void backwardNoActivationComputesBiasAndCenterKernelGradient() {
        ConvolutionalLayer layer = new ConvolutionalLayer(1, 1, 3, 3);

        double[][][] input = new double[1][8][8];
        double[][][] grad = new double[1][8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                input[0][r][c] = 1.0;
                grad[0][r][c] = 1.0;
            }
        }

        layer.backwardNoActivation(grad, input);

        assertEquals(64.0, layer.getBiasGradients()[0], 1e-12);
        assertEquals(64.0, layer.getKernelGradients()[0][0][1][1], 1e-12);
    }
}