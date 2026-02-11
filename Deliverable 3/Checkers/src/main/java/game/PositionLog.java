package game;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PositionLog {
    private final List<int[][]> recentPositions; //index from most recent to least
    private final int maxPositionLogs;

    public PositionLog(int maxPositionLogs) {
        recentPositions = new LinkedList<>();
        this.maxPositionLogs = maxPositionLogs;
    }

    public PositionLog(int maxPositionLogs, List<int[][]> recentPositions) {
        this.maxPositionLogs = maxPositionLogs;
        this.recentPositions = new LinkedList<>(recentPositions.subList(0, Math.min(recentPositions.size(), maxPositionLogs)));
    }

    /**
     *  Adds a board position to the log
     *
     * @param board board to save position of (deep copies it)
     */
    public void addPosition(int[][] board) {
        int[][] boardCopy = new int[8][8];
        for (int i = 0; i < 8; i++) {
            boardCopy[i] = board[i].clone();
        }
        recentPositions.addFirst(boardCopy);
        if (recentPositions.size() > maxPositionLogs) {
            recentPositions.removeLast();
        }
    }

    /**
     *
     * @return if there is a draw by repetition (same position reached 3x)
     *
     */
    public boolean checkForDrawByRepetition() {
        if (recentPositions.size() < 6) {
            return false;
        }
        for (int i = 2; i < Board.DRAW_BY_REPETITION_REPEAT_AMOUNT; i++) {
            //Check this position equals last position (rewind by 2, because by 1 is last player's turn if there was no double capture [wouldn't be a draw])
            if (!Arrays.deepEquals(recentPositions.getFirst(), recentPositions.get(i))) {
                return false;
            }

        }
        return true;
    }

    public List<int[][]> getRecentPositions() {
        return recentPositions;
    }

    public int getMaxPositionLogs() {
        return maxPositionLogs;
    }
}