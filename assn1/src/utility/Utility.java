package utility;

import java.util.*;
import java.awt.geom.*;
import problem.*;
import robotRRT.*;
import java.io.*;

/**
 * This interface has default methods used in a lot of scenarios.
 */
public interface Utility {

    /* Space left for push position. */
    int SPACE_LEFT = 10;

    /* Weight of box distances in box heuristic. */
    double BOX_WEIGHT = 1000.0;

    /* Weight of distance in robot heuristic. */
    double DISTANCE_WEIGHT = 1.0;

    /* Weight of angle in robot heuristic. */
    double ANGLE_WEIGHT = 1.0;

    /* Max double error. */
    double MAX_ERROR = 0.0001;

    /* A unit step. */
    double UNIT_STEP = 0.001;

    /**
     * Check if two positions are away.
     * @param a - Position a.
     * @param b - Position b.
     * @return - True if they are away, false otherwise.
     */
    default boolean is_two_pos_away(Point2D a, Point2D b) {
        return (Math.abs(a.getX() - b.getX()) >= MAX_ERROR ||
                Math.abs(a.getY() - b.getY()) >= MAX_ERROR);
    }

    /**
     * Check if two positions are away by step.
     * @param a - Position a.
     * @param b - Position b.
     * @param step - Step.
     * @return - True if they are away by step, false otherwise.
     */
    default boolean is_two_pos_away(Point2D a, Point2D b, double step) {
        return (Math.abs(a.getX() - b.getX()) >= step ||
                Math.abs(a.getY() - b.getY()) >= step);
    }

    /**
     * Check if two robot configs are equal.
     * @param a - Robot config a.
     * @param b - Robot config b.
     * @return - True if they are equal, false otherwise.
     */
    default boolean is_two_robots_equal(RobotConfig a, RobotConfig b) {
        return (Math.abs(a.getPos().getX() - b.getPos().getX()) < MAX_ERROR &&
                Math.abs(a.getPos().getY() - b.getPos().getY()) < MAX_ERROR &&
                Math.abs(a.getOrientation() - b.getOrientation()) < MAX_ERROR);
    }

    /**
     * Get first point of robot line.
     * @param r - Robot config.
     * @param width - Robot width.
     * @return - Position of first point.
     */
    default Point2D getPoint1(RobotConfig r, double width) {
        double x = r.getPos().getX() - Math.cos(r.getOrientation()) * width * 0.5;
        double y = r.getPos().getY() - Math.sin(r.getOrientation()) * width * 0.5;
        return new Point2D.Double(x,y);
    }

    /**
     * Get second point of robot line.
     * @param r - Robot config.
     * @param width - Robot width.
     * @return - Position of second point.
     */
    default Point2D getPoint2(RobotConfig r, double width) {
        double x = r.getPos().getX() + Math.cos(r.getOrientation()) * width * 0.5;
        double y = r.getPos().getY() + Math.sin(r.getOrientation()) * width * 0.5;
        return new Point2D.Double(x,y);
    }

    /**
     * Grow rectangle by a delta.
     * @param rect - Rectangle.
     * @param delta - Growing delta.
     * @return - The new rectangle.
     */
    default Rectangle2D grow(Rectangle2D rect, double delta) {
        return new Rectangle2D.Double(rect.getX() - delta, rect.getY() - delta,
                rect.getWidth() + 2 * delta, rect.getHeight() + 2 * delta);
    }

    /**
     * Check if collision at same side.
     * @param line - Robot line.
     * @param boxes - All moving objects.
     * @param horizontal - If the robot is horizontal or vertical.
     * @return - True if yes, false otherwise.
     */
    default boolean isAtSameSide(Line2D line, List<Box> boxes, boolean horizontal) {
        double prevSide = 0;
        for (Box box: boxes) {
            double side;
            if (horizontal) {
                side = box.getPos().getY() + MAX_ERROR - line.getY1();
            } else {
                side = box.getPos().getX() + MAX_ERROR - line.getX1();
            }
            if (prevSide * side < 0) {
                return false;
            }
            prevSide = side;
        }
        return true;
    }

