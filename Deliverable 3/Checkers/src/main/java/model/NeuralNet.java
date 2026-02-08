package model;

import java.util.Arrays;

public class NeuralNet {

//    ConvolutionalLayer cnn = new ConvolutionalLayer()

    /**
     * Connects the CNN and DenseLayer
     */
    public void connect() {

    }

    public double[] flatten(double[][][] nestedArray) {
        return Arrays.stream(nestedArray).flatMap(Arrays::stream).flatMapToDouble(Arrays::stream).toArray();
    }

    public double[] flatten(double[][] nestedArray) {
        return Arrays.stream(nestedArray).flatMapToDouble(Arrays::stream).toArray();
    }
}
