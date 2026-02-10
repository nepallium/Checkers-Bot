package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Move extends Action{

    private List<Coordinate> captures;

    public Move(Action action, List<Coordinate> captures) {
        super(action.getStart(), action.getDestination());
        this.captures = captures;
    }

    public Move(Coordinate start, Coordinate destination, List<Coordinate> captures) {
        super(start, destination);
        this.captures = captures;
    }

    public Move(Coordinate start, Coordinate destination) {
        this(start, destination, new ArrayList<>());
    }

    @Override
    public String toString() {
        return String.format("Move<Start: %s, Destination: %s, Captures:%s>", super.getStart(), super.getDestination(), captures);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Move move = (Move) o;
        return Objects.equals(captures, move.captures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), captures);
    }

    public List<Coordinate> getCaptures() {
        return captures;
    }

    public void setCaptures(List<Coordinate> captures) {
        this.captures = captures;
    }

    public boolean isCapture() {
        return !this.captures.isEmpty();
    }
}
