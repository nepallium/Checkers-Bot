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

    public DenseLayer (int inputSize, int outputSize) {
        this.weights = new double[outputSize][inputSize];
        this.bias = new double[outputSize];
        this.inputSize = inputSize;
        this.outputSize = outputSize;

        Random random = new Random();   

        for (int i = 0; i < outputSize; i++) {
            bias[i] = random.nextGaussian() * Math.sqrt(2.0 / inputSize);

            for (int j = 0; j < inputSize; j++) {
                weights[i][j] = random.nextGaussian() * Math.sqrt(2.0 / inputSize);
            }
        }
    }

    public DenseLayer (int inputSize, int outputSize, String path) throws IOException {

        try (DataInputStream dis = new DataInputStream(new FileInputStream(path))) {

            int fileInputSize = dis.readInt();
            int fileOutputSize = dis.readInt();

            this.weights = new double[fileOutputSize][fileInputSize];
            this. = new double[fileOutputSize];

            for (int i = 0; i < fileOutputSize; i++) {
                for (int j = 0; j < fileInputSize; j++) {
                    weights[i][j] = dis.readDouble();
                }
            }
            
            for (int i = 0; i < fileOutputSize; i++) {
                bias[i] = dis.readDouble();
            }
        }
    }

    public double ReLu(double x) {
        return Math.max(0, x);
    }

    public double[] softmax(double[] input) {

        double eSum = 0;
        for (int i = 0; i < outputSize; i++) {
            eSum += Math.exp(input[i]);
        }

        for (int i = 0; i < outputSize; i++) {
            input[i] = Math.exp(input[i]) / eSum;
        }

        return input;
    }


    public double[] forward(double[] inputVector) {
        double[] outputVect = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            for(int j = 0; j < inputSize; j++) {
                outputVect[i] += inputVector[j] * weights[i][j];
            }
            outputVect[i] = ReLu(outputVect[i] + bias[i]);
        }

        return outputVect;
    }

    public double[] policyOutput(double[] inputVector) {
        double[] outputVect = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            for(int j = 0; j < inputSize; j++) {
                outputVect[i] += inputVector[j] * weights[i][j];
            }
            outputVect[i] = outputVect[i] + bias[i];
        }

        return softmax(outputVect);
    }

    public double valueOutput(double[] inputVector) {
        
    }
}
