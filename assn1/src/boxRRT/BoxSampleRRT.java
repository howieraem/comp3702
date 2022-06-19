package boxRRT;

import java.util.*;
import java.awt.geom.*;
import javafx.util.Pair;
import problem.*;
import utility.*;

/**
 * This class is used to solve box move by RRT.
 */
public class BoxSampleRRT implements Utility {

    /* The sample step for RRT. */
    private double boxSampleStep;

    /* The square of the sample step. */
    private double boxSampleStepSquare;

    /* Random number generator. */
    private Random random;

    /* Start config. */
    private BoxSample start;

    /* Start environment. */
    private Environment startEnvironment;

    /* The boxes that can be moved. */
    private List<Integer> moved;

    /* The goal of the moved box. */
    private Point2D goal;

    /* Used for feedback purpose. */
    private Map<Box, Integer> boxCollisionCount;

    /**
     * Constructor.
     * @param moved - The boxes that can be moved (Moved size is used at most with size 2).
     * @param startEnvironment - The start environment.
     * @param goals - The goals.
     * @param step - The sampling step.
     */
    public BoxSampleRRT(List<Integer> moved, Environment startEnvironment, List<Point2D> goals, double step) {
        List<Box> movingObjects = new ArrayList<>();
        List<Box> environmentObjects = startEnvironment.movingObjects;
        for (int index : moved) {
            movingObjects.add(environmentObjects.get(index));
        }
        this.start = new BoxSample(movingObjects);
        this.startEnvironment = startEnvironment;
        this.moved = moved;
        this.goal = goals.get(moved.get(0));
        random = new Random();
        boxSampleStep = step;
        boxSampleStepSquare = step * step;
        boxCollisionCount = new HashMap<>();

        int size = environmentObjects.size();
        for (int i = 0; i < size; i++) {
            if (!moved.contains(i)) {
                boxCollisionCount.put(environmentObjects.get(i), 0);
            }
        }
    }

