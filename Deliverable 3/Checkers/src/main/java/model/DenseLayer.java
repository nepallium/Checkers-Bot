package model;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import lombok.Getter;

public class DenseLayer {
    public double[][] weights;
    public double[] bias;
    private int inputSize;
    @Getter
    private int outputSize;
    private double[] postActivationOutput;

    private double[][] weightGradients;
    private double[] biasGradients;

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
            this.bias = new double[fileOutputSize];

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

        this.postActivationOutput = Activation.softmax(outputVect);

        return postActivationOutput;
    }

    public double valueOutput(double[] inputVector) {
        double[] outputVect = new double[outputSize];
        this.postActivationOutput = new double[outputSize];
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


    public double[] backward(double[] gradientFromNext, double[] outputFromLast) { //outputfromlast is poastActOupt of last layer, aka inputToThisLayer
        this.weightGradients = new double[outputSize][inputSize];
        this.biasGradients = new double[outputSize];

        double[] gradientPreAct = new double[outputSize];
        
        for (int i = 0; i < gradientPreAct.length; i++) {
            if (postActivationOutput[i] > 0) {
                gradientPreAct[i] = gradientFromNext[i];
            } else {
                gradientPreAct[i] = 0;
            }
        }
        
        for (int i = 0; i < weightGradients.length; i ++) {
            for (int j = 0; j < weightGradients[i].length; j++) {
                this.weightGradients[i][j] = gradientPreAct[i] * outputFromLast[j];
            }
        }

        for (int i = 0; i < biasGradients.length; i++) {
            this.biasGradients[i] = gradientPreAct[i];
        }

        double[] gradientToPass = new double[inputSize];
        for (int i = 0; i < gradientToPass.length; i ++) {
            for (int j  = 0; j < weights[i].length; j++) {
                gradientToPass[i] += weights[j][i] * gradientPreAct[j];
            }
        }
        return gradientToPass;
    }   

    public double[][] getWeightGradients() {
        return weightGradients;
    }

    public int getOutputSize() {
        return outputSize;
    }

    public double[] getBiasGradient() {
        return biasGradients;
    }
}
