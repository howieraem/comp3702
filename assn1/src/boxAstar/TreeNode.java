package boxAstar;

import java.util.*;
import java.awt.geom.*;
import javafx.util.Pair;
import problem.*;
import utility.*;
import static utility.Direction.*;

/**
 * This class represents a node in environment search.
 */
public class TreeNode implements Utility {

    /* The current environment. */
    public Environment state;

    /* The goals position for all moving boxes. */
    private List<Point2D> goals;

    /* The cost to goal of this state. */
    private double cost;

    /**
     * Constructor.
     * @param state - Start environment.
     * @param goals - The goals position for all moving boxes.
     */
    public TreeNode(Environment state, List<Point2D> goals) {
        this.state = state;
        this.goals = goals;
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
        List<Box> movingObjects = state.movingObjects;
        int goalSize = goals.size();
        for (int i = 0; i < goalSize; i++) {
            /* First compute total distance from each box to its goal. */
            Box mb = movingObjects.get(i);
            Point2D g = goals.get(i);
            cost += BOX_WEIGHT * Math.sqrt(Math.pow((mb.pos.getX() - g.getX()), 2) +
                    Math.pow((mb.pos.getY() - g.getY()), 2));
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
        List<Box> movingObjects = state.movingObjects;
        int size = movingObjects.size();
        for (int i = 0; i < size; i++) {
            result += (i + 1) * movingObjects.get(i).pos.getX() * (10000 + 10 * i);
            result += (i + 1) * movingObjects.get(i).pos.getY() * (10000 + 10 * i);
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
        if (o instanceof TreeNode) {
            TreeNode tn = (TreeNode) o;
            List<Box> stateMovingObjects = state.movingObjects;
            List<Box> tnMovingObjects = tn.state.movingObjects;
            int size = stateMovingObjects.size();
            for (int i = 0; i < size; i++) {
                if (is_two_pos_away(stateMovingObjects.get(i).getPos(), tnMovingObjects.get(i).getPos())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Get step size based on phase.
     * @param phase - The phase of this search.
     * @param boxWidth - The width of the box.
     * @return - The step size.
     */
    private double get_step_size(Phase phase, double boxWidth) {
        double step = boxWidth;
        switch (phase) {
            case PHASE_1:
                step = boxWidth;
                break;
            case PHASE_2:
                step = boxWidth / 2;
                break;
            case PHASE_3:
                step = boxWidth / 4;
                break;
            case PHASE_4:
                step = boxWidth / 8;
                break;
            case PHASE_5:
                step = boxWidth / 16;
                break;
            case PHASE_6:
                step = UNIT_STEP;
                break;
        }

        step = Math.round(step * 1000.0) / 1000.0;

        /* Step can not less than unit step. */
        if (step < UNIT_STEP) {
            step = UNIT_STEP;
        }
        return step;
    }

    /**
     * Check if this node is goal.
     * @return - True if it is, false otherwise.
     */
    public boolean is_goal(Phase phase) {
        List<Box> stateMovingObjects = this.state.movingObjects;
        int size = goals.size();
        double step = this.get_step_size(phase, stateMovingObjects.get(0).getWidth());
        for (int i = 0; i < size; i++) {
            if (is_two_pos_away(stateMovingObjects.get(i).getPos(), goals.get(i), 0.5 * step)) {
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
     * @param result - A result of TreeNode and corresponding action.
     * @param phase - The phase of the search.
     */
    private void set_further(int index, Direction direction, List<Pair<TreeNode, Action>> result, Phase phase) {

        Environment oldEnvironment = new Environment(state.movingObjects, state.staticObstacles);
        Environment newEnvironment = new Environment(state.movingObjects, state.staticObstacles);

        double startX = state.movingObjects.get(index).getPos().getX();
        double startY = state.movingObjects.get(index).getPos().getY();

        RobotConfig robotBefore = null;
        RobotConfig robotAfter = null;
        RobotConfig robotBackUp = null;

        double width = state.movingObjects.get(index).getWidth();
        double step = get_step_size(phase, state.movingObjects.get(index).getWidth());

        /* The step size depends on the phase of the search. */
        switch (direction) {
            case UP:
                robotBefore = new RobotConfig(new Point2D.Double(startX + 0.5 * width, startY), 0);
                robotBackUp = new RobotConfig(new Point2D.Double(startX + 0.5 * width, startY - SPACE_LEFT * UNIT_STEP), 0);
                set_box(newEnvironment, index, new Point2D.Double(startX, startY + step));
                robotAfter = new RobotConfig(new Point2D.Double(startX + 0.5 * width, startY + step), 0);
                break;
            case DOWN:
                robotBefore = new RobotConfig(new Point2D.Double(startX + 0.5 * width, startY + width), 0);
                robotBackUp = new RobotConfig(new Point2D.Double(startX + 0.5 * width, startY + width +  SPACE_LEFT * UNIT_STEP), 0);
                set_box(newEnvironment, index, new Point2D.Double(startX, startY - step));
                robotAfter = new RobotConfig(new Point2D.Double(startX + 0.5 * width, startY + width - step), 0);
                break;
            case LEFT:
                robotBefore = new RobotConfig(new Point2D.Double(startX + width, startY + 0.5 * width), 1.571);
                robotBackUp = new RobotConfig(new Point2D.Double(startX + width + SPACE_LEFT * UNIT_STEP, startY + 0.5 * width), 1.571);
                set_box(newEnvironment, index, new Point2D.Double(startX - step, startY));
                robotAfter = new RobotConfig(new Point2D.Double(startX + width - step, startY + 0.5 * width), 1.571);
                break;
            case RIGHT:
                robotBefore = new RobotConfig(new Point2D.Double(startX, startY + 0.5 * width), 1.571);
                robotBackUp = new RobotConfig(new Point2D.Double(startX + width - SPACE_LEFT * UNIT_STEP, startY + 0.5 * width), 1.571);
                set_box(newEnvironment, index, new Point2D.Double(startX + step, startY));
                robotAfter = new RobotConfig(new Point2D.Double(startX + step, startY + 0.5 * width), 1.571);
                break;
        }

        if (noCollisionForAll(oldEnvironment, newEnvironment, robotBefore, width) && noCollisionForAll(oldEnvironment, newEnvironment, robotBackUp, width)) {
            TreeNode newTn = new TreeNode(newEnvironment, goals);
            Action action = new Action(oldEnvironment, index, direction, robotBefore, robotAfter);
            result.add(new Pair<>(newTn, action));
        }
    }

    /**
     * Get the children of this state.
     * @return - A list of TreeNode and corresponding action.
     */
    public List<Pair<TreeNode, Action>> get_children(Phase phase) {
        List<Pair<TreeNode, Action>> result = new ArrayList<>();
        int size = state.movingObjects.size();

        /* For each of the box, get child. */
        for (int i = 0; i < size; i++) {
            set_further(i, UP, result, phase);
            set_further(i, DOWN, result, phase);
            set_further(i, LEFT, result, phase);
            set_further(i, RIGHT, result, phase);
        }
        return result;
    }
}
