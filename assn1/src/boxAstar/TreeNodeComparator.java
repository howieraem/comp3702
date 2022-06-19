package boxAstar;

import java.util.Comparator;

/**
 * Used for comparing two tree nodes.
 */
public class TreeNodeComparator implements Comparator<TreeNode> {

    /**
     * Compare two tree nodes.
     * @param bn1 - tree node 1.
     * @param bn2 - tree node 2.
     * @return - 1 if bn1 > bn2, 0 if equals, -1 if bn1 < bn2.
     */
    public int compare(TreeNode bn1, TreeNode bn2) {
        if (bn1.cost() < bn2.cost()) {
            return -1;
        } else if (bn1.cost() > bn2.cost()) {
            return 1;
        } else {
            return 0;
        }
    }
}
