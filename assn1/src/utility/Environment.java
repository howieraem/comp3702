package utility;

import java.util.*;
import problem.*;

/**
 * This class represents a state of environment.
 */
public class Environment {

    /* All moving objects in this environment. */
    public List<Box> movingObjects;

    /* All static obstacles in this environment. */
    public List<StaticObstacle> staticObstacles;

    /**
     * Constructor.
     * @param movingObjects - All moving objects in this environment.
     * @param staticObstacles - All static obstacles in this environment.
     */
    public Environment(List<Box> movingObjects, List<StaticObstacle> staticObstacles) {
        this.movingObjects = new ArrayList<>();
        for (Box b : movingObjects) {
            if (b instanceof MovingBox) {
                this.movingObjects.add(new MovingBox(b.getPos(), b.getWidth()));
            } else {
                this.movingObjects.add(new MovingObstacle(b.getPos(), b.getWidth()));
            }
        }
        this.staticObstacles = staticObstacles;
    }
}