    /**
     * Normalise Angle.
     * @param angle - Raw angle.
     * @return - Normalised angle.
     */
    default double normaliseAngle(double angle) {
        while (angle < 0) {
            angle += 2 * Math.PI;
        }
        while (angle >= 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }
        return angle + 2 * Math.PI;
    }

    /**
     * Test if the current robot config is sliding.
     * @param width - The robot width.
     * @param r - The robot config.
     * @param movingObjects - All moving objects.
     * @return - True if sliding occurs, false otherwise.
     */
    default boolean testGapSliding(double width, RobotConfig r, List<Box> movingObjects) {
        double angleError = Math.asin((MAX_ERROR/2)/(width/2)) * 2;
        double angle = normaliseAngle(r.getOrientation());
        Point2D p1,p2,r1,r2;
        p1 = getPoint1(r, width);
        p2 = getPoint2(r, width);
        boolean horizontal;
        if (angle >= Math.PI * 4 - angleError || angle <= Math.PI * 2 + angleError) {
            r1 = new Point2D.Double(p1.getX() + UNIT_STEP, p1.getY());
            r2 = new Point2D.Double(p2.getX() - UNIT_STEP, p2.getY());
            horizontal = true;
        } else if (angle >= Math.PI * 2.5 - angleError && angle <= Math.PI * 2.5 + angleError) {
            r1 = new Point2D.Double(p1.getX(), p1.getY() + UNIT_STEP);
            r2 = new Point2D.Double(p2.getX(), p2.getY() - UNIT_STEP);
            horizontal = false;
        } else if (angle >= Math.PI * 3 - angleError && angle <= Math.PI * 3 + angleError) {
            r1 = new Point2D.Double(p2.getX() + UNIT_STEP, p2.getY());
            r2 = new Point2D.Double(p1.getX() - UNIT_STEP, p1.getY());
            horizontal = true;
        } else if (angle >= Math.PI * 3.5 - angleError && angle <= Math.PI * 3.5 + angleError) {
            r1 = new Point2D.Double(p2.getX(), p2.getY() + UNIT_STEP);
            r2 = new Point2D.Double(p1.getX(), p1.getY() - UNIT_STEP);
            horizontal = false;
        } else {
            return true;
        }
        int count = 0;
        Line2D robotLine = new Line2D.Double(r1, r2);
        List<Box> collidedBox = new ArrayList<Box>();
        for (Box b : movingObjects) {
            Rectangle2D collisionBox = grow(b.getRect(), MAX_ERROR);
            if (collisionBox.intersectsLine(robotLine)) {
                count++;
                collidedBox.add(b);
            }
        }
        return count <= 1 || isAtSameSide(robotLine, collidedBox, horizontal);
    }

