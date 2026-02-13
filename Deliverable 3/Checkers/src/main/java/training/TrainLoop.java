package training;

import mcts.MCTS;
import model.NeuralNet;

import java.util.List;

public class TrainLoop {
    public void main(String[] args) {
        NeuralNet net = new NeuralNet();
        MCTS mcts = new MCTS(net);
        SelfPlay selfPlay = new SelfPlay(mcts);
        ReplayBuffer buffer = new ReplayBuffer();
//        Trainer trainer = new Trainer();

        int iterations = 10;
        int gamesPerIteration = 50;

        for (int i = 0; i < iterations; i++) {
            for (int j = 0; j < gamesPerIteration; j++) {
                List<TrainingExample> examples = selfPlay.playOneGame();
                buffer.addNewTrainingData(examples);
            }

            // call TrainLoop train on batches after sampling X batches
        }
    }
}
