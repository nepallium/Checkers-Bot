package training;

import mcts.MCTS;
import model.NeuralNet;

import java.util.List;

public class TrainLoop {
    private final int iterations = 10;
    private final int gamesPerIteration = 10;
    private final int trainingStepsPerIteration = 10;
    private final int batchSize = 10;

    public void main(String[] args) {
        // TODO actual meaningful args to NN
        NeuralNet net = new NeuralNet(10, 10);
        MCTS mcts = new MCTS(net);
        SelfPlay selfPlay = new SelfPlay(mcts);
        ReplayBuffer buffer = new ReplayBuffer();
        Trainer trainer = new Trainer(net);


        for (int i = 0; i < iterations; i++) {
            for (int j = 0; j < gamesPerIteration; j++) {
                List<TrainingExample> examples = selfPlay.playOneGame();
                buffer.addNewTrainingData(examples);
            }

            for (int j = 0; j < trainingStepsPerIteration; j++) {
                List<TrainingExample> miniBatch = buffer.sample(batchSize);
                trainer.trainOnBatch(miniBatch);
            }

            // TODO write net checkpoint to file (?) and print current stats
        }
    }
}
