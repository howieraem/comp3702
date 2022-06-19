package mcts;

import problem.*;
import java.util.*;

public class OrNode {

    /* constant for explore nodes */
    private final static double EXPLORATION_CONSTANT = 1.414 * 7;

    private ProblemSpec ps;
    public AndNode parent;
    public State state;
    public double RS;
    public int NS;
    public List<Action> availableActions;
    public Map<Action, AndNode> children;
    public Map<Action, Double> QSA;
    public Map<Action, Integer> NSA;

    public OrNode(ProblemSpec ps, State state, AndNode parent, double RS) {
        this.ps = ps;
        this.state = state;
        this.parent = parent;
        this.RS = RS;
        NS = 0;
        availableActions = get_available_actions();
        children = new HashMap<>();
        QSA = new HashMap<>();
        NSA = new HashMap<>();
        for (Action action : availableActions) {
            children.put(action, null);
            QSA.put(action, 0.0);
            NSA.put(action, 0);
        }
    }

    public Action select() {
        /* First make sure each node in NSA is explored at least once */
        for (Action action : NSA.keySet()) {
            if (NSA.get(action) == 0) return action;
        }
        /* Each node has been explored at least once, use UCT to get action */
        Action aMax = null;
        double uct = -1;
        for (Action action : QSA.keySet()) {
            double value = QSA.get(action) + EXPLORATION_CONSTANT * Math.sqrt(Math.log(NS) / NSA.get(action));
            if (value > uct) {
                aMax = action;
                uct = value;
            }
        }
        return aMax;
    }

    public AndNode enter(Action action) { return children.get(action); }

    public AndNode expand(Action action) {
        AndNode child = new AndNode(ps, state, this, action);
        children.put(action, child);
        return child;
    }

    public double simulate() {
        Random random = new Random();
        Action startAction = availableActions.get(random.nextInt(availableActions.size()));
        Action action = startAction;
        AndNode an = new AndNode(ps, state, null, action);
        OrNode on;
        double discount = ps.getDiscountFactor();
        int count = 1;
        double q = 0;
        while (true) {
            on = an.sample();
            q += Math.pow(discount, count) * on.RS;
            if (on.end()) {
                return q;
            }
            action = on.get_play_out_policy();
            an = new AndNode(ps, on.state, null, action);
            count++;
        }
    }

    public void backPropagate(Action action, double q) {
        double value = QSA.get(action);
        int nsa = NSA.get(action);
        double newValue = (value * nsa + q) / (nsa + 1);
        QSA.put(action, newValue);
        NSA.put(action, nsa + 1);
        NS++;
        if (parent != null) parent.backPropagate(q * ps.getDiscountFactor());
    }

    public boolean end() {
        return state.pos == ps.getN() || state.step >= ps.getMaxT();
    }

    private Action get_play_out_policy() {
        //TODO: THERE ARE TWO TYPES OF PLAY OUT POLICY: GREEDY AND RANDOM, I USED GREEDY HERE
        for (Action action : availableActions) {
            if (action.getActionType() == ActionType.MOVE) return action;
        }
        for (Action action : availableActions) {
            if (action.getActionType() == ActionType.ADD_FUEL && action.getFuel() == 50 - state.fuel) return action;
        }
        System.out.println("Something wrong with the play out policy!");
        return null;
    }

    private List<Action> get_available_actions() {
        List<Action> result = new ArrayList<>();
        List<ActionType> availableActions = ps.getLevel().getAvailableActions();
        for (ActionType actionType : availableActions) {
            switch (actionType) {
                case MOVE:
                    int fuelUsage = ps.getFuelUsage()[state.terrain][ps.getCarIndex(state.carType)];
                    if (state.tirePressure == TirePressure.FIFTY_PERCENT) fuelUsage *= 3;
                    else if (state.tirePressure == TirePressure.SEVENTY_FIVE_PERCENT) fuelUsage *= 2;
                    if (ps.getLevel().getLevelNumber() == 1) fuelUsage = 0;
                    if(state.fuel >= fuelUsage) result.add(new Action(ActionType.MOVE));
                    break;
                case CHANGE_CAR:
                    for (String car : ps.getCarOrder()) {
                        if (!car.equals(state.carType)) {
                            result.add(new Action(ActionType.CHANGE_CAR, car));
                        }
                    }
                    break;
                case CHANGE_DRIVER:
                    for (String driver : ps.getDriverOrder()) {
                        if (!driver.equals(state.driverType)) {
                            result.add(new Action(ActionType.CHANGE_DRIVER, driver));
                        }
                    }
                    break;
                case CHANGE_TIRES:
                    for (Tire tire : ps.getTireOrder()) {
                        if (!tire.asString().equals(state.tireType.asString())) {
                            result.add(new Action(ActionType.CHANGE_TIRES, tire));
                        }
                    }
                    break;
                case ADD_FUEL:
                    //TODO: NOTE THIS ONLY CONSIDER ADD FUEL TO FULL, TO REDUCE ACTION SPACE
                    if (state.fuel < 50) {
                        result.add(new Action(ActionType.ADD_FUEL, 50 - state.fuel));
                    }
                    break;
                case CHANGE_PRESSURE:
                    for (TirePressure tirePressure : TirePressure.values()) {
                        if (!tirePressure.asString().equals(state.tirePressure.asString())) {
                            result.add(new Action(ActionType.CHANGE_PRESSURE, tirePressure));
                        }
                    }
                    break;
                case CHANGE_CAR_AND_DRIVER:
                    for (String car : ps.getCarOrder()) {
                        for (String driver : ps.getDriverOrder()) {
                            if (!car.equals(state.carType) && !driver.equals(state.driverType)) {
                                result.add(new Action(ActionType.CHANGE_CAR_AND_DRIVER, car, driver));
                            }
                        }
                    }
                    break;
                case CHANGE_TIRE_FUEL_PRESSURE:
                    for (int i = 1; i < 50 - state.fuel; i++) {
                        for (Tire tire : ps.getTireOrder()) {
                            for (TirePressure tirePressure : TirePressure.values()) {
                                if (!tirePressure.asString().equals(state.tirePressure.asString()) ||
                                        !tire.asString().equals(state.tireType.asString())) {
                                    result.add(new Action(ActionType.CHANGE_TIRE_FUEL_PRESSURE, tire, i, tirePressure));
                                }
                            }
                        }
                    }
                    break;
            }
        }
        if (ps.getLevel().getLevelNumber() >= 4) {
            //For Level 4 and above, Need to Prune actions that not worth exploring.
            Heuristic.prune_actions(ps, this, result);
        }
        return result;
    }
}
