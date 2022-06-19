package boxAstar;

import java.util.*;
import java.awt.geom.*;
import javafx.util.Pair;
import problem.*;
import utility.*;
import static utility.Direction.*;

/**
 * This class represents a node in box astar search.
 */
public class BoxNode implements Utility {

    /* The current environment. */
    private Environment state;

    /* The goal environment. */
    private Environment goal;

    /* Which box can be moved in this environment. */
    int moved;

    /* The cost to goal of this state. */
    private double cost;

    /**
     * Constructor.
     * @param state - Start environment.
     * @param goal - Goal environment.
     * @param moved - The box can be moved.
     */
    public BoxNode(Environment state, Environment goal, int moved) {
        this.state = state;
        this.goal = goal;
        this.moved = moved;
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
        List<Box> stateMovingObjects = state.movingObjects;
        List<Box> goalMovingObjects = goal.movingObjects;
        int size = stateMovingObjects.size();
        for (int i = 0; i < size; i++) {
            if (moved == i) {
                cost += 10000 * Math.pow(stateMovingObjects.get(i).getPos().getX() - goalMovingObjects.get(i).getPos().getX(), 2);
                cost += 10000 * Math.pow(stateMovingObjects.get(i).getPos().getY() - goalMovingObjects.get(i).getPos().getY(), 2);
            }
        }
    }

    /**
     * Get the hash code for this class.
     * @return - The hash code.
     */
    @Override
    public int hashCode() {
        int result;
        result = 0;
        List<Box> stateMovingObjects = state.movingObjects;
        int size = stateMovingObjects.size();
        for (int i = 0; i < size; i++) {
            if (moved == i) {
                result += (i + 1) * stateMovingObjects.get(i).pos.getX() * (10000 + 10 * i);
                result += (i + 1) * stateMovingObjects.get(i).pos.getY() * (10000 + 10 * i);
            }
        }
        return result;
    }

    /**
     * Check if this state equals to given object.
     * @param o - The given object.
     * @return - True if equals, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof BoxNode) {
            BoxNode bn = (BoxNode) o;
            List<Box> stateMovingObjects = state.movingObjects;
            List<Box> bnMovingObjects = bn.state.movingObjects;
            int size = stateMovingObjects.size();
            for (int i = 0; i < size; i++) {
                if (moved == i && is_two_pos_away(stateMovingObjects.get(i).getPos(), bnMovingObjects.get(i).getPos())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Check if this node is goal.
     * @return - True if it is, false otherwise.
     */
    public boolean is_goal() {
        List<Box> stateMovingObjects = state.movingObjects;
        List<Box> goalMovingObjects = goal.movingObjects;
        int size = stateMovingObjects.size();
        /* Only check the box that can be moved. */
        for (int i = 0; i < size; i++) {
            if (moved == i && is_two_pos_away(stateMovingObjects.get(i).getPos(), goalMovingObjects.get(i).getPos())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Set the box to have the new position in the given environment.
     * @param environment - The given environment.
     * @param index - The index of the box that to be set.
     * @param newPos - The new position of the box.
     */
    private void set_box(Environment environment, int index, Point2D newPos) {
        List<Box> movingObjects = environment.movingObjects;
        Box box = movingObjects.get(index);
        if (box instanceof MovingBox) {
            movingObjects.set(index, new MovingBox(newPos, movingObjects.get(index).getWidth()));
        } else {
            movingObjects.set(index, new MovingObstacle(newPos, movingObjects.get(index).getWidth()));
        }
    }

    /**
     * Set the box further in the given direction, used in get child action of astar.
     * @param index - The index of the moved box.
     * @param direction - The given direction.
     * @param result - A result of boxNode and corresponding action.
     */
    private void set_further(int index, Direction direction, List<Pair<BoxNode, Action>> result) {

        Environment oldEnvironment = new Environment(state.movingObjects, state.staticObstacles);
        Environment newEnvironment = new Environment(state.movingObjects, state.staticObstacles);

        double startX = state.movingObjects.get(index).getPos().getX();
        double startY = state.movingObjects.get(index).getPos().getY();

        RobotConfig robotBefore = null;
        RobotConfig robotAfter = null;

        double width = state.movingObjects.get(moved).getWidth();

        /* The step size in box node is at default minimum step, 0.001. */
        switch (direction) {
            case UP:
                robotBefore = new RobotConfig(new Point2D.Double(startX + 0.5 * width, startY), 0);
                set_box(newEnvironment, index, new Point2D.Double(startX, startY + 0.001));
                robotAfter = new RobotConfig(new Point2D.Double(startX + 0.5 * width, startY + 0.001), 0);
                break;
            case DOWN:
                robotBefore = new RobotConfig(new Point2D.Double(startX + 0.5 * width, startY + width), 0);
                set_box(newEnvironment, index, new Point2D.Double(startX, startY - 0.001));
                robotAfter = new RobotConfig(new Point2D.Double(startX + 0.5 * width, startY + width - 0.001), 0);
                break;
            case LEFT:
                robotBefore = new RobotConfig(new Point2D.Double(startX + width, startY + 0.5 * width), 1.571);
                set_box(newEnvironment, index, new Point2D.Double(startX - 0.001, startY));
                robotAfter = new RobotConfig(new Point2D.Double(startX + width - 0.001, startY + 0.5 * width), 1.571);
                break;
            case RIGHT:
                robotBefore = new RobotConfig(new Point2D.Double(startX, startY + 0.5 * width), 1.571);
                set_box(newEnvironment, index, new Point2D.Double(startX + 0.001, startY));
                robotAfter = new RobotConfig(new Point2D.Double(startX + 0.001, startY + 0.5 * width), 1.571);
                break;
        }

        if (noCollisionForAll(oldEnvironment, newEnvironment, robotBefore, width)) {
            BoxNode newBn = new BoxNode(newEnvironment, goal, moved);
            Action action = new Action(oldEnvironment, index, direction, robotBefore, robotAfter);
            result.add(new Pair<>(newBn, action));
        }
    }

    /**
     * Get the children of this state.
     * @return - A list of BoxNode and corresponding action.
     */
    public List<Pair<BoxNode, Action>> get_children() {
        List<Pair<BoxNode, Action>> result = new ArrayList<>();
        int size = state.movingObjects.size();

        /* For each of the box, if it is in the moved list, get child. */
        for (int i = 0; i < size; i++) {
            if (moved == i) {
                set_further(i, UP, result);
                set_further(i, DOWN, result);
                set_further(i, LEFT, result);
                set_further(i, RIGHT, result);
            }
        }
        return result;
    }
}
