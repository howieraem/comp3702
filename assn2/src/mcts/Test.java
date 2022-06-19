package mcts;

import problem.*;
import java.util.*;

public class Test {

    private static int getFuelConsumption(ProblemSpec ps, State state) {

        Terrain terrain = ps.getEnvironmentMap()[state.pos - 1];
        String car = state.carType;
        TirePressure pressure = state.tirePressure;
        int terrainIndex = ps.getTerrainIndex(terrain);
        int carIndex = ps.getCarIndex(car);
        int fuelConsumption = ps.getFuelUsage()[terrainIndex][carIndex];

        if (pressure == TirePressure.FIFTY_PERCENT) {
            fuelConsumption *= 3;
        } else if (pressure == TirePressure.SEVENTY_FIVE_PERCENT) {
            fuelConsumption *= 2;
        }
        return fuelConsumption;
    }

    private static int getFuelConsumption(ProblemSpec ps, String car, int pos) {

        Terrain terrain = ps.getEnvironmentMap()[pos - 1];
        int terrainIndex = ps.getTerrainIndex(terrain);
        int carIndex = ps.getCarIndex(car);
        return ps.getFuelUsage()[terrainIndex][carIndex];
    }

    public static Action get_play_out_policy(ProblemSpec ps, OrNode on) {
        boolean trigger = true;
        Action aMax = null;
        for (Action a : on.availableActions) {
            if (a.getActionType() == ActionType.MOVE) {
                trigger = false;
                aMax = a;
            }
        }

        if (trigger) {
            for (Action action : on.availableActions) {
                if (action.getActionType() == ActionType.ADD_FUEL && action.getFuel() == 50 - on.state.fuel) {
                    return action;
                } else if (action.getActionType() == ActionType.CHANGE_CAR && getFuelConsumption(ps, on.state) > 3 * getFuelConsumption(ps, action.getCarType(), on.state.pos)) {
                    // If the current fuel consumptiom is much larger than that after changing the car
                    return action;
                }
            }
            System.out.println("Error play out policy!");
        }

        Map<Action, double[]> probs = new HashMap<>();
        for (Action a : on.availableActions) {
            if (a.getActionType() != ActionType.MOVE) {
                probs.put(a, Heuristic.get_probabilities_after_action(ps, on, a));
            }
        }
        double max = 2 * calc_expect(ps, Helper.getMoveProbs(ps, on.state));
        for (Action a : probs.keySet()) {
            double expect = calc_expect(ps, probs.get(a));
            if (expect > max) {
                aMax = a;
                max = expect;
            }
        }
        return aMax;
    }

    private static double calc_expect(ProblemSpec ps, double[] probs) {
        int recover = ps.getSlipRecoveryTime();
        int repair = ps.getRepairTime();
        double expect = 0;
        for (int k = 0; k < probs.length - 2; k++) {
            expect += (k - 4) * probs[k];
        }
        expect += -expect * recover * probs[10] - expect * repair * probs[11];
        return expect;
    }
}
