package game;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.util.*;

public class Move {
    private final List<Action> actions;

    public Move() {
        this(new ArrayList<>());
    }

    public Move(MoveResult moveResult) {
        this(moveResult.getActionResults().stream().map(ActionResult::toSuper).toList());
    }

    public Move(int... coordinateBases) {
        this.actions = new ArrayList<>();
        if (coordinateBases.length % 4 ==0) {
            for (int i = 0; i < coordinateBases.length; i += 4) {
                Action action = new Action(new Coordinate(coordinateBases[i], coordinateBases[i + 1]), new Coordinate(coordinateBases[i + 2], coordinateBases[i + 3]));
                actions.add(action);
            }
        } else {
            System.out.println("INVALID CONSTRUCTOR FOR MOVE WITH INT... " + coordinateBases.toString());
        }
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

    public Coordinate getStart() {
        return actions.getFirst().getStart();
    }

    public Coordinate getDestination() {
        return actions.getLast().getDestination();
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
    public static final String GLOBAL_MOVE_SPACE_FILE_PATH = "data/GlobalMoveSpace.csv";
    public static final int GLOBAL_MOVE_SPACE_SIZE = 1426;
    public static final Move[] GLOBAL_MOVE_SPACE = new Move[GLOBAL_MOVE_SPACE_SIZE];

    /**
     * Call once, sets the global move space array, if called again, does nothing
     */
    public static void init() {
        if (GLOBAL_MOVE_SPACE[0] != null) {
            System.out.println("GlobalMoveSpace already loaded, skipping.");
            return;
        }

        try {
            setGlobalMoveSpace();
        } catch (Exception err) {
            System.err.println("ERROR loading GlobalMoveSpace: " + err.getMessage());
            err.printStackTrace();
        }

        if (GLOBAL_MOVE_SPACE[0] == null) {
            System.err.println("LOAD FAILED — GLOBAL_MOVE_SPACE[0] is still null after init()");
        } else {
            System.out.println("LOAD OK — GLOBAL_MOVE_SPACE[0] = " + GLOBAL_MOVE_SPACE[0]);
        }
    }

    /**
     * Sets the global move space array with the CSV file of the global move space
     */
    private static void setGlobalMoveSpace() throws IOException {
        try (InputStream is = Move.class.getResourceAsStream("/data/GlobalMoveSpace.csv");
             CSVReader reader = new CSVReader(new InputStreamReader(Objects.requireNonNull(is)))) {

            for (int i = 0; i < GLOBAL_MOVE_SPACE_SIZE; i++) {
                String[] split = reader.readNext();
                int[] nums = Arrays.stream(split)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .mapToInt(Integer::parseInt)
                        .toArray();
                List<Action> actions = new ArrayList<>();
                for (int j = 0; j + 4 <= nums.length; j += 4) {
                    actions.add(new Action(
                            new Coordinate(nums[j],     nums[j + 1]),
                            new Coordinate(nums[j + 2], nums[j + 3])
                    ));
                }
                GLOBAL_MOVE_SPACE[i] = new Move(actions);
            }

        } catch (CsvValidationException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

}
