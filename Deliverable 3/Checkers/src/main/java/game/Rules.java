package game;
/* DEPRECATED - moved to Coordinate class
public class Rules {


    boolean isLegalMove(Move move) {
        int r = Board.row(move.from);
        int c = Board.col(move.from);
        return isDarkSquare(r, c) && r >= 0 && r < 8 && c >= 0 && c < 8;
    }

    boolean isDarkSquare(int r, int c) {
        return (r + c) % 2 == 1;
    }
}
*/