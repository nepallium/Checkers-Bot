package model;

import game.Board;

import java.util.Arrays;

public class NeuralNet {

    public static int KERNEL_SIZE = 3;
    public static int BOARD_SIZE = 8;
    public static int CHANNELS = 3 * 4; //3 boards x 4 piece types

    int numFeatureMaps;
    int numActions;

    ConvolutionalLayer cl1;
    ConvolutionalLayer cl2;
    ConvolutionalLayer cl3;

    DenseLayer fc1;
    DenseLayer fc2;
    DenseLayer policyLayer;
    DenseLayer valueLayer;

    public NeuralNet() {

    }

    /**
     * Neural net constructor
     * @param numFeatureMaps num of feature maps / kernels
     * @param numActions global action space length; all possible moves
     */
    public NeuralNet(int numFeatureMaps, int numActions) {
        this.numFeatureMaps = numFeatureMaps;
        this.numActions = numActions;

        this.cl1 = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);
        this.cl2 = new ConvolutionalLayer(numFeatureMaps, numFeatureMaps, KERNEL_SIZE, KERNEL_SIZE);
        this.cl3 = new ConvolutionalLayer(numFeatureMaps, numFeatureMaps, KERNEL_SIZE, KERNEL_SIZE);

        int flattenedSize = numFeatureMaps * BOARD_SIZE * BOARD_SIZE;
        // TODO: outputSize currently arbitrary
        this.fc1 = new DenseLayer(flattenedSize, 256);
        this.fc2 = new DenseLayer(256, 128);

        this.policyLayer = new DenseLayer(128, numActions);
        this.valueLayer = new DenseLayer(128, 1);
    }

    /**
     * Applies forward pass to 8x8 checkers board
     * Goes through CNN first, then flattens the feature maps, then feeds the latter through DenseLayers
     *
     * @param board the current board state
     * @return the policy and value heads
     */
    public PolicyValue forward(double[][][] board) {
        // CONVOLUTIONAL LAYERS
        // numFeatureMaps (m) * 8 * 8, featureMaps[m][r][c] == how strongly pattern m is present around square (r, c)
        double[][][] featureMaps1 = cl1.forwardWithActivation(board);
        double[][][] featureMaps2 = cl2.forwardWithActivation(featureMaps1);
        double[][][] featureMaps3 = cl3.forwardWithActivation(featureMaps2);

        double[] flattenedMaps = flatten(featureMaps3);


        // FULLY CONNECTED LAYERS
        double[] fcOutput1 = fc1.forward(flattenedMaps);
        double[] fcOutput2 = fc2.forward(fcOutput1);

        return new PolicyValue(policyLayer.policyOutput(fcOutput2), valueLayer.valueOutput(fcOutput2));
    }

    public double[] flatten(double[][][] nestedArray) {
        if (nestedArray == null) {
            return null;
        }

        return Arrays.stream(nestedArray).flatMap(Arrays::stream).flatMapToDouble(Arrays::stream).toArray();
    }
}
