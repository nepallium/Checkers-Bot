package main;

import model.ConvolutionalLayer;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        System.out.println("Hello world!");

        ConvolutionalLayer layer = new ConvolutionalLayer(32, 4, 3, 3);

        for (int i = 0; i < layer.kernels.length; i++) {
            for (int j = 0; j < layer.kernels[i].length; j++) {
                for (int k = 0; k < layer.kernels[i][j].length; k++) {
                    System.out.println(Arrays.toString(layer.kernels[i][j][k]));
                }
            }
        }
    }
}
