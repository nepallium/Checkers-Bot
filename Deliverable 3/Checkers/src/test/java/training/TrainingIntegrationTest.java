package training;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import game.Board;
import game.Move;
import model.NeuralNet;
import model.PolicyValue;

public class TrainingIntegrationTest {

    private NeuralNet net;
    private Board board;

    @BeforeClass
    public static void setUpMoveSpace() {
        Move.init();
    }

    @Before
    public void setUp() {
        net = new NeuralNet(12);
        board = new Board();
    }

    @Test
    public void forwardProducesFinitePolicyAndBoundedValue() {
        PolicyValue policyValue = net.forward(board.splitBoardChannels());

        assertNotNull(policyValue);
        assertNotNull(policyValue.policy);
        assertEquals(Move.GLOBAL_MOVE_SPACE_SIZE, policyValue.policy.length);

        double sum = 0.0;
        for (double probability : policyValue.policy) {
            assertFalse(Double.isNaN(probability));
            assertFalse(Double.isInfinite(probability));
            assertTrue(probability >= 0.0);
            sum += probability;
        }

        assertEquals(1.0, sum, 1e-6);
        assertFalse(Double.isNaN(policyValue.value));
        assertFalse(Double.isInfinite(policyValue.value));
        assertTrue(policyValue.value >= -1.0 && policyValue.value <= 1.0);
    }

    @Test
    public void saveAndLoadPreservesForwardOutputExactly() throws IOException {
        PolicyValue before = net.forward(board.splitBoardChannels());

        File tempFile = File.createTempFile("neural-net", ".bin");
        tempFile.deleteOnExit();

        net.save(tempFile.getAbsolutePath());

        NeuralNet restored = new NeuralNet(12);
        restored.load(tempFile.getAbsolutePath());

        PolicyValue after = restored.forward(board.splitBoardChannels());

        assertArrayEquals(before.policy, after.policy, 0.0);
        assertEquals(before.value, after.value, 0.0);
    }

    @Test
    public void trainOnBatchChangesTheNetworkWhileKeepingOutputsValid() {
        TrainingExample example = TrainingTestSupport.exampleFromBoard(board);
        List<TrainingExample> batch = Arrays.asList(example, example, example);
        PolicyValue before = net.forward(example.state);

        new Trainer(net).trainOnBatch(batch);

        PolicyValue after = net.forward(example.state);

        assertNotNull(after);
        assertNotNull(after.policy);
        assertEquals(Move.GLOBAL_MOVE_SPACE_SIZE, after.policy.length);

        double sum = 0.0;
        for (double probability : after.policy) {
            assertFalse(Double.isNaN(probability));
            assertFalse(Double.isInfinite(probability));
            sum += probability;
        }

        assertEquals(1.0, sum, 1e-6);
        assertFalse(Double.isNaN(after.value));
        assertFalse(Double.isInfinite(after.value));
        assertTrue(after.value >= -1.0 && after.value <= 1.0);

        boolean policyChanged = false;
        for (int i = 0; i < before.policy.length; i++) {
            if (Math.abs(before.policy[i] - after.policy[i]) > 1e-12) {
                policyChanged = true;
                break;
            }
        }

        boolean valueChanged = Math.abs(before.value - after.value) > 1e-12;
        assertTrue("Expected training to change the network output", policyChanged || valueChanged);
    }
}