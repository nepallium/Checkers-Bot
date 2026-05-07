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

    public String getMessageText() {
        return switch (this) {
            case WHITE_WIN -> "White Victory";
            case BLACK_WIN -> "Black Victory";
            case DRAW -> "Draw";
            case ONGOING -> "Game Ongoing";
        };
    }
}