package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Move {
    private final List<Action> actions;
    public Move() {
        this(new ArrayList<>());
    }

    public Move(MoveResult moveResult) {
        this(moveResult.getActionResults().stream().map(ActionResult::toSuper).toList());
    }

    public Move(List<Action> actions) {
        this.actions = actions;
    }

    public Move(Action... actions) {
        this.actions = new ArrayList<>(List.of(actions));
    }

    public List<Action> getActions() {
        return actions;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Move that = (Move) o;
        return Objects.equals(actions, that.actions);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(actions);
    }
}
