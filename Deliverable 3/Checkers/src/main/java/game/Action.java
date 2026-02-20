package game;

import java.util.*;

public class Action {
    private Coordinate start;
    private Coordinate destination;


    public Action(Coordinate start, Coordinate destination) {
        this.start = start;
        this.destination = destination;
    }

    public Coordinate getDeltaCoordinate() {
        return new Coordinate(destination.getX() - start.getX(), destination.getY() - start.getY());
    }

    @Override
    public String toString() {
        return String.format("Action<Start: %s, Destination: %s>", start, destination);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Action action)) return false;
        return Objects.equals(start, action.start) && Objects.equals(destination, action.destination);
    }

    public Coordinate getCaptureCoordinate() {
        Coordinate deltaCoordinates = getDeltaCoordinate();
        if (deltaCoordinates.getX() == 1 || deltaCoordinates.getX() == -1) {
            return null;
        }
        return getStart().addedWith(deltaCoordinates.halved());
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, destination);
    }

    public void setDestination(Coordinate destination) {
        this.destination = destination;
    }

    public Coordinate getDestination() {
        return destination;
    }

    public Coordinate getStart() {
        return start;
    }

    public void setStart(Coordinate start) {
        this.start = start;
    }



    public final static List<Action> globalActionSpace = (Arrays.asList(
            new Action(new Coordinate(0, 0), new Coordinate(1, 1)),
            new Action(new Coordinate(2, 0), new Coordinate(3, 1)),
            new Action(new Coordinate(2, 0), new Coordinate(1, 1)),
            new Action(new Coordinate(4, 0), new Coordinate(5, 1)),
            new Action(new Coordinate(4, 0), new Coordinate(3, 1)),
            new Action(new Coordinate(6, 0), new Coordinate(7, 1)),
            new Action(new Coordinate(6, 0), new Coordinate(5, 1)),
            new Action(new Coordinate(1, 1), new Coordinate(2, 2)),
            new Action(new Coordinate(1, 1), new Coordinate(0, 0)),
            new Action(new Coordinate(1, 1), new Coordinate(0, 2)),
            new Action(new Coordinate(1, 1), new Coordinate(2, 0)),
            new Action(new Coordinate(3, 1), new Coordinate(4, 2)),
            new Action(new Coordinate(3, 1), new Coordinate(2, 0)),
            new Action(new Coordinate(3, 1), new Coordinate(2, 2)),
            new Action(new Coordinate(3, 1), new Coordinate(4, 0)),
            new Action(new Coordinate(5, 1), new Coordinate(6, 2)),
            new Action(new Coordinate(5, 1), new Coordinate(4, 0)),
            new Action(new Coordinate(5, 1), new Coordinate(4, 2)),
            new Action(new Coordinate(5, 1), new Coordinate(6, 0)),
            new Action(new Coordinate(7, 1), new Coordinate(6, 0)),
            new Action(new Coordinate(7, 1), new Coordinate(6, 2)),
            new Action(new Coordinate(0, 2), new Coordinate(1, 3)),
            new Action(new Coordinate(0, 2), new Coordinate(1, 1)),
            new Action(new Coordinate(2, 2), new Coordinate(3, 3)),
            new Action(new Coordinate(2, 2), new Coordinate(1, 1)),
            new Action(new Coordinate(2, 2), new Coordinate(1, 3)),
            new Action(new Coordinate(2, 2), new Coordinate(3, 1)),
            new Action(new Coordinate(4, 2), new Coordinate(5, 3)),
            new Action(new Coordinate(4, 2), new Coordinate(3, 1)),
            new Action(new Coordinate(4, 2), new Coordinate(3, 3)),
            new Action(new Coordinate(4, 2), new Coordinate(5, 1)),
            new Action(new Coordinate(6, 2), new Coordinate(7, 3)),
            new Action(new Coordinate(6, 2), new Coordinate(5, 1)),
            new Action(new Coordinate(6, 2), new Coordinate(5, 3)),
            new Action(new Coordinate(6, 2), new Coordinate(7, 1)),
            new Action(new Coordinate(1, 3), new Coordinate(2, 4)),
            new Action(new Coordinate(1, 3), new Coordinate(0, 2)),
            new Action(new Coordinate(1, 3), new Coordinate(0, 4)),
            new Action(new Coordinate(1, 3), new Coordinate(2, 2)),
            new Action(new Coordinate(3, 3), new Coordinate(4, 4)),
            new Action(new Coordinate(3, 3), new Coordinate(2, 2)),
            new Action(new Coordinate(3, 3), new Coordinate(2, 4)),
            new Action(new Coordinate(3, 3), new Coordinate(4, 2)),
            new Action(new Coordinate(5, 3), new Coordinate(6, 4)),
            new Action(new Coordinate(5, 3), new Coordinate(4, 2)),
            new Action(new Coordinate(5, 3), new Coordinate(4, 4)),
            new Action(new Coordinate(5, 3), new Coordinate(6, 2)),
            new Action(new Coordinate(7, 3), new Coordinate(6, 2)),
            new Action(new Coordinate(7, 3), new Coordinate(6, 4)),
            new Action(new Coordinate(0, 4), new Coordinate(1, 5)),
            new Action(new Coordinate(0, 4), new Coordinate(1, 3)),
            new Action(new Coordinate(2, 4), new Coordinate(3, 5)),
            new Action(new Coordinate(2, 4), new Coordinate(1, 3)),
            new Action(new Coordinate(2, 4), new Coordinate(1, 5)),
            new Action(new Coordinate(2, 4), new Coordinate(3, 3)),
            new Action(new Coordinate(4, 4), new Coordinate(5, 5)),
            new Action(new Coordinate(4, 4), new Coordinate(3, 3)),
            new Action(new Coordinate(4, 4), new Coordinate(3, 5)),
            new Action(new Coordinate(4, 4), new Coordinate(5, 3)),
            new Action(new Coordinate(6, 4), new Coordinate(7, 5)),
            new Action(new Coordinate(6, 4), new Coordinate(5, 3)),
            new Action(new Coordinate(6, 4), new Coordinate(5, 5)),
            new Action(new Coordinate(6, 4), new Coordinate(7, 3)),
            new Action(new Coordinate(1, 5), new Coordinate(2, 6)),
            new Action(new Coordinate(1, 5), new Coordinate(0, 4)),
            new Action(new Coordinate(1, 5), new Coordinate(0, 6)),
            new Action(new Coordinate(1, 5), new Coordinate(2, 4)),
            new Action(new Coordinate(3, 5), new Coordinate(4, 6)),
            new Action(new Coordinate(3, 5), new Coordinate(2, 4)),
            new Action(new Coordinate(3, 5), new Coordinate(2, 6)),
            new Action(new Coordinate(3, 5), new Coordinate(4, 4)),
            new Action(new Coordinate(5, 5), new Coordinate(6, 6)),
            new Action(new Coordinate(5, 5), new Coordinate(4, 4)),
            new Action(new Coordinate(5, 5), new Coordinate(4, 6)),
            new Action(new Coordinate(5, 5), new Coordinate(6, 4)),
            new Action(new Coordinate(7, 5), new Coordinate(6, 4)),
            new Action(new Coordinate(7, 5), new Coordinate(6, 6)),
            new Action(new Coordinate(0, 6), new Coordinate(1, 7)),
            new Action(new Coordinate(0, 6), new Coordinate(1, 5)),
            new Action(new Coordinate(2, 6), new Coordinate(3, 7)),
            new Action(new Coordinate(2, 6), new Coordinate(1, 5)),
            new Action(new Coordinate(2, 6), new Coordinate(1, 7)),
            new Action(new Coordinate(2, 6), new Coordinate(3, 5)),
            new Action(new Coordinate(4, 6), new Coordinate(5, 7)),
            new Action(new Coordinate(4, 6), new Coordinate(3, 5)),
            new Action(new Coordinate(4, 6), new Coordinate(3, 7)),
            new Action(new Coordinate(4, 6), new Coordinate(5, 5)),
            new Action(new Coordinate(6, 6), new Coordinate(7, 7)),
            new Action(new Coordinate(6, 6), new Coordinate(5, 5)),
            new Action(new Coordinate(6, 6), new Coordinate(5, 7)),
            new Action(new Coordinate(6, 6), new Coordinate(7, 5)),
            new Action(new Coordinate(1, 7), new Coordinate(0, 6)),
            new Action(new Coordinate(1, 7), new Coordinate(2, 6)),
            new Action(new Coordinate(3, 7), new Coordinate(2, 6)),
            new Action(new Coordinate(3, 7), new Coordinate(4, 6)),
            new Action(new Coordinate(5, 7), new Coordinate(4, 6)),
            new Action(new Coordinate(5, 7), new Coordinate(6, 6)),
            new Action(new Coordinate(7, 7), new Coordinate(6, 6))
    ));
     /*
        //Get and print the global action space
        HashMap<Coordinate, Set<Action>> globalActionSpace = new HashMap<>();
        for (int rowIdx = 0; rowIdx < 8; rowIdx++) {
            for (int xIdx = (rowIdx % 2 == 0 ? 0 : 1); xIdx < 8; xIdx += 2) {
                Coordinate coords = new Coordinate(xIdx,rowIdx);
                List<Action> kingActions = coords.getPossibleKingActions();
                HashSet<Action> coordsActionSpace = new HashSet<>();
                globalActionSpace.put(new Coordinate(xIdx, rowIdx), coordsActionSpace);
                for(Action action : kingActions) {
                    coordsActionSpace.add(action);
                    Coordinate actionDestination = action.getDestination();
                    if (!(actionDestination.isInvalid() || actionDestination.addedWith(action.getDeltaCoordinates()).isInvalid())) {
                        Action captureAction = new Action(action.getStart(), actionDestination.addedWith(action.getDeltaCoordinates()));
                    }
                }
            }
        }
        for (int rowIdx = 0; rowIdx < 8; rowIdx++) {
            for (int xIdx = (rowIdx % 2 == 0 ? 0 : 1); xIdx < 8; xIdx += 2) {
                Set<Action> coordsActionSpace = globalActionSpace.get(new Coordinate(xIdx, rowIdx));
                coordsActionSpace.forEach(action -> {
                    System.out.printf("new Action(new Coordinate(%s, %s), new Coordinate(%s, %s)),\n", action.getStart().getXIndex(), action.getStart().getYIndex(), action.getDestination().getXIndex(), action.getDestination().getYIndex());
                });
            }
        }
     */
}

