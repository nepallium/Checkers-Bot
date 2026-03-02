package model;

import java.util.Arrays;

import lombok.Getter;

public class NeuralNet {

    public static final int KERNEL_SIZE = 3;
    public static final int BOARD_SIZE = 8;
    public static final int CHANNELS = 3 * 4; //3 boards x 4 piece types
    public static final int NUM_ACTIONS = 1666; // length of GlobalMoveSpace.csv

    ConvolutionalLayer firstLayer;
    ResidualBlock rb1;
    ResidualBlock rb2;
    ResidualBlock rb3;
    ResidualBlock rb4;
    ResidualBlock rb5;
    ResidualBlock[] rbs;

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
    
    double[] flattenedMaps;

    @Getter
    DenseLayer fc1;
    @Getter
    DenseLayer fc2;
    @Getter
    DenseLayer policyLayer;
    @Getter
    DenseLayer valueLayer;

    private double learningRate = 0.001;
    private double weightDecay = 0.0001;

    /**
     * Neural net constructor
     * @param numFeatureMaps num of feature maps / kernels
     */
    public NeuralNet(int numFeatureMaps) {
        this.firstLayer = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);

        this.cl1 = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);
        this.cl2 = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);
        this.cl3 = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);
        this.cl4 = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);
        this.cl5 = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);
        this.cl6 = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);
        this.cl7 = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);
        this.cl8 = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);
        this.cl9 = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);
        this.cl10 = new ConvolutionalLayer(numFeatureMaps, CHANNELS, KERNEL_SIZE, KERNEL_SIZE);

        this.rb1 = new ResidualBlock(cl1, cl2);
        this.rb2 = new ResidualBlock(cl3, cl4);
        this.rb3 = new ResidualBlock(cl5, cl6);
        this.rb4 = new ResidualBlock(cl7, cl8);
        this.rb5 = new ResidualBlock(cl9, cl10);
        this.rbs = new ResidualBlock[]{rb1, rb2, rb3, rb4, rb5};


        int flattenedSize = numFeatureMaps * BOARD_SIZE * BOARD_SIZE;

        // TODO: outputSize currently arbitrary
        this.fc1 = new DenseLayer(flattenedSize, 256);
        this.fc2 = new DenseLayer(256, 128);

        this.policyLayer = new DenseLayer(128, NUM_ACTIONS);
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
        double[][][] currFeatureMap = firstLayer.forwardWithActivation(board);
        for (ResidualBlock rb : rbs) {
            currFeatureMap = rb.forward(currFeatureMap);
        }

        this.flattenedMaps = flatten(currFeatureMap);


        // FULLY CONNECTED LAYERS
        double[] fcOutput1 = fc1.forward(flattenedMaps);
        double[] fcOutput2 = fc2.forward(fcOutput1);

        return new PolicyValue(policyLayer.policyOutput(fcOutput2), valueLayer.valueOutput(fcOutput2));
    }

    /**
     * Backpropagates 
     * 
     * @param dLoss_dPolicy gradient from policy wrt its loss
     * @param dLoss_dValue gradient from value wrt its loss
     */
    public void backward(double[] dLoss_dPolicy, double dLoss_dValue) {
        double[] gradFromPolicyHead = policyLayer.backward(dLoss_dPolicy, fc2.getPostActOutput());

        // convert value gradient (scalar) into array
        double[] valueGradArray = new double[]{dLoss_dValue};
        double[] gradFromValueHead = valueLayer.backward(valueGradArray, fc2.getPostActOutput());

        double[] gradToFc2 = new double[fc2.getOutputSize()]; // loss w.r.t fc2 output
        for (int i = 0; i < gradToFc2.length; i++) {
            gradToFc2[i] = gradFromPolicyHead[i] + gradFromValueHead[i];
        }

        double[] gradToFc1 = fc2.backward(gradToFc2, fc1.getPostActOutput());
        double[] gradToFlattened = fc1.backward(gradToFc1, flattenedMaps);



        // TODO convert 1D gradient into [numFeatureMaps][8][8] aka a convolutional layer, call it gradToFeatureMaps
        // then backprop through rbs array
        // then backprop through first conv layer
        // then updateWeights()
    }


    public void updateWeights() {
        // Update first conv layer
        updateConvLayer(firstLayer);

        // Update residual blocks
        for (ResidualBlock rb : rbs) {
            updateConvLayer(rb.getLayer1());
            updateConvLayer(rb.getLayer2());
        }

        // Update dense layers
        updateDenseLayer(fc1);
        updateDenseLayer(fc2);
        updateDenseLayer(policyLayer);
        updateDenseLayer(valueLayer);
    }

    public void updateConvLayer(ConvolutionalLayer layer) {
        double[][][][] kernelGrads = layer.getKernelGradients();
        double[] biasGrads = layer.getBiasGradients();

        for (int i = 0; i < layer.kernels.length; i++) {
            for (int j = 0; j < layer.kernels[i].length; j++) {
                for (int k = 0; k < layer.kernels[i][j].length; k++) {
                    for (int l = 0; l < layer.kernels[i][j][k].length; l++) {
                        layer.kernels[i][j][k][l] -= learningRate * (kernelGrads[i][j][k][l] + weightDecay * layer.kernels[i][j][k][l]);
                    } 
                }    
            }   
            layer.bias[i] -= learningRate * (biasGrads[i]);
        }
    }

    public void updateDenseLayer(DenseLayer layer) {
        double[][] weightGradients = layer.getWeightGradients();
        double[] biasGrads = layer.getBiasGradient();

        for (int i = 0; i < weightGradients.length; i ++) {
            for (int j = 0; j < weightGradients[i].length; j++) {
                layer.weights[i][j] -= learningRate * (weightGradients[i][j] + weightDecay * layer.weights[i][j]);
            }
            layer.bias[i] -= learningRate * (biasGrads[i]);
        }
    }
    
    /**
     * Flattens a 3D tensor into a 1D array
     * @param nestedArray the 3D tensor
     * @return the 1D flattened array
     */
    public double[] flatten(double[][][] nestedArray) {
        if (nestedArray == null) {
            return null;
        }

        return Arrays.stream(nestedArray).flatMap(Arrays::stream).flatMapToDouble(Arrays::stream).toArray();
    }

    /**
     * Converts a flattened array into an unflattened (from dense layer to convolutional layer format)
     * @param flattenedMaps the flattened array
     * @return an unflatted array of feature maps and channels
     */
    public double[][][] unFlatten(double[] flattenedMaps) {
        double finalArray[][][] = new double[firstLayer.kernels.length][BOARD_SIZE][BOARD_SIZE];

        for (int i = 0; i < firstLayer.kernels.length; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                for (int k = 0; k < BOARD_SIZE; k++) {
                    finalArray[i][j][k] = flattenedMaps[i * BOARD_SIZE * BOARD_SIZE + j * BOARD_SIZE + k];
                }
            }
        }

        return finalArray;
    }
}
