package game;

import java.util.ArrayList;
import java.util.List;

public class MoveLog {
    private final List<MoveResult> whiteMoveResults;
    private final List<MoveResult> blackMoveResults; //size is either equal to or one less than that of white moves
    private boolean whiteTurn;

    public MoveLog() {
        this(new ArrayList<MoveResult>(List.of(new MoveResult())), new ArrayList<MoveResult>(List.of(new MoveResult())), true);

    }

    public MoveLog(List<MoveResult> whiteMoveResults, List<MoveResult> blackMoveResults, boolean whiteTurn) {
        this.whiteMoveResults = whiteMoveResults;
        this.blackMoveResults = blackMoveResults;
        this.whiteTurn = whiteTurn;
    }

    public MoveLog getDuplicate(boolean invertColors) {
        return new MoveLog(invertColors ? blackMoveResults : whiteMoveResults, invertColors ? whiteMoveResults : blackMoveResults, invertColors != whiteTurn);
    }

    /**
     * /**
     * Adds an action to the log
     *
     * @param action   action to add
     * @param turnOver whether the turn is over or not
     */
    public void addActionLog(ActionResult action, boolean turnOver) {
        List<MoveResult> playerMoveResults = whiteTurn ? whiteMoveResults : blackMoveResults;
        playerMoveResults.getLast().addAction(action);
        if (!turnOver) {
            return;
        }

        playerMoveResults.addLast(new MoveResult());
        whiteTurn = !whiteTurn;
        return;
    }

    @Override
    public String toString() {
        String str = "";
        int blackMoveCount = blackMoveResults.size();
        for (int i = 0; i < blackMoveCount; i++) {
            str += String.format("%s: W:%s B: %s\n", i + 1, whiteMoveResults.size() > i ? whiteMoveResults.get(i).toString() : "---", blackMoveResults.size() > i ? blackMoveResults.get(i).toString() : "---");
        }
        return str;
    }

    /**
     * Returns the last move played and removes it from the logs
     * @return the last move played | null if no previous move played
     */
    public MoveResult popLastMove() {
        List<MoveResult> currentPlayerTurnLog = whiteTurn ? whiteMoveResults : blackMoveResults;
        List<MoveResult> lastPlayerTurnLog = whiteTurn ? blackMoveResults : whiteMoveResults;

        if (currentPlayerTurnLog.isEmpty() || lastPlayerTurnLog.isEmpty()) {
            return null;
        }
        whiteTurn = !whiteTurn;
        MoveResult playingMoveResult = currentPlayerTurnLog.getLast();
        if (!playingMoveResult.isEmpty()) {
            currentPlayerTurnLog.set(currentPlayerTurnLog.size() - 1, new MoveResult());
            return playingMoveResult;
        }

        MoveResult lastMoveResult = lastPlayerTurnLog.getLast();
        if(!lastMoveResult.isEmpty()) {
            lastPlayerTurnLog.set(lastPlayerTurnLog.size() - 1, new MoveResult());
            return lastMoveResult;
        }
        if (lastPlayerTurnLog.size() == 1) {
            lastPlayerTurnLog.add(new MoveResult());
        }
        return lastPlayerTurnLog.remove(lastPlayerTurnLog.size() - 2);
    }

    public List<MoveResult> getWhiteMoves() {
        return whiteMoveResults;
    }

    public List<MoveResult> getBlackMoves() {
        return blackMoveResults;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }
}