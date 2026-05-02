package model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class NeuralNetUtilsTest {

    @Test
    public void flattenAndUnflattenRoundTrip() {
        NeuralNet net = new NeuralNet(12);
        double[][][] tensor = new double[12][8][8];

        for (int f = 0; f < tensor.length; f++)
            for (int r = 0; r < 8; r++)
                for (int c = 0; c < 8; c++)
                    tensor[f][r][c] = f * 100 + r * 10 + c;

        double[] flat = net.flatten(tensor);
        double[][][] restored = net.unFlatten(flat);

        for (int f = 0; f < tensor.length; f++)
            for (int r = 0; r < 8; r++)
                for (int c = 0; c < 8; c++)
                    assertEquals(tensor[f][r][c], restored[f][r][c], 0.0);
    }
}