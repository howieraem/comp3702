package ai;

import java.util.*;
import java.awt.geom.*;
import javafx.util.Pair;
import problem.*;
import utility.*;
import boxAstar.*;
import boxRRT.*;
import robotAstar.*;
import robotRRT.*;
import static utility.Phase.*;

/**
 * This class is the main process.
 */
public class AI implements Utility {

    /* The problem spec. */
    private ProblemSpec ps;

    /* Indicate whether or not this is running in advanced mode. */
    private boolean advancedMode;

    /* Indicate if timeout is turned on. */
    private boolean timeOut;

    /* Start time. */
    private double startTime;

    /**
     * Constuctor.
     * @param ps - Problem sepc.
     * @param advancedMode - Indicate whether or not this is running in advanced mode.
     * @param timeOut - Indicate whether or not this is running in timeOut mode.
     */
    private AI(ProblemSpec ps, boolean advancedMode, boolean timeOut) {
        this.ps = ps;
        this.advancedMode = advancedMode;
        this.timeOut = timeOut;
        startTime = System.currentTimeMillis();
    }

    /**
     * Solve the given problem.
     * @param outputFile - The output solution file name.
     */
    private void solve(String outputFile) {
        /* Init the problem */
        List<Box> movingObjects = new ArrayList<>();
        movingObjects.addAll(ps.getMovingBoxes());
        movingObjects.addAll(ps.getMovingObstacles());
        Environment thisEnvironment = new Environment(movingObjects, ps.getStaticObstacles());
        List<Point2D> goals = ps.getMovingBoxEndPositions();
        RobotConfig initialRobot = ps.getInitialRobotConfig();

        /* Used to store results. */
        List<Action> actions = new ArrayList<>();
        List<String> finalRoute = new ArrayList<>();
        /* Get the initial configuration. */
        finalRoute.add(get_initial_configuration(thisEnvironment, initialRobot));

        /* First of all, solve box route using Astar. */
        System.out.println("Solve box route..");
        thisEnvironment = TreeNodeAstar.solve(thisEnvironment, goals, PHASE_1, actions, 1000);
        //System.out.println("Solve stage 2..");
        //thisEnvironment = TreeNodeAstar.solve(thisEnvironment, goals, PHASE_2, actions, 1000);
        //System.out.println("Solve stage 3..");
        //thisEnvironment = TreeNodeAstar.solve(thisEnvironment, goals, PHASE_3, actions, 2000);
        //System.out.println("Solve stage 4..");
        //thisEnvironment = TreeNodeAstar.solve(thisEnvironment, goals, PHASE_4, actions, 2000);
        //System.out.println("Solve stage 5..");
        //thisEnvironment = TreeNodeAstar.solve(thisEnvironment, goals, PHASE_5, actions, 3000);
        //System.out.println("Solve stage 6..");
        //thisEnvironment = TreeNodeAstar.solve(thisEnvironment, goals, PHASE_6, actions, 3000);
        //System.out.println("Box Stage Done.");

        /* Check if astar has solved the problem. */
        if (!achieve_goal_environment(thisEnvironment, goals)) {
            /* Try to use RRT to move unreached boxes. */
            List<Integer> unreached = new ArrayList<>();
            PriorityQueue<Map.Entry<Integer, Double>> distancesPriority = new PriorityQueue<>(Comparator.comparing(entry -> entry.getValue()));
            Map<Integer, Double> unsorted = new HashMap<>();
            for (int i = 0; i < goals.size(); i++) {
                if (is_two_pos_away(thisEnvironment.movingObjects.get(i).getPos(), goals.get(i))) {
                    double distance = thisEnvironment.movingObjects.get(i).getPos().distance(goals.get(i));
                    unsorted.put(i, distance);
                }
            }
            distancesPriority.addAll(unsorted.entrySet());
            int size = distancesPriority.size();
            for (int i = 0; i < size; i++) {
                unreached.add(distancesPriority.remove().getKey());
            }
            System.out.printf("%d out of %d goals not reached, Perform further search one by one.\n", unreached.size(), goals.size());
            BoxSampleRRT brrt;
            List<Environment> environments;
            int count = 1;
            for (int j : unreached) {
                /* Try solve with Astar first */
                System.out.printf("Further search for %d start..\n", count++);
                Environment goalEnvironment = BoxNodeAstar.solve(thisEnvironment, goals.get(j), actions, j, 250);
                if (goalEnvironment != null) {
                    System.out.println("Solve with further Astar, RRT not required for this goal.");
                    thisEnvironment = goalEnvironment;
                    continue;
                }
                List<Integer> toBemoved = new ArrayList<>();
                toBemoved.add(j);
                brrt = new BoxSampleRRT(toBemoved, thisEnvironment, goals, 0.05);
                System.out.println("RRT start..");
                environments = brrt.solve(50000);
                System.out.println("RRT finishes.");
                /* Check if the 2d RRT is successful. */
                if (advancedMode && environments == null) {
                    /* Try to conduct 4d RRT search if advanced mode is on. */
                    System.out.println("Advanced mode activated, conduct 4d RRT search...");
                    Pair<Box, Integer> feedback = brrt.get_problemBox();
                    if(feedback.getValue() == 0) {
                        /* It isn't a collision box. Calculate the next box instead. */
                        continue;
                    } else {
                        /* It is indeed a collision box, add it to RRT conducting 4d search. */
                        /* Get index of this collision box. */
                        Box collisionBox = feedback.getKey();
                        List<Box> objects = thisEnvironment.movingObjects;
                        for (int i = 0; i < objects.size(); i++) {
                            if (!is_two_pos_away(collisionBox.getPos(), objects.get(i).getPos())) {
                                toBemoved.add(i);
                            }
                        }
                        brrt = new BoxSampleRRT(toBemoved, thisEnvironment, goals, 0.1);
                        environments = brrt.solve( 100000);
                        System.out.println("4d RRT search complete.");
                    }
                }
                /* Check if RRT (2d/4d) finds a solution. */
                if (environments == null) {
                    System.out.println("RRT didn't work, skip this goal :(");
                } else {
                    /* Compute the solution to a list of actions. */
                    Environment current = thisEnvironment;
                    List<Action> temp = new ArrayList<>();
                    boolean trigger = false;
                    int currentIndex = 1;
                    int totalSize = environments.size();
                    System.out.printf("RRT works, calculate actions for robot, total of %d size\n", totalSize);
                    for (Environment e : environments) {
                        if (!BoxNodeAstar.solve(current, e, temp, toBemoved.get(0), 10000)) {
                            trigger = true;
                            break;
                        }
                        if (toBemoved.size() > 1) {
                            if (currentIndex < totalSize) {
                                current.movingObjects.set(toBemoved.get(0), e.movingObjects.get(toBemoved.get(0)));
                                if (!BoxNodeAstar.solve(current, e, temp, toBemoved.get(1), 10000)) {
                                    trigger = true;
                                    break;
                                }
                            }
                        }
                        System.out.printf("%d / %d\r", currentIndex++, totalSize);
                        current = e;
                    }
                    if (trigger) {
                        System.out.println("\nAstar didn't work for this RRT action, skip to next unreached goal.");
                        continue;
                    }
                    System.out.println("\nThis RRT is done");
                    actions.addAll(temp);
                    thisEnvironment = current;
                }
            }
        }
        /* Box planning is done, plan robot. */
        System.out.println("Start to calculate robot routes..");
        RobotConfig current;
        current = initialRobot;
        int count = 1;
        int size = actions.size();
        for (Action action : actions) {

            if (timeOut && System.currentTimeMillis() - startTime > 120000) {
                System.out.println("\nTimeout.");
                break;
            }

            System.out.printf("%d/%d\r", count++, size);
            if (!RobotNodeAstar.solve(action, current, finalRoute, 10000)) {
                /* Robot Astar didn't solve it within a given step, so conduct RRT instead. */
                System.out.println("\nRRT start");
                RobotSampleRRT rrt = new RobotSampleRRT(current, action.pushPosition, action.environment, 0.05);
                List<RobotConfig> segments = rrt.solve(100000);
                System.out.println("RRT finish");
                if (segments == null) {
                    print_error_information(action.environment, goals, current, action.pushPosition);
                    System.out.println("RRT error.");
                    break;
                } else {
                    for (RobotConfig rc : segments) {
                        if (!RobotNodeAstar.solve(current, rc, action.environment, finalRoute, 10000)) {
                            print_error_information(action.environment, goals, current, rc);
                            System.out.println("2nd Astar error.");
                            break;
                        }
                        current = rc;
                    }
                    if (!RobotNodeAstar.solve(current, action.pushPosition, action.environment, finalRoute, 10000)) {
                        print_error_information(action.environment, goals, current, action.pushPosition);
                        System.out.println("2nd Astar final route error.");
                        break;
                    }
                }
            }
            /* For the push itself, it doesn't require ai to search, so compute action fast. */
            finalRoute.addAll(solve_robot_route_fast(action));
            current = action.afterPushPosition;
        }
        output_to_file(finalRoute, outputFile);
        System.out.println("\nDone.");
    }

