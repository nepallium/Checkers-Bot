package game;

import java.util.ArrayList;
import java.util.List;

public class Move {
    private Coordinate start;
    private Coordinate destination;
    private List<Coordinate> captures;


    public Move(Coordinate start, Coordinate destination, List<Coordinate> captures) {
        this.start = start;
        this.destination = destination;
        this.captures = captures;
    }

    public Move(Coordinate start, Coordinate destination) {
        this(start, destination, new ArrayList<>());
    }

    public Coordinate getDeltaCoordinates() {
        return new Coordinate(destination.getXIndex() - start.getXIndex(), destination.getYIndex() - start.getYIndex());
    }

    public Coordinate getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return String.format("<Start: %s, Destination: %s, Captures:%s>", start, destination, captures);
    }

    public void setDestination(Coordinate destination) {
        this.destination = destination;
    }

    public Coordinate getStart() {
        return start;
    }

    public void setStart(Coordinate start) {
        this.start = start;
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