    /**
     * Check box in collision mode without feedback.
     * @param movingObjects - The moving objects.
     * @param staticObstacles - The static obstacles.
     * @return - True if no collision, false otherwise.
     */
    default boolean noCollisionForBoxStrictWithoutFeedback(List<Box> movingObjects, List<StaticObstacle> staticObstacles) {
        Rectangle2D border = new Rectangle2D.Double(0,0,1,1);
        border = grow(border,MAX_ERROR);
        for (Box b1: movingObjects) {

            if (!border.contains(b1.getRect())) {
                return false;
            }

            Rectangle2D collisionBox = grow(b1.getRect(),-MAX_ERROR);

            for (Box b2: movingObjects) {
                if ((!b1.equals(b2)) && (collisionBox.intersects(b2.getRect()))) {
                    return false;
                }
            }

            for (StaticObstacle o: staticObstacles) {
                if (collisionBox.intersects(o.getRect())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check box in collision mode with feedback.
     * @param movingObjects - The moving objects.
     * @param staticObstacles - The static obstacles.
     * @param boxCollisionCount - Map stores box with collision time.
     * @return - True if no collision, false otherwise.
     */
    default boolean noCollisionForBoxStrictWithFeedback(List<Box> movingObjects, List<StaticObstacle> staticObstacles, Map<Box, Integer> boxCollisionCount) {
        Rectangle2D border = new Rectangle2D.Double(0,0,1,1);
        border = grow(border,MAX_ERROR);
        for (Box b1: movingObjects) {

            if (!border.contains(b1.getRect())) {
                return false;
            }

            Rectangle2D collisionBox = grow(b1.getRect(),-MAX_ERROR);

            for (Box b2: movingObjects) {
                if ((!b1.equals(b2)) && (collisionBox.intersects(b2.getRect()))) {
                    if (boxCollisionCount.containsKey(b1)) {
                        boxCollisionCount.put(b1, boxCollisionCount.get(b1) + 1);
                    } else if (boxCollisionCount.containsKey(b2)) {
                            boxCollisionCount.put(b2, boxCollisionCount.get(b2) + 1);
                    }
                    return false;
                }
            }

            for (StaticObstacle o: staticObstacles) {
                if (collisionBox.intersects(o.getRect())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check current if current state has collision.
     * @param oldEnvironment - The old environment.
     * @param newEnvironment - The new environment.
     * @param robot - Robot config.
     * @param width - Robot width.
     * @return - True if no collision, false otherwise.
     */
    default boolean noCollisionForAll(Environment oldEnvironment, Environment newEnvironment, RobotConfig robot, double width) {

        List<StaticObstacle> staticObstacles = newEnvironment.staticObstacles;
        List<Box> movingObjects = newEnvironment.movingObjects;

        Line2D robotLine = new Line2D.Double(getPoint1(robot, width), getPoint2(robot, width));
        Rectangle2D border = new Rectangle2D.Double(0,0,1,1);
        border = grow(border,MAX_ERROR);
        for (StaticObstacle o: staticObstacles) {
            if (robotLine.intersects(grow(o.getRect(), -MAX_ERROR))) {
                return false;
            }
        }
        if (!border.contains(robotLine.getP1()) || !border.contains(robotLine.getP2())) {
            return false;
        }
        for (Box b1: movingObjects) {

            if (!border.contains(b1.getRect())) {
                return false;
            }

            Rectangle2D collisionBox = grow(b1.getRect(),-MAX_ERROR);
            if (collisionBox.intersectsLine(robotLine)) {
                return false;
            }
            int count = 0;
            for (Box b2: movingObjects) {
                if ((!b1.equals(b2)) && (collisionBox.intersects(b2.getRect()))) {
                    return false;
                } else if (b1.equals(b2)) {
                    count++;
                }
                if (count > 1) {
                    return false;
                }
            }
            for (StaticObstacle o: staticObstacles) {
                if (collisionBox.intersects(o.getRect())) {
                    return false;
                }
            }
        }
        return testGapSliding(width, robot, oldEnvironment.movingObjects);
    }

    /**
     * Solve robot push route fast without ai.
     * @param action - The push action.
     * @return - A list of robot route in string format.
     */
    default List<String> solve_robot_route_fast(Action action) {

        Environment environment = action.environment;
        RobotConfig current = action.pushPosition;
        RobotConfig goal = action.afterPushPosition;
        int movedBox = action.movedBox;
        Direction direction = action.direction;

        List<String> finalRoute = new ArrayList<>();
        List<Box> allBoxes = environment.movingObjects;
        Point2D movedBoxBefore = allBoxes.get(movedBox).getPos();

        int index = -1;
        for (int i = 0; i < allBoxes.size(); i++) {
            if (allBoxes.get(i).pos.getX() - movedBoxBefore.getX() == 0 &&
                    allBoxes.get(i).pos.getY() - movedBoxBefore.getY() == 0) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            /* Something wrong */
            System.out.println("Something wrong.");
            return null;
        }

        int loopTime = 0;

        switch (direction) {
            case UP:
                loopTime = (int)Math.round(Math.abs((goal.getPos().getY() - current.getPos().getY()) / 0.001));
                break;
            case DOWN:
                loopTime = (int)Math.round(Math.abs((goal.getPos().getY() - current.getPos().getY()) / 0.001));
                break;
            case LEFT:
                loopTime = (int)Math.round(Math.abs((goal.getPos().getX() - current.getPos().getX()) / 0.001));
                break;
            case RIGHT:
                loopTime = (int)Math.round(Math.abs((goal.getPos().getX() - current.getPos().getX()) / 0.001));
                break;
        }

        String robotRoute;

        for (int i = 0; i < loopTime + 1; i++) {
            robotRoute = "";
            switch (direction) {
                case UP:
                    robotRoute = String.format("%.3f %.3f %.3f", current.getPos().getX(), current.getPos().getY() + i * 0.001, current.getOrientation());
                    for (int j = 0; j < allBoxes.size(); j++) {
                        double width = allBoxes.get(j).getWidth();
                        if (j == index) {
                            robotRoute = robotRoute.concat(String.format(" %.3f %.3f", movedBoxBefore.getX() + 0.5 * width, movedBoxBefore.getY() + 0.5 * width + i * 0.001));
                        } else {
                            robotRoute = robotRoute.concat(String.format(" %.3f %.3f", allBoxes.get(j).pos.getX() + 0.5 * width, allBoxes.get(j).pos.getY() + 0.5 * width));
                        }
                    }
                    break;
                case DOWN:
                    robotRoute = String.format("%.3f %.3f %.3f", current.getPos().getX(), current.getPos().getY() - i * 0.001, current.getOrientation());
                    for (int j = 0; j < allBoxes.size(); j++) {
                        double width = allBoxes.get(j).getWidth();
                        if (j == index) {
                            robotRoute = robotRoute.concat(String.format(" %.3f %.3f", movedBoxBefore.getX() + 0.5 * width, movedBoxBefore.getY() + 0.5 * width - i * 0.001));
                        } else {
                            robotRoute = robotRoute.concat(String.format(" %.3f %.3f", allBoxes.get(j).pos.getX() + 0.5 * width, allBoxes.get(j).pos.getY() + 0.5 * width));
                        }
                    }
                    break;
                case LEFT:
                    robotRoute = String.format("%.3f %.3f %.3f", current.getPos().getX() - i * 0.001, current.getPos().getY(), current.getOrientation());
                    for (int j = 0; j < allBoxes.size(); j++) {
                        double width = allBoxes.get(j).getWidth();
                        if (j == index) {
                            robotRoute = robotRoute.concat(String.format(" %.3f %.3f", movedBoxBefore.getX() + 0.5 * width - i * 0.001, movedBoxBefore.getY() + 0.5 * width));
                        } else {
                            robotRoute = robotRoute.concat(String.format(" %.3f %.3f", allBoxes.get(j).pos.getX() + 0.5 * width, allBoxes.get(j).pos.getY() + 0.5 * width));
                        }
                    }
                    break;
                case RIGHT:
                    robotRoute = String.format("%.3f %.3f %.3f", current.getPos().getX() + i * 0.001, current.getPos().getY(), current.getOrientation());
                    for (int j = 0; j < allBoxes.size(); j++) {
                        double width = allBoxes.get(j).getWidth();
                        if (j == index) {
                            robotRoute = robotRoute.concat(String.format(" %.3f %.3f", movedBoxBefore.getX() + 0.5 * width + i * 0.001, movedBoxBefore.getY() + 0.5 * width));
                        } else {
                            robotRoute = robotRoute.concat(String.format(" %.3f %.3f", allBoxes.get(j).pos.getX() + 0.5 * width, allBoxes.get(j).pos.getY() + 0.5 * width));
                        }
                    }
                    break;
            }
            finalRoute.add(robotRoute);
        }
        return finalRoute;
    }

    /**
     * Get the initial state of the game.
     * @param environment - The environment.
     * @param initial - The initial robot config.
     * @return - A string of initial state.
     */
    default String get_initial_configuration(Environment environment, RobotConfig initial) {
        String result = String.format("%.3f %.3f %.3f", initial.getPos().getX(), initial.getPos().getY(), initial.getOrientation());
        for (Box b : environment.movingObjects) {
            result = result.concat(String.format(" %.3f %.3f", b.getPos().getX() + 0.5 * b.getWidth(), b.getPos().getY() + 0.5 * b.getWidth()));
        }
        return result;
    }

    /**
     * Output to file.
     * @param finalRoute - The final route to be written.
     * @param fileName - The file name.
     */
    default void output_to_file(List<String> finalRoute, String fileName) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
            bw.write(String.format("%d", finalRoute.size()));
            bw.newLine();
            for (String s : finalRoute) {
                bw.write(s);
                bw.newLine();
            }
            bw.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Check if the environment has reached goal.
     * @param environment - The given environment.
     * @param goals - The goals.
     * @return - True if the given environment is already goal.
     */
    default boolean achieve_goal_environment(Environment environment, List<Point2D> goals) {
        int size = goals.size();
        List<Box> objects = environment.movingObjects;
        for (int i = 0; i < size; i++) {
            if (is_two_pos_away(objects.get(i).getPos(), goals.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Print samples to file "data.txt".
     * @param graph - The samples.
     */
    default void print_data(List<RobotSample> graph) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("data.txt"));
            for (RobotSample rs : graph) {
                bw.write(String.format("%.3f %.3f %.3f\n", rs.state.getPos().getX(), rs.state.getPos().getY(), rs.state.getOrientation()));
                bw.flush();
            }
            bw.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Print error information to file "from.txt" and "to.txt".
     * @param environment - The given environment.
     * @param goals - The goals.
     * @param current - The current robot config.
     * @param goal - The goal robot config.
     */
    default void print_error_information(Environment environment, List<Point2D> goals, RobotConfig current, RobotConfig goal) {
        try {
            BufferedWriter bw1 = new BufferedWriter(new FileWriter("from.txt"));
            BufferedWriter bw2 = new BufferedWriter(new FileWriter("to.txt"));
            bw1.write(String.format("%.3f %.3f %.3f %.3f", environment.movingObjects.get(0).getWidth(), current.getPos().getX(), current.getPos().getY(),
                    current.getOrientation()));
            bw1.newLine();
            bw2.write(String.format("%.3f %.3f %.3f %.3f", environment.movingObjects.get(0).getWidth(), goal.getPos().getX(), goal.getPos().getY(),
                    goal.getOrientation()));
            bw2.newLine();
            bw1.write(String.format("%d %d %d", goals.size(), environment.movingObjects.size() - goals.size(), environment.staticObstacles.size()));
            bw1.newLine();
            bw2.write(String.format("%d %d %d", goals.size(), environment.movingObjects.size() - goals.size(), environment.staticObstacles.size()));
            bw2.newLine();
            //Print environment
            for (int i = 0; i < goals.size(); i++) {
                Box b = environment.movingObjects.get(i);
                double x = b.getPos().getX() + 0.5 * b.getWidth();
                double y = b.getPos().getY() + 0.5 * b.getWidth();
                double xx = goals.get(i).getX() + 0.5 * b.getWidth();
                double yy = goals.get(i).getY() + 0.5 * b.getWidth();
                bw1.write(String.format("%.3f %.3f %.3f %.3f", x, y, xx, yy));
                bw1.newLine();
                bw2.write(String.format("%.3f %.3f %.3f %.3f", x, y, xx, yy));
                bw2.newLine();
            }
            for (int i = goals.size(); i < environment.movingObjects.size(); i++) {
                Box b = environment.movingObjects.get(i);
                double x = b.getPos().getX() + 0.5 * b.getWidth();
                double y = b.getPos().getY() + 0.5 * b.getWidth();
                bw1.write(String.format("%.3f %.3f %.3f", x, y, b.getWidth()));
                bw1.newLine();
                bw2.write(String.format("%.3f %.3f %.3f", x, y, b.getWidth()));
                bw2.newLine();
            }
            for (int i = 0; i < environment.staticObstacles.size(); i++) {
                StaticObstacle o = environment.staticObstacles.get(i);
                double x1 = o.getRect().getMinX();
                double y1 = o.getRect().getMinY();
                double x2 = o.getRect().getMaxX();
                double y2 = o.getRect().getMaxY();
                bw1.write(String.format("%.3f %.3f %.3f %.3f", x1, y1, x2, y2));
                bw1.newLine();
                bw2.write(String.format("%.3f %.3f %.3f %.3f", x1, y1, x2, y2));
                bw2.newLine();
            }
            bw1.flush();
            bw2.flush();
            bw1.close();
            bw2.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}