package model;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

import game.Move;

public class LossFunctionsTest {

    @BeforeClass
    public static void setUpMoveSpace() {
        Move.init();
    }

    @Test
    public void mseLossMatchesExpectedValueAndGradient() {
        MSEloss mse = new MSEloss();

        double loss = mse.forward(0.25, 0.5);

        assertEquals(0.0625, loss, 1e-12);
        assertEquals(-0.5, mse.backward(), 1e-12);
    }

    @Test
    public void crossEntropyLossMatchesExpectedValueAndGradient() {
        int idxA = 5;
        int idxB = 7;

        double[] pred = new double[Move.GLOBAL_MOVE_SPACE_SIZE];
        pred[idxA] = 0.6;
        pred[idxB] = 0.25;
        pred[0] = 0.1;

        double[] target = new double[]{0.7, 0.3};

        CrossEntropyLoss ce = new CrossEntropyLoss();
        double loss = ce.forward(
                pred,
                target,
                Arrays.asList(Move.GLOBAL_MOVE_SPACE[idxA], Move.GLOBAL_MOVE_SPACE[idxB])
        );

        double expected = -(0.7 * Math.log(0.6) + 0.3 * Math.log(0.25));
        assertEquals(expected, loss, 1e-12);

        double[] grad = ce.backward();
        assertEquals(0.6 - 0.7, grad[idxA], 1e-12);
        assertEquals(0.25 - 0.3, grad[idxB], 1e-12);
        assertEquals(0.1, grad[0], 1e-12);
    }
}