package model;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class DenseLayer {
    private double[][] weights;
    private double[] bias;
    private int inputSize;
    private int outputSize;
    private double[] postActivationOutput;

    public DenseLayer (int inputSize, int outputSize) {
        this.weights = new double[outputSize][inputSize];
        this.bias = new double[outputSize];
        this.inputSize = inputSize;
        this.outputSize = outputSize;

        Random random = new Random();   

        for (int i = 0; i < outputSize; i++) {
            bias[i] = 0;

            for (int j = 0; j < inputSize; j++) {
                weights[i][j] = random.nextGaussian() * Math.sqrt(2.0 / inputSize);
            }
        }
    }

    public DenseLayer (int inputSize, int outputSize, String path) throws IOException {

        try (DataInputStream dis = new DataInputStream(new FileInputStream(path))) {

            int fileInputSize = dis.readInt();
            int fileOutputSize = dis.readInt();

            this.inputSize = fileInputSize;
            this.outputSize = fileOutputSize;

            this.weights = new double[fileOutputSize][fileInputSize];

            for (int i = 0; i < fileOutputSize; i++) {
                this.bias[i] = 0;
                for (int j = 0; j < fileInputSize; j++) {
                    weights[i][j] = dis.readDouble();
                }
            }
        }
    }

    public double[] forward(double[] inputVector) {
        double[] outputVect = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            for(int j = 0; j < inputSize; j++) {
                outputVect[i] += inputVector[j] * weights[i][j];
            }
            outputVect[i] = Activation.relu(outputVect[i] + bias[i]);
        }
        this.postActivationOutput = outputVect;

        return postActivationOutput;
    }

    public double[] policyOutput(double[] inputVector) {
        double[] outputVect = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            for(int j = 0; j < inputSize; j++) {
                outputVect[i] += inputVector[j] * weights[i][j];
            }
            outputVect[i] = outputVect[i] + bias[i];
        }

        this.postActivationOutput = softmax(outputVect);

        return postActivationOutput;
    }

    public double valueOutput(double[] inputVector) {
        double[] outputVect = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            for(int j = 0; j < inputSize; j++) {
                outputVect[i] += inputVector[j] * weights[i][j];
            }
            outputVect[i] = outputVect[i] + bias[i];
        }

        for (int i = 0; i < outputVect.length; i++) {
            this.postActivationOutput[i] = Math.tanh(outputVect[i]);
        }
        return postActivationOutput[0];
    }

    public double[] getPostActOutput() {
        return this.postActivationOutput;
    }
}
