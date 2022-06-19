package robotAstar;

import java.util.Comparator;

/**
 * Used for comparing two robot nodes.
 */
public class RobotNodeComparator implements Comparator<RobotNode> {

    /**
     * Compare two robot nodes.
     * @param bn1 - robot node 1.
     * @param bn2 - robot node 2.
     * @return - 1 if bn1 > bn2, 0 if equals, -1 if bn1 < bn2.
     */
    public int compare(RobotNode bn1, RobotNode bn2) {
        if (bn1.cost() < bn2.cost()) {
            return -1;
        } else if (bn1.cost() > bn2.cost()) {
            return 1;
        } else {
            return 0;
        }
    }
}