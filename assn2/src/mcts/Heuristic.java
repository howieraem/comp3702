package mcts;

import problem.*;
import java.util.*;

public class Heuristic {

    /* Tunable magic number used to determine if actions are pretty close. */
    private final static double MAGIC_NUMBER = 1.8;
    private final static double MAGIC_PROB_THRES = -0.9;

    /* This is used to give a MOVE action if actions are same, MOVE only has 1 step advantage. */
    /* Used in Level-3 and above. */
    public static boolean has_similar_reward(Map<Action, Integer> NSA) {
        /* Before check, see if available actions contain move */
        boolean trigger = true;
        for (Action a : NSA.keySet()) {
            if (a.getActionType() == ActionType.MOVE) trigger = false;
        }
        if (trigger) return false;  //It doesn't have MOVE, so we can not pick.

        //It does have MOVE, we need to check exploration times.
        int min = 0;
        int max = 0;
        for (Action a : NSA.keySet()) {
            int explore = NSA.get(a);
            if (min == 0 && max == 0) {
                min = max = explore;
            } else {
                if (explore < min) min = explore;
                if (explore > max) max = explore;
            }
        }
        return (max / (double)min < MAGIC_NUMBER);
    }

    /* Used in Level-4 and above (May be used in Level-3) */
    public static void prune_actions(ProblemSpec ps, OrNode on, List<Action> availableActions) {
        /* Before prune, check if available actions contain move */
        boolean trigger = true;
        for (Action a : availableActions) {
            if (a.getActionType() == ActionType.MOVE) trigger = false;
        }
        if (trigger) return;    //AvailableActions don't have MOVE, we can not prune rest actions.

        //AvailableActions do have MOVE, we can compare the other actions' probability with current probability
        Map<Action, double[]> probs = new HashMap<>();
        for (Action a : availableActions) {
            if (a.getActionType() != ActionType.MOVE) {
                probs.put(a, get_probabilities_after_action(ps, on, a));
            }
        }
        double[] currentProbs = Helper.getMoveProbs(ps, on.state);

        for (Action a : probs.keySet()) {
            if (is_worse(ps, probs.get(a), currentProbs)) {
                //Action a will lead to a worse condition, prune it.
                availableActions.remove(a);
            }
        }
    }

    /* Require: action can not already be move. */
    public static double[] get_probabilities_after_action(ProblemSpec ps, OrNode on, Action action) {
        State state = on.state.clone();
        switch (action.getActionType()) {
            case CHANGE_CAR:
                state.carType = action.getCarType();
                state.tirePressure = TirePressure.ONE_HUNDRED_PERCENT;
                break;
            case CHANGE_DRIVER:
                state.driverType = action.getDriverType();
                break;
            case CHANGE_TIRES:
                state.tireType = action.getTireModel();
                break;
            case CHANGE_PRESSURE:
                state.tirePressure = action.getTirePressure();
                break;
            case CHANGE_CAR_AND_DRIVER:
                state.carType = action.getCarType();
                state.tirePressure = TirePressure.ONE_HUNDRED_PERCENT;
                state.driverType = action.getDriverType();
                break;
            case CHANGE_TIRE_FUEL_PRESSURE:
                state.tireType = action.getTireModel();
                state.tirePressure = action.getTirePressure();
                break;
        }
        return Helper.getMoveProbs(ps, state);
    }

    ///////////////////////////  is_worse (different version)
    private static boolean is_worse(ProblemSpec ps, double[] givenProbs, double[] currentProbs) {
        double compare = 0;
        // probs 0-3 : moving backward
        for (int i = 0; i < 4; i++) {
            compare -= (4 - i) * (givenProbs[i] - currentProbs[i]);
        }
        // probs 5-9 : moving forward
        for (int i = 0; i < 5; i++) {
            compare += (i + 1) * (givenProbs[i+5] - currentProbs[i+5]);
        }
        // probs 10 : slip
        compare -= ps.getSlipRecoveryTime() * (givenProbs[10] - currentProbs[10]);
        // probs 11 : breakdown
        compare -= ps.getRepairTime() * (givenProbs[11] - currentProbs[11]);

        return compare >= -MAGIC_PROB_THRES;
    }

    private static boolean is_worse(double[] givenProbs, double[] currentProbs) {
        if (calc_expect(givenProbs) - calc_expect(currentProbs) < MAGIC_PROB_THRES) {
            return true;
        }
        return false;
    }

    private static double calc_expect(double[] probs) {
        double expect = 0;
        for (int k = 0; k < probs.length; ++k) {
            if (k < probs.length - 2) {
                expect += (k - 4)*probs[k];
            } else {
                expect -= k*probs[k];
            }
        }
        return expect;
    }
}
