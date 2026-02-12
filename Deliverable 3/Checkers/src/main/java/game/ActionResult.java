package game;

public class ActionResult extends Action {
    private final int capturedPiece;

    public ActionResult(Action action) {
        this(action, 0);
    }

    public ActionResult(Action action, int capturedPiece) {
        super(action.getStart(), action.getDestination());
        this.capturedPiece = capturedPiece;
    }

    public ActionResult(Coordinate start, Coordinate destination, int capturedPiece) {
        super(start, destination);
        this.capturedPiece = capturedPiece;
    }

    @Override
    public String toString() {
        return String.format("Action<Start: %s, Destination: %s%s>", getStart(), getDestination(), capturedPiece != 0 ? ", CapturedPiece: " + capturedPiece : "");
    }

    public int getCapturedPiece() {
        return capturedPiece;
    }
}
