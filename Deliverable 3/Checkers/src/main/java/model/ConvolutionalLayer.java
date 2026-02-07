package model;

import java.util.Random;

public class ConvolutionalLayer {
   public class BoardSize {
       int width;
       int height;
   }

    private double[][][][] kernels;

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

    public ConvolutionalLayer(int channels, BoardSize board, int numOut, String filePath) {
        this.kernels = new double[numOut][channels][board.width][board.height];
    }
    
}
