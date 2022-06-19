package test;

import problem.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.*;

public class CollisionTester {
	
    /** Maximum step size for a primitive step*/
    public static final double MAX_BASE_STEP = 0.001;
    /** Maximum error*/
    public static final double MAX_ERROR = 0.0001;
    /** Remembers the specifications of the problem. */
    private ProblemSpec ps;
    /** Maximum angle error when checking if robot is parallel to axis */
    private double angleError;
	
	public CollisionTester(ProblemSpec ps){
        this.ps = ps;
        angleError = Math.asin((MAX_ERROR/2)/(ps.getRobotWidth()/2)) * 2;
    }
	
	/**
     * Creates a new Rectangle2D that is grown by delta in each direction
     * compared to the given Rectangle2D.
     *
     * @param rect
     *            the Rectangle2D to expand.
     * @param delta
     *            the amount to expand by.
     * @return a Rectangle2D expanded by delta in each direction.
     */
    private Rectangle2D grow(Rectangle2D rect, double delta) {
        return new Rectangle2D.Double(rect.getX() - delta, rect.getY() - delta,
                rect.getWidth() + 2 * delta, rect.getHeight() + 2 * delta);
    }
	
	/**
     * Check if a given state contains collision
     * @param r state of robot
     * @param movingObjects state of all movable objects
     * @return true if no collision
     */
    public void hasCollision() {
		
		RobotConfig r = ps.getInitialRobotConfig();
		
		List<Box> movingObjects = new ArrayList<Box>();
		movingObjects.addAll(ps.getMovingBoxes());
		movingObjects.addAll(ps.getMovingObstacles());
		
		boolean hasCollision = false;
		
        Line2D robotLine = new Line2D.Double(getPoint1(r), getPoint2(r));
        Rectangle2D border = new Rectangle2D.Double(0,0,1,1);
        for (StaticObstacle o: ps.getStaticObstacles()) {
            if (robotLine.intersects(grow(o.getRect(), -MAX_ERROR))) {
                hasCollision = true;
            }
        }

        if (!border.contains(robotLine.getP1()) || !border.contains(robotLine.getP2())) {
            hasCollision = true;
        }

        for (Box b1: movingObjects) {

            if (!border.contains(b1.getRect())) {
                hasCollision = true;
            }

            Rectangle2D collisionBox = grow(b1.getRect(),-MAX_ERROR);
            if (collisionBox.intersectsLine(robotLine)) {
                    hasCollision = true;
            }

            for (Box b2: movingObjects) {
                if ((!b1.equals(b2)) && (collisionBox.intersects(b2.getRect()))) {
                    hasCollision = true;
                }
            }

            for (StaticObstacle o: ps.getStaticObstacles()) {
                if (collisionBox.intersects(o.getRect())) {
                    hasCollision = true;
                }
            }
        }
		
		if (hasCollision == false) {
			System.out.println("Pass");
		} else {
			System.out.println("Collision");
		}
    }
	
	/**
     * Get the first point of the robot
     * @param r the robot
     * @return A Point2D representing the first point.
     */
    private Point2D getPoint2(RobotConfig r) {
        double x = r.getPos().getX() + Math.cos(r.getOrientation()) * ps.getRobotWidth() * 0.5;
        double y = r.getPos().getY() + Math.sin(r.getOrientation()) * ps.getRobotWidth() * 0.5;
        return new Point2D.Double(x,y);
    }
    /**
     * Get the second point of the robot
     * @param r the robot
     * @return A Point2D representing the second point.
     */
    private Point2D getPoint1(RobotConfig r) {
        double x = r.getPos().getX() - Math.cos(r.getOrientation()) * ps.getRobotWidth() * 0.5;
        double y = r.getPos().getY() - Math.sin(r.getOrientation()) * ps.getRobotWidth() * 0.5;
        return new Point2D.Double(x,y);
    }
	
	/**
     * Read problem and solution. Runs tests.
     * @param args input file name for problem and solution
     */
    public static void main(String[] args) {
        ProblemSpec ps = new ProblemSpec();
        try {
            ps.loadProblem(args[0]);
        } catch (IOException e) {
            System.out.println("FAILED: Invalid problem file");
            System.out.println(e.getMessage());
            return;
        }
        CollisionTester tester = new CollisionTester(ps);
		tester.hasCollision();
    }
}