package main;

import game.Action;
import game.Board;
import game.Coordinate;
import game.Move;
import model.ConvolutionalLayer;
import model.NeuralNet;
import model.PolicyValue;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        // ALEX
        //neuralNetTest();


        // DANIEL
        Board b = new Board();
        //Test playing
        for (int i = 0; i < 100; i++) {
            Map<Integer, Move> ms = b.getBoardMoveSpace();
            if (ms == null || ms.isEmpty()) {
                break;
            }
            if (!b.applyMove(ms.get((int)(Math.random() * ms.size())))) {
                break;
            };
        }
        System.out.println(b);
        for (int i = 0; i < 100; i++) {
            System.out.println(b.getMoveLog());
            if(!b.undoLastMove()) {
                System.out.println("could not undo");
                break;
            };
        }
        System.out.println(b);

        //List<Move> globalActionSpace = b.getGlobalMoveSpace(true);
        //globalActionSpace.forEach(System.out::println);


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

    private static void neuralNetTest() {
        Board b = new Board();
        b.cells[2][3] = 1; // ally man
        b.cells[5][4] = -1; // op man
        b.cells[0][7] = 2; // ally king

        NeuralNet net = new NeuralNet(8, 16);

        PolicyValue pv = net.forward(b);

        System.out.println("Policy:");
        System.out.println(Arrays.toString(pv.policy));

        System.out.println("\nValue:");
        System.out.println(pv.value);
    }

    public static void printConvolution(ConvolutionalLayer layer, double[][][] board) {
        double[][][] ans = layer.forwardWithActivation(board);

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
