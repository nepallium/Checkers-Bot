package training;

import java.util.List;

import model.CrossEntropyLoss;
import model.MSEloss;
import model.NeuralNet;
import model.PolicyValue;

/**
 * Takes mini batches from replay buffer to update network through backprop
 */
public class Trainer {
    private NeuralNet net;
    private MSEloss mse;
    private CrossEntropyLoss ce;

    public Trainer(NeuralNet net) {
        this.net = net;
        mse = new MSEloss();
        ce = new CrossEntropyLoss();
    }

    public void trainOnBatch(List<TrainingExample> batch) {
        double totalPolicyLoss = 0.0, totalValueLoss = 0.0;

        // TODO mini-batch gradients instead of stochastic gradient descent
        // double[] accumulatedGradients

        for (TrainingExample ex : batch) {
            PolicyValue predictedPV = net.forward(ex.state);

            // LOSS derivatives
            // POLICY loss and gradient
            totalPolicyLoss += ce.forward(predictedPV.policy, ex.pi);
            double[] dLoss_dPolicy = ce.backward();

            
            // VALUE loss and gradient
            totalValueLoss += mse.forward(predictedPV.value, ex.z);
            double dLoss_dValue = mse.backward();



            // Propagate loss derivatives (gradients) backwards
            
        }

    }

}
