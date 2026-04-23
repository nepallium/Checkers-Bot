package training;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Stores multiple TrainingExamples and selects random minibatches
 */
public class ReplayBuffer {
    public List<TrainingExample> dataList;
    private final int MAXSIZE = 10000; // max size for dataList
    private final Random random = new Random();

    public ReplayBuffer() {
        this.dataList = new ArrayList<>();
    }


    public void addNewTrainingData(List<TrainingExample> newExamples) {
        if (newExamples == null) {
            return;
        }

        dataList.addAll(newExamples);

        // clear old examples once MAXSIZE exceeded
        if (dataList.size() > MAXSIZE) {
            int amtToRemove = dataList.size() - MAXSIZE;

            dataList.subList(0, amtToRemove).clear();
        }
    }

    public List<TrainingExample> sample(int batchSize) {
        if (batchSize <= 0 || dataList.isEmpty()) {
            return new ArrayList<>();
        }

        List<TrainingExample> samples = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            int randIdx = random.nextInt(dataList.size());
            samples.add(dataList.get(randIdx));
        }

        return samples;
    }
}
