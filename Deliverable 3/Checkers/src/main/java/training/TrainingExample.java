package training;

import game.Move;

import java.util.List;

/**
 * Data container for game states, policy pi, and game value z
 */
public class TrainingExample {
    public double[][][] state; // encoded board 12x8x8 channels
    public double[] pi;
    public List<Move> legalMoves;
    public boolean whiteToMove;
    public Double z;

    public TrainingExample(double[][][] state, double[] pi, List<Move> legalMoves, boolean whiteToMove) {
        this.state = state.clone();
        this.pi = pi;
        this.legalMoves = legalMoves;
        this.whiteToMove = whiteToMove;
        this.z = null;
    }
}
