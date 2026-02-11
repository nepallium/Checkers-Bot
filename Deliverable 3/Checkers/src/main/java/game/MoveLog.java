package game;

import java.util.ArrayList;
import java.util.List;

public class MoveLog {
    private final List<Move> whiteMoves;
    private final List<Move> blackMoves; //size is either equal to or one less than that of white moves
    private boolean whiteTurn = true;

    public MoveLog() {
        this.whiteMoves = new ArrayList<Move>(List.of(new Move()));
        this.blackMoves = new ArrayList<Move>(List.of(new Move()));
    }

    /**
     /**
     * Adds an action to the log
     *
     * @param action   action to add
     * @param turnOver whether the turn is over or not
     */
    public void addActionLog(Action action, boolean turnOver) {
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
            str += String.format("W:%s B: %s\n", whiteMoves.get(i).toString(), blackMoves.get(i).toString());
        }
        return str;
    }
}