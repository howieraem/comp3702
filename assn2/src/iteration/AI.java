package iteration;

import problem.*;
import simulator.Simulator;

public class AI {

    private static State get_state(ProblemSpec ps, Simulator simulator, simulator.State nextState) {
        int pos = nextState.getPos();
        int terrain = ps.getTerrainIndex(ps.getEnvironmentMap()[pos - 1]);
        String carType = nextState.getCarType();
        String driverType = nextState.getDriver();
        Tire tireType = nextState.getTireModel();
        int fuel = nextState.getFuel();
        TirePressure tirePressure = nextState.getTirePressure();
        return new State(ps, pos, carType, driverType, tireType, tirePressure, fuel);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: AI inputFileName outputFileName");
            throw new IllegalArgumentException();
        }
        ProblemSpec ps = new ProblemSpec(args[0]);
        Simulator simulator = new Simulator(ps, args[1]);
        State current = get_state(ps, simulator, simulator.reset());
        Search search = new Search(ps);
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
            current = get_state(ps, simulator, nextState);
        }
    }
}
