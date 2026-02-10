package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Coordinate {
    /**
     * x index coordinate, bounded between 0 and 7, cells[y][x]
     */
    private int x;
    /**
     * y index coordinate, bounded between 0 and 7, cells[y][x]
     */
    private int y;

    public Coordinate(int xIdx, int yIdx) {
        this.x = xIdx;
        this.y = yIdx;
    }

    public Coordinate addedWith(Coordinate addCoords) {
        return new Coordinate(this.getX() + addCoords.getX(), this.getY() + addCoords.getY());
    }

    public List<Action> getPossibleManActions(boolean whiteToMove) {
        List<Action> actions = new ArrayList<>();
        int toYIdx = getY() + (whiteToMove ? 1 : -1);
        //No possible moves if is on an invalid square or at the end of the board
        if (isInvalid() || Coordinate.isIndexInvalid(toYIdx)) {
            return actions;
        }
        for (int xDisplacement = -1; xDisplacement <= 1; xDisplacement += 2) {
            int toXIdx = x + xDisplacement;
            if (isIndexInvalid(toXIdx)) {
                continue;
            }
            actions.add(new Action(this, new Coordinate(toXIdx, toYIdx)));
        }
        return actions;
    }

    /**
     * Gets the change in coordinates
     * @param other other coordinate C
     * @return self - C
     */
    public Coordinate getSubtracted(Coordinate other) {
        return new Coordinate(x - other.getX(), y - other.getY());
    }

    public Coordinate halved() {
        return new Coordinate(x/2, y/2);
    }

    public List<Action> getPossibleKingActions() {
        List<Action> actions = getPossibleManActions(true);
        actions.addAll(getPossibleManActions(false));
        return actions;
    }

    public void set(int XIdx, int YIdx) {
        this.x = XIdx;
        this.y = YIdx;
    }

    public int getYNotation() {
        return y + 1;
    }

    public int getXNotation() {
        return x + 1;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public boolean isInvalid() {
        return !isValidPiecePosition() || isIndexInvalid(x) || isIndexInvalid(y);
    }

    public static boolean isIndexInvalid(int entry) {
        return entry < 0 || entry >= 8;
    }

    private boolean isValidPiecePosition() {
        return (x + y) % 2 == 0;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s]", x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
