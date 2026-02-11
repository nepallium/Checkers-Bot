package game;

public enum GameResult {
    WHITE_WIN, BLACK_WIN, DRAW, ONGOING;

    GameResult colorInverted() {
        if (this.equals(WHITE_WIN)) {
            return BLACK_WIN;
        }
        if (this.equals(BLACK_WIN)) {
            return WHITE_WIN;
        }
        return this;
    }
}