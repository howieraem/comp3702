package robotAstar;

import java.util.*;
import java.awt.geom.*;
import javafx.util.Pair;
import problem.*;
import utility.*;
import static utility.Direction.*;

/**
 * This class represents a node in robot astar search.
 */
public class RobotNode implements Utility {

    /* The width of the robot. */
    private double robotWidth;

    /* The minimumStep of the angle. */
    private double minimumStep;

    /* The current robot config. */
    public RobotConfig current;

    /* The goal robot config. */
    private RobotConfig goal;

    /* The environment. */
    private Environment environment;

    /* The cost to goal of this state. */
    private double cost;

    /**
     * Constructor.
     * @param environment - Environment.
     * @param current - The current robot config.
     * @param goal - The goal robot config.
     */
    public RobotNode(Environment environment, RobotConfig current, RobotConfig goal) {
        this.environment = environment;
        this.current =current;
        this.goal = goal;
        robotWidth = environment.movingObjects.get(0).getWidth();
        minimumStep = Math.round(Math.acos(1 - 2 * UNIT_STEP * UNIT_STEP / (robotWidth * robotWidth)) * 1000d) / 1000d - UNIT_STEP;
        if (minimumStep < 0.001) {
            minimumStep = 0.001;
        }
        update_cost();
    }

    /**
     * Get the cost of this state.
     * @return - The cost of this state.
     */
    public double cost() {
        return cost;
    }

    /**
     * Update the cost of this state, called in constructor.
     */
    private void update_cost() {
        cost = 0;
        cost += DISTANCE_WEIGHT * Math.sqrt(Math.pow((this.current.getPos().getX() - this.goal.getPos().getX()), 2) + Math.pow((this.current.getPos().getY() - this.goal.getPos().getY()), 2));
        cost += ANGLE_WEIGHT * Math.abs(this.current.getOrientation() - this.goal.getOrientation());
    }

    /**
     * Get the hash code for this class.
     * @return - The hash code.
     */
    @Override
    public int hashCode() {
        int result = 0;
        result += current.getPos().getX() * 1010;
        result += current.getPos().getY() * 1020;
        result += current.getOrientation() * 1030;
        return result;
    }

    /**
     * Check if this state equals to given object.
     * @param o - The given object.
     * @return - True if equals, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof RobotNode) {
            RobotNode rn = (RobotNode) o;
            return is_two_robots_equal(current, rn.current);
        }
        return false;
    }

    /**
     * Check if this node is goal.
     * @return - True if it is, false otherwise.
     */
    public boolean is_goal() {
        return is_two_robots_equal(current, goal);
    }

    /**
     * Get the action in output string format.
     * @param thisX - X position.
     * @param thisY - Y position.
     * @param thisA - Angle position.
     * @param environment - Environment.
     * @return - Action in string.
     */
    private String get_action_in_string(double thisX, double thisY, double thisA, Environment environment) {
        String action = String.format("%.3f %.3f %.3f", thisX, thisY, thisA);
        for (Box box : environment.movingObjects) {
            double width = box.getWidth();
            action = action.concat(String.format(" %.3f %.3f", box.getPos().getX() + 0.5 * width, box.getPos().getY() + 0.5 * width));
        }
        return action;
    }

    /**
     * Set the robot further in the given direction, used in get child action of astar.
     * @param environment - The environment.
     * @param direction - The given direction.
     * @param result - A result of robotNode and corresponding action.
     */
    private void walk_further(Environment environment, Direction direction, List<Pair<RobotNode, String>> result) {
        double startX = current.getPos().getX();
        double startY = current.getPos().getY();
        double startA = current.getOrientation();
        double thisX = startX;
        double thisY = startY;
        double thisA = startA;

        switch(direction) {
            case UP:
                thisY += UNIT_STEP;
                break;
            case DOWN:
                thisY -= UNIT_STEP;
                break;
            case LEFT:
                thisX -= UNIT_STEP;
                break;
            case RIGHT:
                thisX += UNIT_STEP;
                break;
            case ANTI_CLOCKWISE:;
                thisA += minimumStep;
                if (thisA - minimumStep < goal.getOrientation() && thisA > goal.getOrientation()) {
                    thisA = goal.getOrientation();
                }
                break;
            case CLOCK_WISE:
                thisA -= minimumStep;
                if (thisA + minimumStep > goal.getOrientation() && thisA < goal.getOrientation()) {
                    thisA = goal.getOrientation();
                }
                break;
        }

        RobotConfig rc = new RobotConfig(new Point2D.Double(thisX, thisY), thisA);

        if (noCollisionForAll(environment, environment, rc, robotWidth)) {
            String s = get_action_in_string(thisX, thisY, thisA, environment);
            result.add(new Pair<>(new RobotNode(environment, rc, goal), s));
        }
    }

    /**
     * Get the children of this state.
     * @return - A list of robotNode and corresponding action.
     */
    public List<Pair<RobotNode, String>> get_children() {

        List<Pair<RobotNode, String>> result = new ArrayList<>();

        walk_further(environment, UP, result);
        walk_further(environment, DOWN, result);
        walk_further(environment, LEFT, result);
        walk_further(environment, RIGHT, result);
        walk_further(environment, CLOCK_WISE, result);
        walk_further(environment, ANTI_CLOCKWISE, result);

        return result;
    }
}