package game;

import java.io.IOException;
import java.util.*;
import java.io.File;

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

    public String toString() {
        return String.format("Move:%s", actions.toString());
    }

    /// GLOBAL ACTION SPACE
    public static final String GLOBAL_MOVE_SPACE_FILE_PATH = "Deliverable 3/Checkers/src/main/data/GlobalMoveSpace.csv";
    public static final int GLOBAL_MOVE_SPACE_SIZE = 1666;
    public static final Move[] GLOBAL_MOVE_SPACE = new Move[GLOBAL_MOVE_SPACE_SIZE];

    /**
     * Call once, sets the global move space array, if called again, does nothing
     */
    public static void init() {
        if (GLOBAL_MOVE_SPACE[0] != null) {
            return;
        }
        setGlobalMoveSpace();
    }

    /**
     * Sets the global move space array with the CSV file of the global move space
     */
    private static void setGlobalMoveSpace() {
        File file = new File(GLOBAL_MOVE_SPACE_FILE_PATH);
        try (Scanner scanner = new Scanner(file)) {
            for (int i = 0; i < GLOBAL_MOVE_SPACE_SIZE; i++) {
                String line = scanner.nextLine();
                String[] split = line.split(",");
                int[] nums = Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
                List<Action> actions = new ArrayList<>();
                for (int j = 0; j >= 0; j += 4) {
                    if (nums.length < j + 4) {
                        break;
                    }
                    actions.add(new Action(new Coordinate(nums[j], nums[j + 1]), new Coordinate(nums[j + 2], nums[j + 3])));
                }
                GLOBAL_MOVE_SPACE[i] = new Move(actions);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
