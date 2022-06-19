package iteration;

import problem.*;
import java.util.*;

public class State {

    /* The following terms identify the state. */
    public ProblemSpec ps;
    public int pos;
    public String car;
    public String driver;
    public Tire tire;
    public TirePressure tirePressure;
    public int fuel;

    /* The following terms are used for calculating. */
    public double VT;
    public double VTT;
    public double RS;
    public Map<Action, List<Pair>> children;
    public Action aMax;
    public double diff;

    public State(ProblemSpec ps, int pos, String car, String driver,
                 Tire tire, TirePressure tirePressure, int fuel) {
        this.ps = ps;
        this.pos = pos;
        this.car = car;
        this.driver = driver;
        this.tire = tire;
        this.tirePressure = tirePressure;
        this.fuel = fuel;
        VT = VTT = diff = 0;
        children = new HashMap<>();
        aMax = null;
        if (pos == ps.getN()) {
            RS = 1;    //Reward
        } else {
            RS = 0;
        }
    }

    public void init() {
        VT = RS;
    }

    public void iterate() {
        //if (this.pos == ps.getN()) VTT = VT;
        VTT = -1;
        for (Action a : children.keySet()) {
            List<Pair> transition = children.get(a);
            double temp = RS;
            int count = 0;
            for (Pair p : transition) {
                double discount = ps.getDiscountFactor();
                int step;
                switch (a.getActionType()) {
                    case ADD_FUEL:
                        step = (int) Math.ceil((p.state.fuel - fuel) / (float) 10);
                        discount = Math.pow(discount, step);
                        break;
                    case CHANGE_TIRE_FUEL_PRESSURE:
                        step = (int) Math.ceil((p.state.fuel - fuel) / (float) 10);
                        discount = Math.pow(discount, step);
                        break;
                    case MOVE:
                        if (count == transition.size() - 1) {
                            step = 1 + ps.getRepairTime();
                            discount = Math.pow(discount, step);
                        } else if (count == transition.size() - 2) {
                            step = 1 + ps.getSlipRecoveryTime();
                            discount = Math.pow(discount, step);
                        }
                        count++;
                        break;
                }
                temp += discount * p.probability * p.state.VT;
                if (temp > VTT) {
                    VTT = temp;
                    aMax = a;
                }
            }
        }
    }

    public void update() {
        diff = Math.abs(VT - VTT);
        VT = VTT;
    }

    public void expand(List<List<List<List<List<List<State>>>>>> all) {
        //if (pos == ps.getN()) return;
        List<ActionType> availableActions = ps.getLevel().getAvailableActions();
        List<Pair> transition;
        for (ActionType actionType : availableActions) {
            switch (actionType) {
                case MOVE:
                    int terrain = ps.getTerrainIndex(ps.getEnvironmentMap()[pos - 1]);
                    int fuelUsage = ps.getFuelUsage()[terrain][ps.getCarIndex(car)];
                    if (tirePressure == TirePressure.FIFTY_PERCENT) fuelUsage *= 3;
                    else if (tirePressure == TirePressure.SEVENTY_FIVE_PERCENT) fuelUsage *= 2;
                    if (ps.getLevel().getLevelNumber() == 1) fuelUsage = 0;
                    if (fuelUsage <= fuel) {
                        Action action = new Action(ActionType.MOVE);
                        transition = get_move_transition(fuelUsage, all);
                        children.put(action, transition);
                    }
                    break;
                case CHANGE_CAR:
                    for (String car : ps.getCarOrder()) {
                        if (!car.equals(this.car)) {
                            Action action = new Action(ActionType.CHANGE_CAR, car);
                            transition = new ArrayList<>();
                            State child = search(pos, car, driver, tire, TirePressure.ONE_HUNDRED_PERCENT, 50, all);
                            transition.add(new Pair(child, 1.00));
                            children.put(action, transition);
                        }
                    }
                    break;
                case CHANGE_DRIVER:
                    for (String driver : ps.getDriverOrder()) {
                        if (!driver.equals(this.driver)) {
                            Action action = new Action(ActionType.CHANGE_DRIVER, driver);
                            transition = new ArrayList<>();
                            State child = search(pos, car, driver, tire, tirePressure, fuel, all);
                            transition.add(new Pair(child, 1.00));
                            children.put(action, transition);
                        }
                    }
                    break;
                case CHANGE_TIRES:
                    for (Tire tire : ps.getTireOrder()) {
                        if (!tire.asString().equals(this.tire.asString())) {
                            Action action = new Action(ActionType.CHANGE_TIRES, tire);
                            transition = new ArrayList<>();
                            State child = search(pos, car, driver, tire, TirePressure.ONE_HUNDRED_PERCENT, fuel, all);
                            transition.add(new Pair(child, 1.00));
                            children.put(action, transition);
                        }
                    }
                    break;
                case ADD_FUEL:
                    //TODO: NOTE THIS ONLY CONSIDER ADD FUEL TO FULL, TO REDUCE ACTION SPACE
                    if (fuel < 50) {
                        Action action = new Action(ActionType.ADD_FUEL, 50 - fuel);
                        transition = new ArrayList<>();
                        State child = search(pos, car, driver, tire, tirePressure, 50, all);
                        transition.add(new Pair(child, 1.00));
                        children.put(action, transition);
                    }
                    break;
                case CHANGE_PRESSURE:
                    for (TirePressure tirePressure : TirePressure.values()) {
                        if (!tirePressure.asString().equals(this.tirePressure.asString())) {
                            Action action = new Action(ActionType.CHANGE_PRESSURE, tirePressure);
                            transition = new ArrayList<>();
                            State child = search(pos, car, driver, tire, tirePressure, fuel, all);
                            transition.add(new Pair(child, 1.00));
                            children.put(action, transition);
                        }
                    }
                    break;
                case CHANGE_CAR_AND_DRIVER:
                    for (String car : ps.getCarOrder()) {
                        for (String driver : ps.getDriverOrder()) {
                            if (!car.equals(this.car) && !driver.equals(this.driver)) {
                                //Only a combination of different car AND different driver will be added. To reduce action apace.
                                Action action = new Action(ActionType.CHANGE_CAR_AND_DRIVER, car, driver);
                                transition = new ArrayList<>();
                                State child = search(pos, car, driver, tire, TirePressure.ONE_HUNDRED_PERCENT, 50, all);
                                transition.add(new Pair(child, 1.00));
                                children.put(action, transition);
                            }
                        }
                    }
                    break;
                case CHANGE_TIRE_FUEL_PRESSURE:
                    for (Tire tire : ps.getTireOrder()) {
                        for (TirePressure tirePressure : TirePressure.values()) {
                            if (!tirePressure.asString().equals(this.tirePressure.asString()) &&
                                    !tire.asString().equals(this.tire.asString())) {
                                //Only a combination of differnt tire and pressure will be added. To reduce action space.
                                Action action = new Action(ActionType.CHANGE_TIRE_FUEL_PRESSURE, tire, 50-fuel, tirePressure);
                                transition = new ArrayList<>();
                                State child = search(pos, car, driver, tire, tirePressure, 50, all);
                                transition.add(new Pair(child, 1.00));
                                children.put(action, transition);
                            }
                        }
                    }
                    break;
            }
        }
    }

