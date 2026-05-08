package model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DenseLayerTest {

    @Test
    public void forwardAppliesWeightsBiasAndRelu() {
        DenseLayer layer = new DenseLayer(3, 2);
        layer.weights = new double[][]{
                new double[]{1.0, -1.0, 0.5},
                new double[]{0.0, 2.0, -1.0}
        };
        layer.bias = new double[]{0.0, -0.5};

        double[] output = layer.forward(new double[]{2.0, 1.0, -1.0});

        assertEquals(0.5, output[0], 1e-12);
        assertEquals(2.5, output[1], 1e-12);
    }

    @Test
    public void policyOutputMatchesSoftmaxOfLogits() {
        DenseLayer layer = new DenseLayer(2, 3);
        layer.weights = new double[][]{
                new double[]{1.0, 0.0},
                new double[]{0.0, 0.0},
                new double[]{-1.0, 0.0}
        };
        layer.bias = new double[]{0.0, 0.0, 0.0};

        double[] output = layer.policyOutput(new double[]{1.0, 0.0});

        double denom = Math.exp(1.0) + Math.exp(0.0) + Math.exp(-1.0);
        assertEquals(Math.exp(1.0) / denom, output[0], 1e-12);
        assertEquals(Math.exp(0.0) / denom, output[1], 1e-12);
        assertEquals(Math.exp(-1.0) / denom, output[2], 1e-12);
    }

    @Test
    public void valueOutputUsesTanhActivation() {
        DenseLayer layer = new DenseLayer(2, 1);
        layer.weights = new double[][]{new double[]{1.0, 1.0}};
        layer.bias = new double[]{0.0};

        double value = layer.valueOutput(new double[]{1.0, 1.0});

        assertEquals(Math.tanh(2.0), value, 1e-12);
        assertEquals(value, layer.getPostActOutput()[0], 1e-12);
    }

    @Test
    public void backwardNoActivationComputesExpectedGradients() {
        DenseLayer layer = new DenseLayer(2, 2);
        layer.weights = new double[][]{
                new double[]{1.0, 0.0},
                new double[]{0.0, 1.0}
        };
        layer.bias = new double[]{0.0, 0.0};

        double[] gradientToPass = layer.backwardNoActivation(
                new double[]{1.0, 2.0},
                new double[]{3.0, 4.0}
        );

        assertArrayEquals(new double[]{1.0, 2.0}, gradientToPass, 1e-12);

        double[][] weightGrads = layer.getWeightGradients();
        assertEquals(3.0, weightGrads[0][0], 1e-12);
        assertEquals(4.0, weightGrads[0][1], 1e-12);
        assertEquals(6.0, weightGrads[1][0], 1e-12);
        assertEquals(8.0, weightGrads[1][1], 1e-12);

        assertArrayEquals(new double[]{1.0, 2.0}, layer.getBiasGradient(), 1e-12);
    }
}