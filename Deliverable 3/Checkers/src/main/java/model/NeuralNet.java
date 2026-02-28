package model;

import game.Board;

import java.util.Arrays;

public class NeuralNet {

    public static int KERNEL_SIZE = 3;
    public static int BOARD_SIZE = 8;
    public static int CHANNELS = 3 * 4; //3 boards x 4 piece types

    int numFeatureMaps;
    int numActions;

    //to change
    ConvolutionalLayer firstLayer;
    ResidualBlock rb1;
    ResidualBlock rb2;
    ResidualBlock rb3;
    ResidualBlock rb4;
    ResidualBlock rb5;

    ConvolutionalLayer cl1;
    ConvolutionalLayer cl2;
    ConvolutionalLayer cl3;
    ConvolutionalLayer cl4;
    ConvolutionalLayer cl5;
    ConvolutionalLayer cl6;
    ConvolutionalLayer cl7;
    ConvolutionalLayer cl8;
    ConvolutionalLayer cl9;
    ConvolutionalLayer cl10;


    //to change
    DenseLayer fc1;
    DenseLayer fc2;
    DenseLayer policyLayer;
    DenseLayer valueLayer;

    private MSEloss lossFunction;
    private double learningRate = 0.001;
    private double weightDecay = 0.0001;

    /**
     * Neural net constructor
     * @param numFeatureMaps num of feature maps / kernels
     * @param numActions global action space length; all possible moves
     */
    public NeuralNet(int numFeatureMaps, int numActions) {
        this.numFeatureMaps = numFeatureMaps;
        this.numActions = numActions;

        //to change
        this.firstLayer = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);

        this.cl1 = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);
        this.cl2 = new ConvolutionalLayer(numFeatureMaps, numFeatureMaps, KERNEL_SIZE, KERNEL_SIZE);
        this.cl3 = new ConvolutionalLayer(numFeatureMaps, numFeatureMaps, KERNEL_SIZE, KERNEL_SIZE);
        this.cl4 = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);
        this.cl5 = new ConvolutionalLayer(numFeatureMaps, numFeatureMaps, KERNEL_SIZE, KERNEL_SIZE);
        this.cl6 = new ConvolutionalLayer(numFeatureMaps, numFeatureMaps, KERNEL_SIZE, KERNEL_SIZE);
        this.cl7 = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);
        this.cl8 = new ConvolutionalLayer(numFeatureMaps, numFeatureMaps, KERNEL_SIZE, KERNEL_SIZE);
        this.cl9 = new ConvolutionalLayer(numFeatureMaps, numFeatureMaps, KERNEL_SIZE, KERNEL_SIZE);
        this.cl10 = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);

        this.rb1 = new ResidualBlock(cl1, cl2);
        this.rb2 = new ResidualBlock(cl3, cl4);
        this.rb3 = new ResidualBlock(cl5, cl6);
        this.rb4 = new ResidualBlock(cl7, cl8);
        this.rb5 = new ResidualBlock(cl9, cl10);


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
     * @param boardObj the current board state
     * @return the policy and value heads
     */
    public PolicyValue forward(Board boardObj) {
        //have to redo this method with new architecture
        double[][][] board = boardObj.splitBoardChannels();

        // CONVOLUTIONAL LAYERS
        // numFeatureMaps (m) * 8 * 8, featureMaps[m][r][c] == how strongly pattern m is present around square (r, c)
        double[][][] featureMaps1 = firstLayer.forwardWithActivation(board);
        double[][][] featureMaps2 = rb1.forward(featureMaps1);
        double[][][] featureMaps3 = rb2.forward(featureMaps2);
        double[][][] featureMaps4 = rb3.forward(featureMaps3);
        double[][][] featureMaps5 = rb4.forward(featureMaps4);
        double[][][] featureMaps6 = rb5.forward(featureMaps5);

        double[] flattenedMaps = flatten(featureMaps5);


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


    public void updateWeights() {
        //TO DO
    }
}
