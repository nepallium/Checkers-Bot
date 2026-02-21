package training;

/**
 * Data container for game states, policy pi, and game value z
 */
public class TrainingExample {
    public double[][][] state; // encoded board 12x8x8 channels
    public double[] pi;
    public boolean whiteToMove;
    public Double z;

    public TrainingExample(double[][][] state, double[] pi, boolean whiteToMove) {
        this.state = state.clone();
        this.pi = pi;
        this.whiteToMove = whiteToMove;
        this.z = null;
    }
}
