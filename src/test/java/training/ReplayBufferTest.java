package training;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class ReplayBufferTest {

    private ReplayBuffer replayBuffer;

    @Before
    public void setUp() {
        replayBuffer = new ReplayBuffer();
    }

    @Test
    public void addNewTrainingDataIgnoresNullInput() {
        replayBuffer.addNewTrainingData(null);

        assertTrue(replayBuffer.dataList.isEmpty());
    }

    @Test
    public void sampleReturnsRequestedSizeAndStoredElements() {
        TrainingExample first = TrainingTestSupport.exampleWithValue(1.0);
        TrainingExample second = TrainingTestSupport.exampleWithValue(-1.0);
        replayBuffer.addNewTrainingData(Arrays.asList(first, second));

        List<TrainingExample> sample = replayBuffer.sample(8);

        assertEquals(8, sample.size());
        for (TrainingExample example : sample) {
            assertTrue(example == first || example == second);
        }
    }

    @Test
    public void addNewTrainingDataTrimsOldestEntriesWhenBufferExceedsMaximumSize() {
        List<TrainingExample> examples = new ArrayList<>();
        for (int i = 0; i < 10005; i++) {
            examples.add(TrainingTestSupport.exampleWithValue(i));
        }

        replayBuffer.addNewTrainingData(examples);

        assertEquals(10000, replayBuffer.dataList.size());
        assertEquals(5.0, replayBuffer.dataList.get(0).z, 0.0);
        assertEquals(10004.0, replayBuffer.dataList.get(replayBuffer.dataList.size() - 1).z, 0.0);
        assertFalse(replayBuffer.dataList.contains(examples.get(0)));
    }
}