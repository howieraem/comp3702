package boxRRT;

import java.util.*;
import problem.*;
import utility.*;

/**
 * This class represents a sample in RRT.
 */
public class BoxSample implements Utility {

    /* The state of this sample. */
    public List<Box> movingObjects;

    /* The parent of this sample. */
    public BoxSample parent;

    /**
     * Constructor.
     * @param movingObjects - The start state.
     */
    public BoxSample(List<Box> movingObjects) {
        this.movingObjects = movingObjects;
        parent = null;
    }

    /**
     * Get the hash code of this sample.
     * @return - The hash code of this sample.
     */
    @Override
    public int hashCode() {
        int result;
        result = 0;
        int i = 0;
        for (Box b : movingObjects) {
            result += (i + 1) * b.getPos().getX() * (10000 + 10 * i);
            result += (i + 1) * b.getPos().getY() * (10000 + 10 * i);
            i++;
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
        if (o instanceof BoxSample) {
            BoxSample bs = (BoxSample) o;
            List<Box> bsMovingObjects = bs.movingObjects;
            int size = movingObjects.size();
            for (int i = 0; i < size; i++) {
                if (is_two_pos_away(movingObjects.get(i).getPos(), bsMovingObjects.get(i).getPos())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}