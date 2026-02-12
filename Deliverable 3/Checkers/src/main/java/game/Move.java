package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Move{

    private final List<ActionResult> actions;

    public Move() {
        this(new ArrayList<>());
    }

    public Move(List<ActionResult> actions) {
        this.actions = actions;
    }

    public Move(ActionResult action) {
        this.actions = List.of(action);
    }

    public Move(Coordinate start, Coordinate destination, int capturedPiece) {
        this.actions = List.of(new ActionResult(start, destination, capturedPiece));
    }

    public void addAction(ActionResult action) {
        this.actions.add(action);
    }

    @Override
    public String toString() {
        return String.format("Move:%s", actions.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Move move = (Move) o;
        return Objects.equals(actions, move.actions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), actions);
    }

    public boolean isCapture() {
        return !(actions.size() >= 2);
    }

    public List<ActionResult> getActions() {
        return actions;
    }
}
