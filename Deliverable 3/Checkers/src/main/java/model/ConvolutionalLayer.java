package model;

import java.util.Random;

public class ConvolutionalLayer {
   public class BoardSize {
       int width;
       int height;
   }

   public double[][][][] kernels;
   private double[] bias;

    public ConvolutionalLayer(int numInAndOut, int channels, int width, int height) {
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

    public double[][][] forward(int[][][] board) {
        double[][][] ans = new double[kernels.length][8][8];

        for (int m = 0; m < kernels.length; m++) { // num of kernels/filters per layer
            for (int r = 0; r < board[0].length; r++) { // current row
                for (int c = 0; c < board[0][0].length; c++) { // current column

                    double sum = bias[m]; // bias applied to every index in the board (per filter)

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


        return ans;
    }
}
