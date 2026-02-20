package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MoveResult {
    private final List<ActionResult> actionResults;

    public MoveResult() {
        this(new ArrayList<>());
    }

    public MoveResult(List<ActionResult> actionResults) {
        this.actionResults = actionResults;
    }

    public MoveResult(ActionResult actionResult) {
        this.actionResults = List.of(actionResult);
    }

    public MoveResult(Coordinate start, Coordinate destination, int capturedPiece, boolean promotion) {
        this.actionResults = List.of(new ActionResult(start, destination, capturedPiece, promotion));
    }

    public void addAction(ActionResult action) {
        this.actionResults.add(action);
    }

    /**
     * Check if the move is empty
     * @return true if the move has no actions, false if it does
     */
    public boolean isEmpty() {
        return this.actionResults.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("Move:%s", actionResults.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MoveResult moveResult = (MoveResult) o;
        return Objects.equals(actionResults, moveResult.actionResults);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), actionResults);
    }

    public boolean isCapture() {
        return !(actionResults.size() >= 2);
    }

    public List<ActionResult> getActionResults() {
        return actionResults;
    }
}