    /**
     * Get a ramdom sample from c-space.
     * @return A box sample.
     */
    private BoxSample get_random_sample() {
        List<Box> objects;
        List<Box> movedObjects;
        int size = startEnvironment.movingObjects.size();
        objects = new ArrayList<>();
        movedObjects = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (moved.contains(i)) {
                Box box = startEnvironment.movingObjects.get(i);
                double boxWidth = box.getWidth();
                double x = (double) Math.round(random.nextDouble() * 1000d) / 1000d;
                double y = (double) Math.round(random.nextDouble() * 1000d) / 1000d;
                if (box instanceof MovingBox) {
                    MovingBox mb = new MovingBox(new Point2D.Double(x, y), boxWidth);
                    movedObjects.add(mb);
                    objects.add(mb);
                } else {
                    MovingObstacle mo = new MovingObstacle(new Point2D.Double(x, y), boxWidth);
                    movedObjects.add(mo);
                    objects.add(mo);
                }
            } else {
                objects.add(startEnvironment.movingObjects.get(i));
            }
        }
        return new BoxSample(movedObjects);
    }

    /**
     * Get the distance square from sample 1 to sample 2.
     * @param s1 - Sample 1.
     * @param s2 - Sample 2.
     * @return - The distance square.
     */
    private static double distance2_between(BoxSample s1, BoxSample s2) {
        double result = 0;
        int size = s1.movingObjects.size();
        List<Box> s1box = s1.movingObjects;
        List<Box> s2box = s2.movingObjects;
        for (int i = 0; i < size; i++) {
            Point2D pos1 = s1box.get(i).getPos();
            Point2D pos2 = s2box.get(i).getPos();
            result += Math.pow(pos1.getX() - pos2.getX(), 2);
            result += Math.pow(pos1.getY() - pos2.getY(), 2);
        }
        return result;
    }

    /**
     * Get the problem box.
     * @return - The problem box.
     */
    public Pair<Box, Integer> get_problemBox() {
        Box problemBox = null;
        int maxCount = 0;
        for (Box b : boxCollisionCount.keySet()) {
            if (boxCollisionCount.get(b) > maxCount) {
                problemBox = b;
                maxCount = boxCollisionCount.get(b);
            }
        }
        return new Pair<>(problemBox, maxCount);
    }

    /**
     * Get the closest sample to the child in this graph.
     * @param graph - The graph.
     * @param child - The child sample.
     * @return - The closest sample.
     */
    private BoxSample get_closest(List<BoxSample> graph, BoxSample child) {
        double distance = 0;
        double temp;
        BoxSample result = null;
        for (BoxSample sn : graph) {
            if (result == null) {
                distance = distance2_between(child, sn);
                result = sn;
            } else {
                temp = distance2_between(child, sn);
                if (temp < distance) {
                    distance = temp;
                    result = sn;
                }
            }
        }
        return result;
    }

    /**
     * Extend from Sample-from to Sample-to
     * @param from - Sample-from.
     * @param to - Sample-to.
     * @return - The most further sample.
     */
    private BoxSample extend(BoxSample from, BoxSample to) {
        double distance = Math.sqrt(distance2_between(from, to));
        List<Box> objects;
        List<Box> currentMovedObjects;
        List<Box> previousMovedObjects = null;
        int size = startEnvironment.movingObjects.size();
        for (int j = 0; j < 10; j++) {
            objects = new ArrayList<>();
            currentMovedObjects = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                if (moved.contains(i)) {
                    Box boxFrom = from.movingObjects.get(moved.indexOf(i));
                    Box boxTo = to.movingObjects.get(moved.indexOf(i));
                    double boxWidth = boxFrom.getWidth();
                    double x1 = boxFrom.getPos().getX();
                    double y1 = boxFrom.getPos().getY();
                    double x2 = boxTo.getPos().getX();
                    double y2 = boxTo.getPos().getY();
                    double x3 = Math.round((x1 - (1 - 0.1 * j) * (x1 - x2) * boxSampleStep / distance) * 1000d) / 1000d;
                    double y3 = Math.round((y1 - (1 - 0.1 * j) * (y1 - y2) * boxSampleStep / distance) * 1000d) / 1000d;
                    if (boxFrom instanceof MovingBox) {
                        MovingBox mb = new MovingBox(new Point2D.Double(x3, y3), boxWidth);
                        currentMovedObjects.add(mb);
                        objects.add(mb);
                    } else {
                        MovingObstacle mo = new MovingObstacle(new Point2D.Double(x3, y3), boxWidth);
                        currentMovedObjects.add(mo);
                        objects.add(mo);
                    }
                } else {
                    objects.add(startEnvironment.movingObjects.get(i));
                }
            }
            if (noCollisionForBoxStrictWithFeedback(objects, startEnvironment.staticObstacles, boxCollisionCount)) {
                previousMovedObjects = currentMovedObjects;
            } else {
                break;
            }
        }
        if (previousMovedObjects == null) {
            return null;
        }
        return new BoxSample(previousMovedObjects);
    }

    /**
     * Check if two samples can connect.
     * @param from - Sample-from.
     * @param to - Sample-to.
     * @return - true if can connect, false otherwise.
     */
    private boolean can_connect(BoxSample from, BoxSample to) {
        double distance = Math.sqrt(distance2_between(from, to));
        List<Box> objects;
        int size = startEnvironment.movingObjects.size();
        /* Check middle 10 points. */
        for (int j = 0; j < 10; j++) {
            objects = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                if (moved.contains(i)) {
                    Box boxFrom = from.movingObjects.get(moved.indexOf(i));
                    Box boxTo = to.movingObjects.get(moved.indexOf(i));
                    double boxWidth = boxFrom.getWidth();
                    double x1 = boxFrom.getPos().getX();
                    double y1 = boxFrom.getPos().getY();
                    double x2 = boxTo.getPos().getX();
                    double y2 = boxTo.getPos().getY();
                    double x3 = Math.round((x1 - (1 - 0.1 * j) * (x1 - x2) * boxSampleStep / distance) * 1000d) / 1000d;
                    double y3 = Math.round((y1 - (1 - 0.1 * j) * (y1 - y2) * boxSampleStep / distance) * 1000d) / 1000d;
                    if (boxFrom instanceof MovingBox) {
                        MovingBox mb = new MovingBox(new Point2D.Double(x3, y3), boxWidth);
                        objects.add(mb);
                    } else {
                        MovingObstacle mo = new MovingObstacle(new Point2D.Double(x3, y3), boxWidth);
                        objects.add(mo);
                    }
                } else {
                    objects.add(startEnvironment.movingObjects.get(i));
                }
            }
            if (!noCollisionForBoxStrictWithFeedback(objects, startEnvironment.staticObstacles,boxCollisionCount)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a given sample is goal.
     * @param sn - The given sample.
     * @return - true if it is goal, false otherwise.
     */
    private boolean is_goal(BoxSample sn) {
        List<Box> objects = sn.movingObjects;
        Point2D boxPos = objects.get(0).getPos();
        if (is_two_pos_away(boxPos, goal, 0.005)) {
            return false;
        }
        return true;
    }

    /**
     * Construct list of environments that needs to be solved by box node.
     * @param bs - The goal sample.
     * @return - A list of environments that needs to be solved by box node.
     */
    private List<Environment> construct_box_route(BoxSample bs) {
        Stack<List<Box>> actions = new Stack<>();
        List<List<Box>> result = new ArrayList<>();
        BoxSample rs = bs;
        while (rs.parent != null) {
            actions.push(rs.movingObjects);
            rs = rs.parent;
        }
        int actionSize = actions.size();
        for (int i = 0; i < actionSize; i++) {
            result.add(actions.pop());
        }
        /* Make result to a list of environment. */
        List<Environment> totalRoute = new ArrayList<>();
        int boxSize = startEnvironment.movingObjects.size();
        for (int i = 0; i < actionSize; i++) {
            List<Box> objects = new ArrayList<>();
            for (int j = 0; j < boxSize; j++) {
                if (moved.contains(j)) {
                    objects.add(result.get(i).get(moved.indexOf(j)));
                } else {
                    objects.add(startEnvironment.movingObjects.get(j));
                }
            }
            totalRoute.add(new Environment(objects, startEnvironment.staticObstacles));
        }

        /* Add final environment. */
        List<Box> finalObjects = new ArrayList<>();
        List<Box> objects = startEnvironment.movingObjects;
        for (int i = 0; i < objects.size(); i++) {
            if (i == moved.get(0)) {
                finalObjects.add(new MovingBox(goal, objects.get(i).getWidth()));
            } else {
                finalObjects.add(objects.get(i));
            }
        }
        totalRoute.add(new Environment(finalObjects, startEnvironment.staticObstacles));
        return totalRoute;
    }

    /**
     * Solve the RRT problem.
     * @param maxGraphSize - The max graph size RRT can explore.
     * @return - A list of environments that needs to be solved by box node.
     */
    public List<Environment> solve (int maxGraphSize) {
        BoxSample goal = new BoxSample(null);
        List<BoxSample> graph = new ArrayList<>();
        graph.add(start);
        boolean notReachGoal = true;
        if (is_goal(start)) {
            goal.parent = start;
            notReachGoal = false;
        }
        int count = 0;
        while (notReachGoal) {

            if (count++ > maxGraphSize) {
                System.out.println("RRT exceed max computation time, exit.");
                return null;
            }

            BoxSample thisSample = get_random_sample();
            if (!graph.contains(thisSample)) {
                BoxSample closest = get_closest(graph, thisSample);
                if (distance2_between(closest, thisSample) > boxSampleStepSquare) {
                    BoxSample temp = extend(closest, thisSample);
                    if (temp == null) continue;
                    thisSample = temp;
                }
                if (can_connect(closest, thisSample)) {
                    thisSample.parent = closest;
                    graph.add(thisSample);
                    if (is_goal(thisSample)) {
                        goal.parent = thisSample;
                        notReachGoal = false;
                    }
                }
            }
        }
        System.out.printf("RRT solved with %d samples.\n", graph.size());
        return construct_box_route(goal.parent);
    }
}