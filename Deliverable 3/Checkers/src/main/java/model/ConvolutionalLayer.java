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

        for (int i = 0; i < kernels.length; i++) {
            for (int j = 0; j < kernels[i].length; j++) {
                for (int k = 0; k < kernels[i][j].length; k++) {
                    for (int l = 0; l < kernels[i][j][k].length; l++) {
                        kernels[i][j][k][l] = random.nextGaussian();
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

        for (int channel = 0; channel < board.length; channel++) {
            for (int w = 0; w < board[channel].length; w++) {
                for (int h = 0; h < board[channel][w].length; h++) {

                    for (int m = 0; m < kernels.length; m++) {
                        for (int i = 0; i < kernels[m][channel].length; i++) {
                            for (int j = 0; j < kernels[m][channel][i].length; j++) {
                                ans[m][w][h] += ((w - 1 + i) >= 0 && (w - 1 + i) < board[channel].length
                                && (h - 1 + j ) >= 0 && (h - 1 + j) < board[channel][w].length)
                                        ? board[channel][w - 1 + i][h - 1 + j] * kernels[m][channel][i][j]
                                        : 0;
                            }
                        }
//                        ans[m][w][h] += bias[m];
                    }

                }
            }
        }

        return ans;
    }
}
