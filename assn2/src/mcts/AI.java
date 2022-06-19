package mcts;

import problem.*;
import simulator.*;

public class AI {

    private static State get_state(ProblemSpec ps, Simulator simulator, simulator.State nextState) {
        int pos = nextState.getPos();
        int terrain = ps.getTerrainIndex(ps.getEnvironmentMap()[pos - 1]);
        String carType = nextState.getCarType();
        String driverType = nextState.getDriver();
        Tire tireType = nextState.getTireModel();
        int fuel = nextState.getFuel();
        TirePressure tirePressure = nextState.getTirePressure();
        int step = simulator.getSteps();
        return new State(pos, terrain, carType, driverType, tireType, fuel, tirePressure, step);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: AI inputFileName outputFileName");
            throw new IllegalArgumentException();
        }
        ProblemSpec ps = new ProblemSpec(args[0]);
        Simulator simulator = new Simulator(ps, args[1]);
        State current = get_state(ps, simulator, simulator.reset());
        Search search = new Search(ps, current);
        while (current.pos < ps.getN() && current.step <= ps.getMaxT()) {
            Action action = search.search(15, true);
            simulator.State nextState = simulator.step(action);
            current = get_state(ps, simulator, nextState);
            search.prune(action, current);
        }
        if (current.pos < ps.getN()) {
            simulator.step(new Action(ActionType.MOVE));
            System.out.println("Didn't solve.");
        } else {
            System.out.println("Solved!");
        }
    }
}
