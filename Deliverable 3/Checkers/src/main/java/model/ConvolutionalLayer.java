package model;

import java.util.Random;

public class ConvolutionalLayer {
    private double[][][] postActivationOutput;

   public class BoardSize {
       int width;
       int height;
   }

   public double[][][][] kernels;
   public double[] bias;

   private double[][][][] kernelGradients;
   private double[] biasGradients;
   private int numInAndOut;
   private int channels;
   private int width;
   private int height;

    public ConvolutionalLayer(int numInAndOut, int channels, int width, int height) {
        this.numInAndOut = numInAndOut;
        this.channels = channels;
        this.width = width;
        this.height = height;

        this.kernels = new double[numInAndOut][channels][width][height];

        Random random = new Random();   

        double fan_in = kernels[0].length * kernels[0][0].length * kernels[0][0][0].length;

        double std = Math.sqrt(2 / fan_in);

        for (int i = 0; i < kernels.length; i++) {
            for (int j = 0; j < kernels[i].length; j++) {
                for (int k = 0; k < kernels[i][j].length; k++) {
                    for (int l = 0; l < kernels[i][j][k].length; l++) {
                        kernels[i][j][k][l] = random.nextGaussian() * std;
                    }
                }
            }
        }
    }

    public ConvolutionalLayer(int numInAndOut, int channels, int width, int height, String filePath) {
        this.kernels = new double[numInAndOut][channels][width][height];
    }

    public double[][][] forwardWithActivation(double[][][] board) {
        double[][][] ans = new double[kernels.length][8][8];

        for (int m = 0; m < kernels.length; m++) { // num of kernels/filters per layer
            for (int r = 0; r < board[0].length; r++) { // current row
                for (int c = 0; c < board[0][0].length; c++) { // current column

                    double sum = bias == null ? 0 : bias[m]; // bias applied to every index in the board (per filter)
                    //double sum = 0;

                    for (int channel = 0; channel < board.length; channel++) { // num of channels per board/kernel (must be the same)
                        for (int i = 0; i < kernels[m][channel].length; i++) { // current row (of filter)
                            for (int j = 0; j < kernels[m][channel][i].length; j++) { // current column (of filter)

                                int x = r - 1 + i; // centers filter around the current index of the board
                                int y = c - 1 + j; // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

                                if (x >= 0 && x < board[channel].length && // check horizontal bound
                                        y >= 0 && y < board[channel][0].length) { // check vertical bound
                                    sum += board[channel][x][y] * kernels[m][channel][i][j]; // add product
                                }
                            }
                        }
                    }

                    ans[m][r][c] = Activation.relu(sum);
                }
            }
        }
        this.postActivationOutput = ans;
        return ans;
    }

    public double[][][] forwardNoActivation(double[][][] board) {
        double[][][] ans = new double[kernels.length][8][8];

        for (int m = 0; m < kernels.length; m++) { // num of kernels/filters per layer
            for (int r = 0; r < board[0].length; r++) { // current row
                for (int c = 0; c < board[0][0].length; c++) { // current column

                    double sum = bias[m]; // bias applied to every index in the board (per filter)
//                    double sum = 0;

                    for (int channel = 0; channel < board.length; channel++) { // num of channels per board/kernel (must be the same)
                        for (int i = 0; i < kernels[m][channel].length; i++) { // current row (of filter)
                            for (int j = 0; j < kernels[m][channel][i].length; j++) { // current column (of filter)

                                int x = r - 1 + i; // centers filter around the current index of the board
                                int y = c - 1 + j; // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

                                if (x >= 0 && x < board[channel].length && // check horizontal bound
                                        y >= 0 && y < board[channel][0].length) { // check vertical bound
                                    sum += board[channel][x][y] * kernels[m][channel][i][j]; // add product
                                }
                            }
                        }
                    }
                    ans[m][r][c] = sum;
                }
            }
        }

        return ans;
    }

    public double[][][] backward(double[][][] gradientFromNext, double[][][] outputFromLast) { //outputfromlast is poastActOupt of last layer
        this.kernelGradients = new double[numInAndOut][channels][width][height];
        this.biasGradients = new double[numInAndOut];

        double[][][] gradientPreAct = new double[kernels.length][8][8];
        
        for (int i = 0; i < kernels.length; i++) {
            for (int j = 0; j < width; j++) {
                for (int k = 0; k < height; k++) {
                    if (postActivationOutput[i][j][k] > 0) {
                        gradientPreAct[i][j][k] = gradientFromNext[i][j][k];
                    } else {
                        gradientPreAct[i][j][k] = 0;
                    }
                }
            }
        }

        for (int m = 0; m < numInAndOut; m++) {
            for (int c = 0; c < channels; c++) {
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        double sum = 0;

                        for (int r = 0; r < 8; r++) {
                            for (int t = 0; t < 8; t++) {
                                int x = r - 1 + i;
                                int y = t - 1 + j;
                                if (x >= 0 && x < 8 && y >= 0 && y < 8) {
                                    sum += gradientPreAct[m][r][t] * outputFromLast[c][x][y];
                                }
                            }
                        }

                        kernelGradients[m][c][i][j] = sum;
                    }
                }
            }
        }

        for (int i = 0; i < gradientPreAct.length; i++) {
            double sum = 0;
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 8; k++) {
                    sum += gradientPreAct[i][j][k];
                }
            }
            biasGradients[i] = sum; 
        }

        double[][][] gradientToPass = new double[channels][8][8];

        for (int m = 0; m < channels; m++) {
            for (int c = 0; c < 8; c++) {
                for (int i = 0; i < 8; i++) {
                    double sum = 0;

                    for (int o = 0; o < numInAndOut; o++) {
                        for (int k = 0; k < 8; k++) {
                            for (int z = 0; z < 8; z++) {
                                int x = c + 1 - k;
                                int y = i + 1 - z;
                                if (x >= 0 && x < 8 && y >= 0 && y < 8) {
                                    sum += kernels[o][m][k][z] * gradientPreAct[o][x][y];
                                }
                            }
                        }
                    }

                    gradientToPass[m][c][i] = sum;
                }
            }
        }    

        return gradientToPass;
    }   

    public double[][][] getPostActOutput() {
        return postActivationOutput;
    }

    public double[][][][] getKernelGradients() {
        return kernelGradients;
    }

    public double[] getBiasGradients() {
        return biasGradients;
    }

    public void setPostActivationOutput(double[][][] post) {
        this.postActivationOutput = post;
    }
}
