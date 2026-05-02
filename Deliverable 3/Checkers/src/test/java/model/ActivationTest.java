package model;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

import game.Move;

public class ActivationTest {

    @BeforeClass
    public static void setUpMoveSpace() {
        Move.init();
    }

    @Test
    public void reluAndDerivBehaveAsExpected() {
        assertEquals(0.0, Activation.relu(-2.0), 0.0);
        assertEquals(3.5, Activation.relu(3.5), 0.0);
        assertEquals(0.0, Activation.reluDeriv(-1.0), 0.0);
        assertEquals(0.0, Activation.reluDeriv(0.0), 0.0);
        assertEquals(1.0, Activation.reluDeriv(2.0), 0.0);
    }

    @Test
    public void tanhAndDerivMatchMathLibrary() {
        assertEquals(0.0, Activation.tanh(0.0), 0.0);
        assertEquals(1.0, Activation.tanhDeriv(0.0), 1e-12);
        assertEquals(Math.tanh(1.5), Activation.tanh(1.5), 1e-12);
    }

    @Test
    public void softmaxProducesExpectedProbabilities() {
        double[] input = new double[]{2.0, 1.0, 0.0};
        double[] output = Activation.softmax(input);

        double denom = Math.exp(2.0) + Math.exp(1.0) + Math.exp(0.0);
        assertEquals(Math.exp(2.0) / denom, output[0], 1e-12);
        assertEquals(Math.exp(1.0) / denom, output[1], 1e-12);
        assertEquals(Math.exp(0.0) / denom, output[2], 1e-12);

        assertTrue(output[0] > output[1]);
        assertTrue(output[1] > output[2]);
    }

    @Test
    public void softmaxDerivReturnsPredMinusTargetOnLegalMoves() {
        int idxA = 5;
        int idxB = 7;

        double[] pred = new double[Move.GLOBAL_MOVE_SPACE_SIZE];
        pred[idxA] = 0.6;
        pred[idxB] = 0.25;
        pred[0] = 0.1;

        double[] target = new double[]{0.7, 0.3};

        double[] grad = Activation.softmaxDeriv(
                pred,
                target,
                Arrays.asList(Move.GLOBAL_MOVE_SPACE[idxA], Move.GLOBAL_MOVE_SPACE[idxB])
        );

        assertEquals(0.6 - 0.7, grad[idxA], 1e-12);
        assertEquals(0.25 - 0.3, grad[idxB], 1e-12);
        assertEquals(0.1, grad[0], 1e-12);
    }
}