package boxAstar;

import java.util.*;
import java.awt.geom.*;
import javafx.util.Pair;
import utility.*;

/**
 * This class is used to solve tree node problem.
 * From one environment, try to move moving boxes to goals.
 * It takes the start environment and the goal positions, and
 * return a list of Actions that needs to be solved by robot.
 */
public class TreeNodeAstar {

    /**
     * Construct the box route for the problem.
     * @param thisState - The goal state.
     * @param meta - The route information.
     * @return - A list of Actions that needs to be solved by robot.
     */
    private static List<Action> construct_box_route(TreeNode thisState, Map<TreeNode, Pair<TreeNode, Action>> meta) {
        List<Action> result = new ArrayList<>();
        Stack<Action> actions = new Stack<>();
        TreeNode state = thisState;
        while (meta.get(state).getKey() != null) {
            Pair<TreeNode, Action> parent_action = meta.get(state);
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
     * Solve the environment.
     * @param environment - The start environment.
     * @param goals - The position of the goals.
     * @param phase - The phase of the search.
     * @param result - The result.
     * @param time - The max time.
     * @return - The closest environment to the goal.
     */
    public static Environment solve(Environment environment, List<Point2D> goals, Phase phase, List<Action> result, double time) {

        TreeNode initial = new TreeNode(environment, goals);

        Map<TreeNode, Pair<TreeNode, Action>> meta = new HashMap<>();
        Comparator<TreeNode> comparator = new TreeNodeComparator();
        PriorityQueue<TreeNode> toBeVisited = new PriorityQueue<>(11, comparator);
        PriorityQueue<TreeNode> visited = new PriorityQueue<>(11, comparator);

        /* Set initial state. */
        meta.put(initial, new Pair<>(null, null));
        toBeVisited.add(initial);

        /*  Used for detect trap. */
        int count = 0;
        double start_time = System.currentTimeMillis();
        while(toBeVisited.size() != 0) {

            TreeNode thisState = toBeVisited.remove();
            visited.add(thisState);

            count++;

            if (System.currentTimeMillis() - start_time > time) {
                break;
            }

            if (thisState.is_goal(phase)) {
                System.out.printf("Stage solved with %d steps.\n", count);
                result.addAll(construct_box_route(thisState, meta));
                return thisState.state;
            }

            /* Get children for this state. */
            List<Pair<TreeNode, Action>> stateActions = thisState.get_children(phase);

            for (Pair<TreeNode, Action> p : stateActions) {
                TreeNode child = p.getKey();
                Action action = p.getValue();
                if (!visited.contains(child) && !toBeVisited.contains(child)) {
                    toBeVisited.add(child);
                    meta.put(child, new Pair<>(thisState, action));
                }
            }
        }
        System.out.printf("Stage solved to most close solution with %d steps.\n", count);
        TreeNode closest = visited.remove();
        result.addAll(construct_box_route(closest, meta));
        return closest.state;
    }
}