package main;

import game.Board;
import game.Move;
import model.ConvolutionalLayer;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

//        System.out.println("Hello world!");

        ConvolutionalLayer layer = new ConvolutionalLayer(32, 4, 3, 3);

//        System.out.println("DD");

        Board b = new Board();
        for (int i = 0; i < 8; i++) {
            String output = "";
            for (int j = 0; j < 8; j++) {
                output += b.cells[b.idx(i, j)];
            }

            System.out.println(output);
        }

    }
}
