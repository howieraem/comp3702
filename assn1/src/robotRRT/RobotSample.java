package robotRRT;

import problem.*;
import utility.*;

/**
 * This class represents a sample in RRT.
 */
public class RobotSample implements Utility {

    /* The state of this sample. */
    public RobotConfig state;

    /* The parent of this sample. */
    public RobotSample parent;

    /**
     * Constructor.
     * @param state - The start state.
     */
    public RobotSample(RobotConfig state) {
        this.state = state;
        parent = null;
    }

    /**
     * Get the hash code of this sample.
     * @return - The hash code of this sample.
     */
    @Override
    public int hashCode() {
        int result = 0;
        result += state.getPos().getX() * 1010;
        result += state.getPos().getY() * 1020;
        result += state.getOrientation() * 1030;
        return result;
    }

    /**
     * Check if this state equals to given object.
     * @param o - The given object.
     * @return - True if equals, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof RobotSample) {
            RobotSample rs = (RobotSample) o;
            return is_two_robots_equal(state, rs.state);
        }
        return false;
    }
}
