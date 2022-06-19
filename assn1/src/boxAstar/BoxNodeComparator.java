package boxAstar;

import java.util.Comparator;

/**
 * Used for comparing two box nodes.
 */
public class BoxNodeComparator implements Comparator<BoxNode> {

    /**
     * Compare two box nodes.
     * @param bn1 - box node 1.
     * @param bn2 - box node 2.
     * @return - 1 if bn1 > bn2, 0 if equals, -1 if bn1 < bn2.
     */
    public int compare(BoxNode bn1, BoxNode bn2) {
        if (bn1.cost() < bn2.cost()) {
            return -1;
        } else if (bn1.cost() > bn2.cost()) {
            return 1;
        } else {
            return 0;
        }
    }
}
