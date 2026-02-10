package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Move{

    private final List<Action> actions;

    public Move() {
        this(new ArrayList<>());
    }

    public Move(List<Action> actions) {
        this.actions = actions;
    }

    public Move(Coordinate start, List<Coordinate> captureCoordinates) {
        this.actions = new ArrayList<>();
        Coordinate lastCoordinate = start;
        for (Coordinate capture : captureCoordinates) {
            Coordinate endCoordinate = lastCoordinate.addedWith(capture.getSubtracted(lastCoordinate));
            actions.add(new Action(lastCoordinate, endCoordinate));
            lastCoordinate = endCoordinate;

        }
    }

    public Move(Action action) {
        this.actions = List.of(action);
    }

    public Move(Coordinate start, Coordinate destination) {
        this.actions = List.of(new Action(start, destination));
    }

    public void addAction(Action action) {
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

    public List<Action> getActions() {
        return actions;
    }
}
