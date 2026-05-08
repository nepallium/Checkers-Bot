package UI;

/**
 * Stores the user's current preference selections.
 * Instantiated by MainGameController and passed to PreferencesController.
 */
public class AppPreferences {

    public enum PieceStyle { DOG, DEFAULT }
    public enum BoardTheme { BLUE_WHITE, BROWN_WHITE }
    public enum TargetIcon { CIRCLE, X }

    private PieceStyle pieceStyle = PieceStyle.DOG;
    private BoardTheme boardTheme = BoardTheme.BLUE_WHITE;
    private TargetIcon targetIcon = TargetIcon.CIRCLE;

    public PieceStyle getPieceStyle()             { return pieceStyle; }
    public void       setPieceStyle(PieceStyle s) { this.pieceStyle = s; }

    public BoardTheme getBoardTheme()             { return boardTheme; }
    public void       setBoardTheme(BoardTheme t) { this.boardTheme = t; }

    public TargetIcon getTargetIcon()             { return targetIcon; }
    public void       setTargetIcon(TargetIcon i) { this.targetIcon = i; }

    public String getDarkSquareColor() {
        return boardTheme == BoardTheme.BLUE_WHITE ? "#4a6fa5" : "#8B4513";
    }

    public String getLightSquareColor() {
        return boardTheme == BoardTheme.BLUE_WHITE ? "#dce8f8" : "#f0d9b5";
    }
}