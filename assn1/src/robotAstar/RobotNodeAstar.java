package robotAstar;

import java.util.*;
import javafx.util.Pair;
import problem.*;
import utility.*;

/**
 * This class is used to solve robot node problem.
 * From one robot config to another robot config.
 * It takes the environment, start robot config, end robot config, and
 * return a list of actions.
 */
public class RobotNodeAstar implements Utility {

    /**
     * Construct the robot route for the problem.
     * @param thisState - The goal state.
     * @param meta - The route information.
     * @return - A list of Actions.
     */
    private static List<String> construct_robot_route(RobotNode thisState, Map<RobotNode, Pair<RobotNode, String>> meta) {
        List<String> result = new ArrayList<>();
        Stack<String> actions = new Stack<>();
        RobotNode state = thisState;
        while (meta.get(state).getKey() != null) {
            Pair<RobotNode, String> parent_action = meta.get(state);
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
     * Solve the transition from a start robot config to a goal robot config.
     * @param action - The action to be solved.
     * @param current - The current robot config.
     * @param result - The result.
     * @param maxCount - The max computation steps.
     * @return - True if solved, false otherwise.
     */
    public static boolean solve(Action action, RobotConfig current, List<String> result, int maxCount) {

        RobotNode initial = new RobotNode(action.environment, current, action.pushPosition);

        Map<RobotNode, Pair<RobotNode, String>> meta = new HashMap<>();
        Comparator<RobotNode> comparator = new RobotNodeComparator();
        PriorityQueue<RobotNode> toBeVisited = new PriorityQueue<>(11, comparator);
        Set<RobotNode> visited = new HashSet<>();

        meta.put(initial, new Pair<>(null, null));
        toBeVisited.add(initial);

        while(toBeVisited.size() != 0) {

            if (visited.size() > maxCount) {
                break;
            }

            RobotNode thisState = toBeVisited.remove();
            visited.add(thisState);

            if (thisState.is_goal()) {
                result.addAll(construct_robot_route(thisState, meta));
                return true;
            }

            List<Pair<RobotNode, String>> stateActions = thisState.get_children();

            for (Pair<RobotNode, String> p : stateActions) {
                RobotNode child = p.getKey();
                String route = p.getValue();
                if (!visited.contains(child) && !toBeVisited.contains(child)) {
                    toBeVisited.add(child);
                    meta.put(child, new Pair<>(thisState, route));
                }
            }
        }
        return false;
    }

    /**
     * Solve the transition from a start robot config to a goal robot config.
     * @param current - The start robot config.
     * @param next - The next robot config.
     * @param environment - The environment.
     * @param result - The result.
     * @param maxCount - The max computation steps.
     * @return - True if solved, false otherwise.
     */
    public static boolean solve(RobotConfig current, RobotConfig next, Environment environment, List<String> result, int maxCount) {

        RobotNode initial = new RobotNode(environment, current, next);

        Map<RobotNode, Pair<RobotNode, String>> meta = new HashMap<>();
        Comparator<RobotNode> comparator = new RobotNodeComparator();
        PriorityQueue<RobotNode> toBeVisited = new PriorityQueue<>(11, comparator);
        Set<RobotNode> visited = new HashSet<>();

        meta.put(initial, new Pair<>(null, null));
        toBeVisited.add(initial);

        while(toBeVisited.size() != 0) {

            if (visited.size() > maxCount) {
                break;
            }

            RobotNode thisState = toBeVisited.remove();
            visited.add(thisState);

            if (thisState.is_goal()) {
                result.addAll(construct_robot_route(thisState, meta));
                return true;
            }

            List<Pair<RobotNode, String>> stateActions = thisState.get_children();

            for (Pair<RobotNode, String> p : stateActions) {
                RobotNode child = p.getKey();
                String route = p.getValue();
                if (!visited.contains(child) && !toBeVisited.contains(child)) {
                    toBeVisited.add(child);
                    meta.put(child, new Pair<>(thisState, route));
                }
            }
        }
        return false;
    }

    /**
     * Solve the transition from a start robot config to goal robot config quick.
     * @param current - Start config.
     * @param next - Goal config.
     * @param environment - Environment.
     * @return - True if solved, false otherwise.
     */
    public static boolean solve_quick(RobotConfig current, RobotConfig next, Environment environment) {

        RobotNode initial = new RobotNode(environment, current, next);
        Comparator<RobotNode> comparator = new RobotNodeComparator();
        PriorityQueue<RobotNode> toBeVisited = new PriorityQueue<>(11, comparator);
        Set<RobotNode> visited = new HashSet<>();
        toBeVisited.add(initial);

        while(toBeVisited.size() != 0) {

            if (visited.size() > 1000) {
                break;
            }

            RobotNode thisState = toBeVisited.remove();
            visited.add(thisState);

            if (thisState.is_goal()) {
                return true;
            }

            List<Pair<RobotNode, String>> stateActions = thisState.get_children();

            for (Pair<RobotNode, String> p : stateActions) {
                RobotNode child = p.getKey();
                if (!visited.contains(child) && !toBeVisited.contains(child)) {
                    toBeVisited.add(child);
                }
            }
        }
        return false;
    }
}