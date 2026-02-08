package game;

import java.util.ArrayList;
import java.util.List;

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
        return new Coordinate(this.getXIndex() + addCoords.getXIndex(), this.getYIndex() + addCoords.getYIndex());
    }

    public List<Move> getPossibleManMoves(boolean whiteToMove) {
        List<Move> moves = new ArrayList<>();
        int toYIdx = getYIndex() + (whiteToMove ? 1 : -1);
        //No possible moves if is on an invalid square or at the end of the board
        if (isInvalid() || Coordinate.isIndexInvalid(toYIdx)) {
            return moves;
        }
        for (int xDisplacement = -1; xDisplacement <= 1; xDisplacement += 2) {
            int toXIdx = x + xDisplacement;
            if (isIndexInvalid(toXIdx)) {
                continue;
            }
            moves.add(new Move(this, new Coordinate(toXIdx, toYIdx)));
        }
        return moves;
    }

    public List<Move> getPossibleKingMoves() {
        List<Move> moves = getPossibleManMoves(true);
        moves.addAll(getPossibleManMoves(false));
        return  moves;
    }

    public int getYNotation() {
        return y + 1;
    }

    public int getXNotation() {
        return x + 1;
    }

    public int getYIndex() {
        return y;
    }

    public void setYIndex(int y) {
        this.y = y;
    }

    public int getXIndex() {
        return x;
    }

    public void setXIndex(int x) {
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
}
