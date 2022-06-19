package boxAstar;

import java.util.*;
import java.awt.geom.*;
import javafx.util.Pair;
import problem.*;
import utility.*;

/**
 * This class is used to solve box node problem.
 * From one environment to another environment.
 * It takes the start environment and the end environment, and
 * return a list of Actions that needs to be solved by robot.
 */
public class BoxNodeAstar {

    /**
     * Construct the box route for the problem.
     * @param thisState - The goal state.
     * @param meta - The route information.
     * @return - A list of Actions that needs to be solved by robot.
     */
    private static List<Action> construct_box_route(BoxNode thisState, Map<BoxNode, Pair<BoxNode, Action>> meta) {
        List<Action> result = new ArrayList<>();
        Stack<Action> actions = new Stack<>();
        BoxNode state = thisState;
        while (meta.get(state).getKey() != null) {
            Pair<BoxNode, Action> parent_action = meta.get(state);
            actions.push(parent_action.getValue());
            state = parent_action.getKey();
        }
        int size = actions.size();
        for (int i = 0; i < size; i++) {
            result.add(actions.pop());
        }
        return result;
    }

    /**
     * Solve the transition from a given environment to a goal environment.
     * @param start - The start environment.
     * @param goal - The goal environment.
     * @param result - The result.
     * @param moved - The index for the box that can be moved.
     * @param maxCount - The max computation steps.
     * @return - True if solved, false otherwise.
     */
    public static boolean solve(Environment start, Environment goal, List<Action> result, int moved, int maxCount) {
        BoxNode initial = new BoxNode(start, goal, moved);

        Map<BoxNode, Pair<BoxNode, Action>> meta = new HashMap<>();
        Comparator<BoxNode> comparator = new BoxNodeComparator();
        PriorityQueue<BoxNode> toBeVisited = new PriorityQueue<>(11, comparator);
        Set<BoxNode> visited = new HashSet<>();

        /* Set initial state. */
        meta.put(initial, new Pair<>(null, null));
        toBeVisited.add(initial);

        int count = 0;
        while(toBeVisited.size() != 0) {

            if (count++ > maxCount) {
                break;
            }

            BoxNode thisState = toBeVisited.remove();
            visited.add(thisState);

            if (thisState.is_goal()) {
                result.addAll(construct_box_route(thisState, meta));
                return true;
            }

            /* Get children for this state. */
            List<Pair<BoxNode, Action>> stateActions = thisState.get_children();

            for (Pair<BoxNode, Action> p : stateActions) {
                BoxNode child = p.getKey();
                Action action = p.getValue();
                if (!visited.contains(child) && !toBeVisited.contains(child)) {
                    toBeVisited.add(child);
                    meta.put(child, new Pair<>(thisState, action));
                }
            }
        }
        return false;
    }

    /**
     * Solve the transition from a given environment to a goal, only one box is moved.
     * @param start - The start environment.
     * @param goal - The goal of the moved box.
     * @param result - The result.
     * @param moved - The index for the box that can be moved.
     * @param maxCount - The max computation steps.
     * @return - Goal environment if solved, null otherwise.
     */
    public static Environment solve(Environment start, Point2D goal, List<Action> result, int moved, int maxCount) {
        List<Box> objects = new ArrayList<>();
        for (int i = 0; i < start.movingObjects.size(); i++) {
            double width = start.movingObjects.get(i).getWidth();
            if (i == moved) {
                objects.add(new MovingBox(new Point2D.Double(goal.getX(), goal.getY()), width));
            } else {
                objects.add(start.movingObjects.get(i));
            }
        }
        Environment goalEnvironment = new Environment(objects, start.staticObstacles);
        if (solve(start, goalEnvironment, result, moved, maxCount)) {
            return goalEnvironment;
        } else {
            return null;
        }
    }
}
