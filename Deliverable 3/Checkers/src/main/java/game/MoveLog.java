package game;

import java.util.ArrayList;
import java.util.List;

public class MoveLog {
    private final List<Move> whiteMoves;
    private final List<Move> blackMoves; //size is either equal to or one less than that of white moves
    private boolean whiteTurn;

    public MoveLog() {
        this(new ArrayList<Move>(List.of(new Move())), new ArrayList<Move>(List.of(new Move())), true);

    }

    public MoveLog(List<Move> whiteMoves, List<Move> blackMoves, boolean whiteTurn) {
        this.whiteMoves = whiteMoves;
        this.blackMoves = blackMoves;
        this.whiteTurn = whiteTurn;
    }

    public MoveLog getDuplicate(boolean invertColors) {
        return new MoveLog(invertColors ? blackMoves : whiteMoves, invertColors ? whiteMoves : blackMoves, invertColors != whiteTurn);
    }

    /**
     * /**
     * Adds an action to the log
     *
     * @param action   action to add
     * @param turnOver whether the turn is over or not
     */
    public void addActionLog(ActionResult action, boolean turnOver) {
        List<Move> playerMoves = whiteTurn ? whiteMoves : blackMoves;
        playerMoves.getLast().addAction(action);
        if (!turnOver) {
            return;
        }

        playerMoves.addLast(new Move());
        whiteTurn = !whiteTurn;
        return;
    }

    @Override
    public String toString() {
        String str = "";
        int blackMoveCount = blackMoves.size();
        for (int i = 0; i < blackMoveCount; i++) {
            str += String.format("%s: W:%s B: %s\n", i + 1, whiteMoves.size() > i ? whiteMoves.get(i).toString() : "---", blackMoves.size() > i ? blackMoves.get(i).toString() : "---");
        }
        return str;
    }

    /**
     * Returns the last move played and removes it from the logs
     * @return the last move played | null if no previous move played
     */
    public Move popLastMove() {
        System.out.println(whiteTurn);
        List<Move> currentPlayerTurnLog = whiteTurn ? whiteMoves : blackMoves;
        List<Move> lastPlayerTurnLog = whiteTurn ? blackMoves : whiteMoves;

        if (currentPlayerTurnLog.isEmpty() || lastPlayerTurnLog.isEmpty()) {
            return null;
        }
        whiteTurn = !whiteTurn;
        Move playingMove = currentPlayerTurnLog.getLast();
        if (!playingMove.isEmpty()) {
            currentPlayerTurnLog.set(currentPlayerTurnLog.size() - 1, new Move());
            return playingMove;
        }

        Move lastMove = lastPlayerTurnLog.getLast();
        if(!lastMove.isEmpty()) {
            lastPlayerTurnLog.set(lastPlayerTurnLog.size() - 1, new Move());
            return lastMove;
        }
        if (lastPlayerTurnLog.size() == 1) {
            lastPlayerTurnLog.add(new Move());
        }
        return lastPlayerTurnLog.remove(lastPlayerTurnLog.size() - 2);
    }

    public List<Move> getWhiteMoves() {
        return whiteMoves;
    }

    public List<Move> getBlackMoves() {
        return blackMoves;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }
}