package main;

import game.Board;
import game.Coordinate;
import game.Move;
import model.ConvolutionalLayer;
import model.DenseLayer;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Board b = new Board();
        System.out.println(b);
        List<Move> globalActionSpace =b.getGlobalActionSpace(true);
        globalActionSpace.forEach(System.out::println);
        /*
        long startTime = System.nanoTime();

        System.out.println("START TIME: " + startTime / 1000000);

//        DenseLayer layer = new DenseLayer(64, 768);
//
//        double[] mockBoard = new double[128];
//
//        for (int i = 0; i < mockBoard.length; i++) {
//            mockBoard[i] = Math.random() * 284;
//        }
//
//        double[] ans = layer.forward(mockBoard);
//
//        System.out.println(Arrays.toString(ans));

        ConvolutionalLayer layer = new ConvolutionalLayer(64, 4, 3, 3);

        int[][][] mockBoard = new int[4][8][8];

        for (int i = 0; i < mockBoard.length; i++) {
            for (int j = 0; j < mockBoard[i].length; j++) {
                for (int k = 0; k < mockBoard[i][j].length; k++) {
                    mockBoard[i][j][k] = ((int) (Math.random() * 5)) - 2;
                }
            }
        }

        printConvolution(layer, mockBoard);

        long endTime = System.nanoTime();

        System.out.println("Time taken: " + ((endTime - startTime) / 1000000));

         */
    }

    public static void printConvolution(ConvolutionalLayer layer, double[][][] board) {
        double[][][] ans = layer.forward(board);

        for (int i = 0; i < ans.length; i++) {
            for (int j = 0; j < ans[i].length; j++) {
                System.out.println(Arrays.toString(ans[i][j]));
//                for (int k = 0; k < ans[i][j].length; k++) {
//
//                }
            }
        }
    }
}
