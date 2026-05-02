package training;

import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TrainingExampleTest {

    @Test
    public void constructorStoresBasicFields() {
        double[][][] state = TrainingTestSupport.EMPTY_STATE;
        double[] pi = new double[]{0.25, 0.75};

        TrainingExample example = new TrainingExample(state, pi, true);

        assertNotSame(state, example.state);
        assertArrayEquals(pi, example.pi, 0.0);
        assertTrue(example.whiteToMove);
        assertNull(example.legalMoves);
        assertNull(example.z);
    }

    @Test
    public void constructorWithLegalMovesKeepsProvidedList() {
        double[][][] state = TrainingTestSupport.EMPTY_STATE;
        double[] pi = new double[]{1.0};
        java.util.List<game.Move> legalMoves = Collections.emptyList();

        TrainingExample example = new TrainingExample(state, pi, legalMoves, false);

        assertNotSame(state, example.state);
        assertArrayEquals(pi, example.pi, 0.0);
        assertSame(legalMoves, example.legalMoves);
        assertFalse(example.whiteToMove);
        assertNull(example.z);
    }
}