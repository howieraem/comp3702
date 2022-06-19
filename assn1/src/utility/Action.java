package utility;

import problem.*;

/**
 * This class represents an action for robot to solve.
 */
public class Action {

    /* The environment of operation. */
    public Environment environment;

    /* The index of moved box in this action. */
    public int movedBox;

    /* The direction of the action. */
    public Direction direction;

    /* The position where the push starts. */
    public RobotConfig pushPosition;

    /* The position where the push ends. */
    public RobotConfig afterPushPosition;

    /**
     * Constructor.
     * @param environment - The environment of operation.
     * @param movedBox - The index of moved box in this action.
     * @param direction - The direction of the action.
     * @param pushPosition - The position where the push starts.
     * @param afterPushPosition - The position where the push ends.
     */
    public Action(Environment environment, int movedBox, Direction direction,
                  RobotConfig pushPosition, RobotConfig afterPushPosition) {
        this.environment = environment;
        this.movedBox = movedBox;
        this.direction = direction;
        this.pushPosition = pushPosition;
        this.afterPushPosition = afterPushPosition;
    }
}