    /**
     * Main class.
     * @param args - A list of arguements.
     * @throws Exception - When file read/write raise exception.
     */
    public static void main(String[] args) throws Exception {
        long startTime = System.nanoTime();
        AI ai;
        ProblemSpec ps = new ProblemSpec();
        if (args.length != 2 && args.length != 3) {
            System.out.println("Usage: AI inputFileName outputFileName [--advanced-mode, --time-out]");
            return;
        }
        try {
            ps.loadProblem(args[0]);
        } catch (Exception e) {
            System.out.print("Error - Invalid file: ");
            System.out.print(e.getMessage());
            return;
        }
        if (ps.getProblemLoaded()) {
            System.out.println("Solving...");
            if (args.length == 3 && args[2].equals("--advanced-mode")) {
                System.out.println("Warning: Advanced mode enables 4d RRT search, may result in a large time.");
                ai = new AI(ps, true, false);
            } else if (args.length == 3 && args[2].equals("--time-out")) {
                System.out.println("Running up to 120 seconds.");
                ai = new AI(ps, false, true);
            } else {
                ai = new AI(ps, false, false);
            }
            ai.solve(args[1]);
        } else {
            throw new Exception();
        }
        long duration = System.nanoTime() - startTime;
        System.out.printf("Time: %.2f s\n", duration / 1000000000.0);
    }
}