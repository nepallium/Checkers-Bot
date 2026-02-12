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
            str += String.format("%s: W:%s B: %s\n", i + 1, whiteMoves.get(i).toString(), blackMoves.get(i).toString());
        }
        return str;
    }

    /**
     * Returns the last move played and removes it from the logs
     * @return the last move played
     */
    public Move popLastMove() {
        //black moves last, so check for black first
        if (!blackMoves.getLast().getActions().isEmpty()) {
            whiteTurn = true;
            Move move = blackMoves.removeLast();
            blackMoves.add(new Move());
            return move;
        }
        //Check for white
        if (!whiteMoves.getLast().getActions().isEmpty()) {
            whiteTurn = false;
            Move move = whiteMoves.removeLast();
            whiteMoves.add(new Move());
            return move;
        }
        //Must be black's second to last log
        whiteTurn = true;
        if (blackMoves.size() <= 1) {
            return null;
        }
        return blackMoves.remove(blackMoves.size() - 2);
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