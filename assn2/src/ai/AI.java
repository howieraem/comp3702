package ai;

import problem.*;
import simulator.*;

public class AI {

    private static mcts.State get_mcst_state(ProblemSpec ps, Simulator simulator, simulator.State nextState) {
        int pos = nextState.getPos();
        int terrain = ps.getTerrainIndex(ps.getEnvironmentMap()[pos - 1]);
        String carType = nextState.getCarType();
        String driverType = nextState.getDriver();
        Tire tireType = nextState.getTireModel();
        int fuel = nextState.getFuel();
        TirePressure tirePressure = nextState.getTirePressure();
        int step = simulator.getSteps();
        return new mcts.State(pos, terrain, carType, driverType, tireType, fuel, tirePressure, step);
    }

    private static iteration.State get_iteration_state(ProblemSpec ps, Simulator simulator, simulator.State nextState) {
        int pos = nextState.getPos();
        int terrain = ps.getTerrainIndex(ps.getEnvironmentMap()[pos - 1]);
        String carType = nextState.getCarType();
        String driverType = nextState.getDriver();
        Tire tireType = nextState.getTireModel();
        int fuel = nextState.getFuel();
        TirePressure tirePressure = nextState.getTirePressure();
        return new iteration.State(ps, pos, carType, driverType, tireType, tirePressure, fuel);
    }

    private static void iteration_search(ProblemSpec ps, Simulator simulator) {
        iteration.State current = get_iteration_state(ps, simulator, simulator.reset());
        iteration.Search search = new iteration.Search(ps);
        double startTime = System.currentTimeMillis();
        search.load();
        while ((System.currentTimeMillis() - startTime) / 1000 < 120) {
            search.iterate();
            search.update();
            double max = search.getMax();
            System.out.println(max);
            if (max <= 0.0000001) {
                break;
            }
        }
        while (current.pos < ps.getN()) {
            Action action = search.search(current);
            simulator.State nextState = simulator.step(action);
            if (nextState == null) break;
            current = get_iteration_state(ps, simulator, nextState);
        }
    }

    private static void mcts_search(ProblemSpec ps, Simulator simulator) {
        mcts.State current = get_mcst_state(ps, simulator, simulator.reset());
        mcts.Search search = new mcts.Search(ps, current);
        while (current.pos < ps.getN() && current.step <= ps.getMaxT()) {
            Action action = search.search(15, true);
            simulator.State nextState = simulator.step(action);
            current = get_mcst_state(ps, simulator, nextState);
            search.prune(action, current);
        }
        if (current.pos < ps.getN()) {
            simulator.step(new Action(ActionType.MOVE));
            System.out.println("Didn't solve.");
        } else {
            System.out.println("Solved!");
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: AI inputFileName outputFileName");
            throw new IllegalArgumentException();
        }
        ProblemSpec ps = new ProblemSpec(args[0]);
        Simulator simulator = new Simulator(ps, args[1]);
        if (ps.getLevel().getLevelNumber() <= 3) {
            iteration_search(ps, simulator);
        } else {
            mcts_search(ps, simulator);
        }
    }

}
