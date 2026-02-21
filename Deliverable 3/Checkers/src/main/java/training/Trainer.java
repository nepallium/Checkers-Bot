package training;

import model.MSEloss;
import model.NeuralNet;
import model.PolicyValue;

import java.util.List;

/**
 * Takes mini batches from replay buffer to update network through backprop
 */
public class Trainer {
    private NeuralNet net;
    private MSEloss mse;

    public Trainer(NeuralNet net) {
        this.net = net;
        mse = new MSEloss();
    }

    public void trainOnBatch(List<TrainingExample> batch) {
        double totalPolicyLoss = 0.0, totalValueLoss = 0.0;

        for (TrainingExample ex : batch) {
            PolicyValue predictedPV = net.forward(ex.state);


            // VALUE loss and gradient
            totalValueLoss += mse.forward(predictedPV.value, ex.z);
            double dLoss_dValue = mse.backward();


            // POLICY loss and gradient
//            totalPolicyLoss += mse.forward(predictedPV.policy, ex.pi);
//            double[] gradient = mse.backward();


            // then net.backward w/ the gradients
        }

    }

}
