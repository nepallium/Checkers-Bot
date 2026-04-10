package main;

import game.*;
import mcts.MCTS;
import model.ConvolutionalLayer;
import model.NeuralNet;
import model.PolicyValue;
import org.w3c.dom.ls.LSOutput;
import training.SelfPlay;
import training.Trainer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        Move.init();

        // ALEX
        //neuralNetTest();
    	System.out.println("Working.....");
        NeuralNet net = new NeuralNet(12);

//        net.load("src/main/data/checkersModel.bin");

        Trainer trainer = new Trainer(net);
        SelfPlay selfPlay = new SelfPlay(net);

        System.out.println("Checkpoint 1");

        System.out.println("Checkpoint 2");

        for (int i = 0; i < 1000; i++) {
            try {
                trainer.trainOnBatch(selfPlay.playOneGame());
                if (i % 1 == 0) {
                    System.out.println("Games played: " + i + " / 100 000");
                }
                if (i % 5 == 0) {
                    net.save("src/main/data/checkersModel.bin");
                    System.out.println("----- Saved a model -----");
                }
            } catch (Exception err) {
                System.out.println("An error occured in training + self play for this iteration: " + err.getMessage());
                System.out.println("Printing Stack Trace...");
                System.err.println(Arrays.toString(err.getStackTrace()));
            }
        }

        net.save("src/main/data/checkersModel.bin");




        // DANIEL
//        Board b = new Board();
//        Move.init();
//        System.out.println(Arrays.toString(Move.GLOBAL_MOVE_SPACE));
//        System.out.println(Move.GLOBAL_MOVE_SPACE.length);
        /*
        //Test playing
        for (int i = 0; i < 100; i++) {
            List<Move> ms = b.getBoardMoveSpace();
            if (ms == null || ms.isEmpty()) {
                break;
            }
            if (!b.applyMove(ms.get((int)(Math.random() * ms.size())))) {
                break;
            };
        }
        System.out.println(b);
        for (int i = 0; i < 100; i++) {
            if(!b.undoLastMove()) {
                break;
            };
        }
        System.out.println(b);

        //List<Move> globalActionSpace = b.getGlobalMoveSpace(true);
        //globalActionSpace.forEach(System.out::println);

*/
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

    /**
     * [not used in application]
     * Call to set the global move space into the CSV file
     */
    private static void logGlobalMoveSpace() {
        Map<Coordinate, Set<Move>> globalMoveSpace = new HashMap<>();

        for (int rowIdx = 0; rowIdx < 8; rowIdx++) {
            for (int xIdx = (rowIdx % 2 == 0 ? 0 : 1); xIdx < 8; xIdx += 2) {
                Set<Move> localMoveSpace = getGlobalMoveSpaceFrom(xIdx, rowIdx);
                globalMoveSpace.put(new Coordinate(xIdx, rowIdx), localMoveSpace);
            }
        }
        int moveCounter = 0;
        File file = new File(Move.GLOBAL_MOVE_SPACE_FILE_PATH);
        try (FileWriter writer = new FileWriter(file)) {
            for (Coordinate coordinate : globalMoveSpace.keySet()) {
                for (Move move : globalMoveSpace.get(coordinate)) {
                    String line = "";
                    for (Action action : move.getActions()) {
                        Coordinate start = action.getStart();
                        Coordinate end = action.getDestination();
                        line += String.format("%s,%s,%s,%s,", start.getX(), start.getY(), end.getX(), end.getY());
                    }
                    writer.write(line + "\n");
                    moveCounter++;
                }
            }
        } catch (IOException e) {

        }
        System.out.printf("Overwrite of GlobalMoveSpace.csv - global move space now has %s entries", moveCounter);
    }


    /**
     * [not used in application]
     * Helper function for logGlobalMoveSpace()
     * @param x x coordinate
     * @param y y coordinate
     * @return set of all possible moves for that coordinate
     */
    public static Set<Move> getGlobalMoveSpaceFrom(int x, int y) {
        Set<Move> results = new HashSet<>();

        int[][] dirs = {
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        // simple king moves (1-step diagonals)
        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];

            if (isInBounds(nx, ny)) {
                List<Action> list = new ArrayList<>();
                list.add(new Action(new Coordinate(x, y),
                        new Coordinate(nx, ny)));
                results.add(new Move(list));
            }
        }

        // multi-jump sequences (abstract, enemy-agnostic)
        getAllCapturesAt(x, y,
                new ArrayList<>(),
                results);

        return results;
    }

    private static boolean isInBounds(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    /**
     * [not used in application]
     * Helper function for getGlobalMoveSpaceFrom(x, y)
     * @param x x coordinate
     * @param y y coordinate
     * @param path previous actions [empty arrayList when calling, recursive]
     * @param results set to which moves are added
     */
    private static void getAllCapturesAt(int x, int y, List<Action> path, Set<Move> results) {
        if (!isInBounds(x, y)) {
            return;
        }
        int[][] dirs = {
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        if (!path.isEmpty()) {
            results.add(new Move(new ArrayList<>(path)));
        }

        for (int[] d : dirs) {
            int newX = x + 2 * d[0];
            int newY = y + 2 * d[1];

            if (!isInBounds(newX, newY)) {
                continue;
            }
            Coordinate landing = new Coordinate(newX, newY);
            Action action = new Action(new Coordinate(x, y), landing);
            //if already passed by, continue
            if (path.contains(action) || path.contains(new Action(action.getDestination(), action.getStart()))) {
                continue;
            }
            //Chain moves are captures, assume always enough pieces to capture (12 enemy pieces, too much to capture all in 8x8)
            path.add(action);
            getAllCapturesAt(newX, newY, path, results);

            // backtrack
            path.removeLast();
        }
    }


    private static void neuralNetTest() {
        Board b = new Board();
        b.cells[2][3] = 1; // ally man
        b.cells[5][4] = -1; // op man
        b.cells[0][7] = 2; // ally king

        NeuralNet net = new NeuralNet(8);

        PolicyValue pv = net.forward(b.splitBoardChannels());

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

     /*
        Map<Coordinate, Set<MoveIntent>> globalMoveSpace = new HashMap<>();
        //Loop through every possible position and stack moves


        List<Coordinate> keys = new ArrayList<>();
        for (int rowIdx = 0; rowIdx < 8; rowIdx++) {
            for (int xIdx = (rowIdx % 2 == 0 ? 0 : 1); xIdx < 8; xIdx += 2) {
                Set<MoveIntent> possibleMovesHere = getGlobalMoveSpaceFrom(xIdx, rowIdx);
                Coordinate key = new Coordinate(xIdx, rowIdx);
                globalMoveSpace.put(key, possibleMovesHere);
                keys.add(key);
            }
        }

        int total = 0;
        for (Coordinate key : keys) {
            int size = globalMoveSpace.get(key).size();
            System.out.printf("%s: %s\n", key, size);
            total += size;
        }

        try (FileWriter writer = new FileWriter(new File("Deliverable 3/Checkers/src/main/data/GlobalMoveSpace.csv"), false)) {
            for (Set<MoveIntent> moveIntents : globalMoveSpace.values()) {
                for (MoveIntent moveIntent : moveIntents) {
                    if (moveIntent.getActions().isEmpty()) {
                        continue;
                    }
                    StringBuilder rowCSV = new StringBuilder();
                    for (Action action : moveIntent.getActions()) {
                        rowCSV.append(String.format("%s,%s,%s,%s,", action.getStart().getX(), action.getStart().getY(), action.getDestination().getX(), action.getDestination().getY()));
                    }
                    writer.write(rowCSV + "\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        */
}
