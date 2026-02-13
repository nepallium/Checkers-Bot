package game;

public class ActionResult extends Action {
    private final int capturedPiece;
    private boolean promotion;

    public ActionResult(Action action) {
        this(action, 0, false);
    }

    public ActionResult(Action action, int capturedPiece) {
        this(action, capturedPiece, false);
    }

    public ActionResult(Action action, int capturedPiece, boolean promotion) {
        super(action.getStart(), action.getDestination());
        this.capturedPiece = capturedPiece;
        this.promotion = promotion;
    }

    public ActionResult(Coordinate start, Coordinate destination, int capturedPiece, boolean promotion) {
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

    public boolean isPromotion() {
        return promotion;
    }

}