    private List<Pair> get_move_transition(int fuelUsage, List<List<List<List<List<List<State>>>>>> all) {
        List<Pair> transition = new ArrayList<>();
        for (int i = -4; i <= 5; i++) {
            if (pos + i >= 1 && pos + i <= ps.getN()) {
                State child = search(pos + i, car, driver, tire, tirePressure, 50 - fuelUsage, all);
                transition.add(new Pair(child, 1.00));
            }
        }
        /* Add slip, and breakdown. */
        State child = search(pos, car, driver, tire, tirePressure, 50 - fuelUsage, all);
        transition.add(new Pair(child, 1.00));  //Slip
        transition.add(new Pair(child, 1.00));  //Breakdown
        set_probability(transition);
        return transition;
    }

    private void set_probability(List<Pair> children) {
        /* Action to here is move, multiple children  */
        double[] probability = Helper.getMoveProbs(ps, this);
        if (children.size() != 12) {
            /* Edge case. */
            int truncatedLength = 12 - children.size();
            double[] newProbability = new double[children.size()];
            if (children.get(0).state.pos == 1) {
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
        int size = children.size();
        for (int i = 0; i < size; i++) {
            children.get(i).probability = probability[i];
        }
    }

    private State search(int pos, String car, String driver, Tire tire, TirePressure tirePressure, int fuel,
                         List<List<List<List<List<List<State>>>>>> all) {
        int posIndex = pos - 1;
        int carIndex = ps.getCarIndex(car);
        int driverIndex = ps.getDriverIndex(driver);
        int tireIndex = ps.getTireIndex(tire);
        int tirePressureIndex = 0;
        if (tirePressure == TirePressure.SEVENTY_FIVE_PERCENT) tirePressureIndex = 1;
        if (tirePressure == TirePressure.ONE_HUNDRED_PERCENT) tirePressureIndex = 2;
        int fuelIndex = fuel;
        if (ps.getLevel().getLevelNumber() == 1) fuelIndex = 0;
        return all.get(posIndex).get(carIndex).get(driverIndex).get(tireIndex).get(tirePressureIndex).get(fuelIndex);
    }

    @Override
    public int hashCode() {
        return pos + car.hashCode() + driver.hashCode() + tire.asString().hashCode() +
                tirePressure.asString().hashCode() + fuel;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof State) {
            State state = (State) o;
            return this.pos == state.pos &&
                    this.car.equals(state.car) &&
                    this.driver.equals(state.driver) &&
                    this.tire.asString().equals(state.tire.asString()) &&
                    this.tirePressure.asString().equals(state.tirePressure.asString()) &&
                    this.fuel == state.fuel;
        }
        return false;
    }
}
