package robotRRT;

import java.util.*;
import java.awt.geom.*;
import problem.*;
import utility.*;
import robotAstar.*;

/**
 * This class is used to solve robot move by RRT.
 */
public class RobotSampleRRT implements Utility {

    /* The sample step for RRT. */
    private double robotSampleStep;

    /* The square of the sample step. */
    private double robotSampleStepSquare;

    /* Random number generator. */
    private Random random;

    /* Start config. */
    private RobotSample start;

    /* Goal config. */
    private RobotSample goal;

    /* The environment. */
    private Environment environment;

    /* The robot width. */
    private double robotWidth;

    /**
     * Constructor.
     * @param start - The start config.
     * @param goal - The goal config.
     * @param environment - The environment.
     * @param step - The sampling step.
     */
    public RobotSampleRRT(RobotConfig start, RobotConfig goal, Environment environment, double step) {
        this.start = new RobotSample(start);
        this.goal = new RobotSample(goal);
        this.environment = environment;
        robotWidth = environment.movingObjects.get(0).getWidth();
        random = new Random();
        robotSampleStep = step;
        robotSampleStepSquare = step * step;
    }

    /**
     * Get a random sample from c-space.
     * @return A robot sample.
     */
    private RobotSample get_random_sample() {
        double x, y, z;
        x = (double)Math.round(random.nextDouble() * 1000d) / 1000d;
        y = (double)Math.round(random.nextDouble() * 1000d) / 1000d;
        z = (double)Math.round(random.nextDouble() * 1.571 * 1000d) / 1000d;
        return new RobotSample(new RobotConfig(new Point2D.Double(x, y), z));
    }

    /**
     * Get the distance square from sample 1 to sample 2.
     * @param s1 - Sample 1.
     * @param s2 - Sample 2.
     * @return - The distance square.
     */
    private static double distance2_between(RobotSample s1, RobotSample s2) {
        Point2D r1 = s1.state.getPos();
        Point2D r2 = s2.state.getPos();
        double o1 = s1.state.getOrientation();
        double o2 = s2.state.getOrientation();
        return Math.pow(r1.getX() - r2.getX(), 2) + Math.pow(r1.getY() - r2.getY(), 2) + Math.pow((o1 - o2) / 15.71, 2);
    }

    /**
     * Get the closest sample to the child in this graph.
     * @param graph - The graph.
     * @param child - The child sample.
     * @return - The closest sample.
     */
    private RobotSample get_closest(List<RobotSample> graph, RobotSample child) {

        double distance = 0;
        double temp;
        RobotSample result = null;
        for (RobotSample rs : graph) {
            if (result == null) {
                distance = distance2_between(child, rs);
                result = rs;
            } else {
                temp = distance2_between(child, rs);
                if (temp < distance) {
                    distance = temp;
                    result = rs;
                }
            }
        }
        return result;
    }

    /**
     * Extend from Sample-from to Sample-to
     * @param from - Sample-from.
     * @param to - Sample-to.
     * @return - The most further sample.
     */
    private RobotSample extend (RobotSample from, RobotSample to) {
        double distance = Math.sqrt(distance2_between(from, to));

        double x1 = from.state.getPos().getX();
        double y1 = from.state.getPos().getY();
        double z1 = from.state.getOrientation();
        double x2 = to.state.getPos().getX();
        double y2 = to.state.getPos().getY();
        double z2 = to.state.getOrientation();

        double x3, y3, z3;

        RobotSample current;
        RobotSample previous = null;

        for (int i = 0; i < 10; i++) {
            x3 = Math.round((x1 - (1 - 0.1 * i) * (x1 - x2) * robotSampleStep / distance) * 1000d) / 1000d;
            y3 = Math.round((y1 - (1 - 0.1 * i) * (y1 - y2) * robotSampleStep / distance) * 1000d) / 1000d;
            z3 = Math.round((z1 - (1 - 0.1 * i) * (z1 - z2) * robotSampleStep / distance) * 1000d) / 1000d;
            current = new RobotSample(new RobotConfig(new Point2D.Double(x3, y3), z3));
            if (noCollisionForAll(environment, environment, current.state, robotWidth)) {
                previous = current;
            } else {
                break;
            }
        }
        return previous;
    }

    /**
     * Check if two samples can connect.
     * @param from - Sample-from.
     * @param to - Sample-to.
     * @return - true if can connect, false otherwise.
     */
    private boolean can_connect(RobotSample from, RobotSample to) {
        double distance = Math.sqrt(distance2_between(from, to));

        double x1 = from.state.getPos().getX();
        double y1 = from.state.getPos().getY();
        double z1 = from.state.getOrientation();
        double x2 = to.state.getPos().getX();
        double y2 = to.state.getPos().getY();
        double z2 = to.state.getOrientation();

        double x3, y3, z3;

        RobotSample result = new RobotSample(null);
        /* Check middle 10 points. */
        for (int i = 0; i < 10; i++) {
            x3 = Math.round((x1 - (1 - 0.1 * i) * (x1 - x2) * robotSampleStep / distance) * 1000d) / 1000d;
            y3 = Math.round((y1 - (1 - 0.1 * i) * (y1 - y2) * robotSampleStep / distance) * 1000d) / 1000d;
            z3 = Math.round((z1 - (1 - 0.1 * i) * (z1 - z2) * robotSampleStep / distance) * 1000d) / 1000d;
            result.state = new RobotConfig(new Point2D.Double(x3, y3), z3);
            if (!noCollisionForAll(environment, environment, result.state, robotWidth)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Construct list of robot config that needs to be solved by robot astar.
     * @return - A list of robot config that needs to be solved by robot astar.
     */
    private List<RobotConfig> construct_robot_route() {
        Stack<RobotConfig> actions = new Stack<>();
        List<RobotConfig> result = new ArrayList<>();
        RobotSample rs = goal;
        while (rs.parent != null) {
            actions.push(rs.state);
            rs = rs.parent;
        }
        int size = actions.size();
        for (int i = 0; i < size; i++) {
            result.add(actions.pop());
        }
        return result;
    }

    /**
     * Solve the RRT problem.
     * @param maxGraphSize - The max graph size RRT can explore.
     * @return - A list of environments that needs to be solved by box node.
     */
    public List<RobotConfig> solve(int maxGraphSize) {
        List<RobotSample> graph = new ArrayList<>();
        graph.add(start);
        boolean notReachGoal = true;
        if (distance2_between(start, goal) < robotSampleStepSquare) {
            goal.parent = start;
            notReachGoal = false;
        }
        int count = 0;
        while (notReachGoal) {
            if (count++ > maxGraphSize) {
                System.out.println("RRT exceed max computation time, exit.");
                print_data(graph);
                return null;
            }
            RobotSample thisSample = get_random_sample();
            if (!graph.contains(thisSample)) {
                RobotSample closest = get_closest(graph, thisSample);

                if (distance2_between(closest, thisSample) > robotSampleStepSquare) {
                    RobotSample temp = extend(closest, thisSample);
                    if (temp == null) continue;
                    thisSample = temp;
                }
                if (can_connect(closest, thisSample)) {
                    thisSample.parent = closest;
                    graph.add(thisSample);
                    if (distance2_between(thisSample, goal) < robotSampleStepSquare &&
                            RobotNodeAstar.solve_quick(thisSample.state, goal.state, environment)) {
                        goal.parent = thisSample;
                        notReachGoal = false;
                    }
                }
            }
        }
        System.out.printf("RRT solved with %d samples.\n", graph.size());
        return construct_robot_route();
    }
}