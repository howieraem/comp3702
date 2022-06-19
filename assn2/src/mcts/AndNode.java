package mcts;

import problem.*;
import java.util.*;

public class AndNode {

    private ProblemSpec ps;
    public OrNode parent;
    public State state;
    public Action actionToHere;
    public List<OrNode> availableChildren;
    public Map<OrNode, Double> probability;
    public Map<OrNode, Double> sample;

    public AndNode(ProblemSpec ps, State state, OrNode parent, Action actionToHere) {
        this.ps = ps;
        this.state = state;
        this.parent = parent;
        this.actionToHere = actionToHere;
        availableChildren = get_available_children();
        sample = new LinkedHashMap<>();
        probability = new HashMap<>();
        set_sample();
    }

    public OrNode sample() {
        Random random = new Random();
        double p = random.nextDouble();
        for (OrNode child : availableChildren) {
            if (p <= sample.get(child)) {
                return child;
            }
        }
        System.out.println("Error sampling");
        return null;
    }

    public void simulate() {
        double q = 0;
        for (OrNode child : availableChildren) {
            q += child.simulate() * probability.get(child);
        }
        backPropagate(q);
    }

    public void backPropagate(double q) {
        parent.backPropagate(actionToHere, q);
    }

    private List<OrNode> get_available_children() {
        List<OrNode> result = new ArrayList<>();
        State newState;
        switch (actionToHere.getActionType()) {
            case CHANGE_CAR:
                newState = state.clone();
                newState.carType = actionToHere.getCarType();
                newState.fuel = 50;
                newState.tirePressure = TirePressure.ONE_HUNDRED_PERCENT;
                newState.step += 1;
                result.add(new OrNode(ps, newState, this, 0));
                break;
            case CHANGE_DRIVER:
                newState = state.clone();
                newState.driverType = actionToHere.getDriverType();
                newState.step += 1;
                result.add(new OrNode(ps, newState, this, 0));
                break;
            case CHANGE_TIRES:
                newState = state.clone();
                newState.tireType = actionToHere.getTireModel();
                newState.tirePressure = TirePressure.ONE_HUNDRED_PERCENT;
                newState.step += 1;
                result.add(new OrNode(ps, newState, this, 0));
                break;
            case ADD_FUEL:
                newState = state.clone();
                newState.fuel += actionToHere.getFuel();
                newState.step += (int) Math.ceil(actionToHere.getFuel() / (float) 10);
                result.add(new OrNode(ps, newState, this, 0));
                break;
            case CHANGE_PRESSURE:
                newState = state.clone();
                newState.tirePressure = actionToHere.getTirePressure();
                newState.step += 1;
                result.add(new OrNode(ps, newState, this, 0));
                break;
            case CHANGE_CAR_AND_DRIVER:
                newState = state.clone();
                newState.carType = actionToHere.getCarType();
                newState.fuel = 50;
                newState.tirePressure = TirePressure.ONE_HUNDRED_PERCENT;
                newState.driverType = actionToHere.getDriverType();
                newState.step += 1;
                result.add(new OrNode(ps, newState, this, 0));
                break;
            case CHANGE_TIRE_FUEL_PRESSURE:
                newState = state.clone();
                newState.tireType = actionToHere.getTireModel();
                newState.tirePressure = actionToHere.getTirePressure();
                newState.fuel += actionToHere.getFuel();
                newState.step += (int) Math.ceil(actionToHere.getFuel() / (float) 10);
                result.add(new OrNode(ps, newState, this, 0));
                break;
            case MOVE:
                int fuelUsage = ps.getFuelUsage()[state.terrain][ps.getCarIndex(state.carType)];
                if (state.tirePressure == TirePressure.FIFTY_PERCENT) fuelUsage *= 3;
                else if (state.tirePressure == TirePressure.SEVENTY_FIVE_PERCENT) fuelUsage *= 2;
                if (ps.getLevel().getLevelNumber() == 1) fuelUsage = 0;
                for (int i = -4; i <= 5; i++) {
                    if (state.pos + i >= 1 && state.pos + i <= ps.getN()) {
                        newState = state.clone();
                        newState.fuel -= fuelUsage;
                        newState.pos += i;
                        newState.terrain = ps.getTerrainIndex(ps.getEnvironmentMap()[newState.pos - 1]);
                        newState.step += 1;
                        double reward = i > 0 ? i : 0;
                        if (newState.pos == ps.getN()) {
                            /* TODO: THIS IS THE REWARD FUNCTION */
                            reward += ps.getMaxT() - newState.step;
                        }
                        result.add(new OrNode(ps, newState, this, reward));
                    }
                }
                /* Add slip. */
                newState = state.clone();
                newState.fuel -= fuelUsage;
                newState.step += ps.getSlipRecoveryTime();
                result.add(new OrNode(ps, newState, this, 0));
                /* Add breakdown. */
                newState = state.clone();
                newState.fuel -= fuelUsage;
                newState.step += ps.getRepairTime();
                result.add(new OrNode(ps, newState, this, 0));
                break;
        }
        return result;
    }

    private void set_sample() {
        if (actionToHere.getActionType() != ActionType.MOVE) {
            /* Action to here is not move, only one child is available with 100% probability */
            probability.put(availableChildren.get(0), 1.0);
            sample.put(availableChildren.get(0), 1.0);
        } else {
            /* Action to here is move, multiple children  */
            double[] probability = Helper.getMoveProbs(ps, state);
            if (availableChildren.size() != 12) {
                /* Edge case. */
                int truncatedLength = 12 - availableChildren.size();
                double[] newProbability = new double[availableChildren.size()];
                if (availableChildren.get(0).state.pos == 1) {
                    /* Truncate from left to right. */
                    int count = 0;
                    for (int i = truncatedLength; i < 10; i++) {
                        newProbability[count++] = probability[i];
                    }
                    for (int i = 0; i < truncatedLength; i++) {
                        newProbability[0] += probability[i];
                    }
                } else {
                    /* Truncate from right to left. */
                    for (int i = 0; i < 10 - truncatedLength; i++) {
                        newProbability[i] = probability[i];
                    }
                    for (int i = 10 - truncatedLength; i < 10; i++) {
                        newProbability[newProbability.length - 3] += probability[i];
                    }
                }
                newProbability[newProbability.length - 2] = probability[10];
                newProbability[newProbability.length - 1] = probability[11];
                probability = newProbability;
            }
            /* Put probabilities */
            double pSum = 0;
            int size = availableChildren.size();
            List<OrNode> removed = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                /* Remove children with 0 probability */
                if (probability[i] == 0.0) removed.add(availableChildren.get(i));
                this.probability.put(availableChildren.get(i), probability[i]);
                pSum += probability[i];
                sample.put(availableChildren.get(i), pSum);
            }
            if (pSum < 0.99 || pSum > 1.01) {
                System.out.println("Error calculating Probability!");
            }
            sample.put(availableChildren.get(availableChildren.size() - 1), 1.0);
            /* Remove node with 0 probability */
            for (OrNode remove : removed) {
                availableChildren.remove(remove);
                this.probability.remove(remove);
                sample.remove(remove);
            }
        }
    }
}
