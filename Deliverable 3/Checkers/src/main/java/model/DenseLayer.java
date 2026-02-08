//package model;
//
//import java.io.*;
//import java.util.Random;
//
//public class DenseLayer {
//    private double[][] weights;
//
//    public DenseLayer (int inputSize, int OutputSize) {
//        this.weights = new double[inputSize][OutputSize];
//
//        Random random = new Random();
//
//        for (int i = 0; i < inputSize; i++) {
//            for (int j = 0; j < OutputSize; j++) {
//                weights[i][j] = random.nextGaussian() * Math.sqrt(2.0 / inputSize);
//            }
//        }
//    }
//
//    public DenseLayer (int inputSize, int OutputSize, String path) {
//        this.weights = new double[inputSize][OutputSize];
//
//        try (DataInputStream dis = new DataInputStream(new FileInputStream(path))) {
//
//            int inputSize = dis.readInt();
//            int outputSize = dis.readInt();
//
//            weights = new double[inputSize][outputSize];
//
//            for (int i = 0; i < inputSize; i++) {
//                for (int j = 0; j < outputSize; j++) {
//                    weights[i][j] = dis.readDouble();
//                }
//            }
//        }
//    }
//
//    public int ReLu(int x) {
//        return Math.max(0, x);
//    }
//}
