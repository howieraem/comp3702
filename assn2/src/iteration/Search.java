package iteration;

import problem.*;
import java.util.*;

public class Search {

    public ProblemSpec ps;
    public List<List<List<List<List<List<State>>>>>> all;

    public Search(ProblemSpec ps) {
        this.ps = ps;
        all = new LinkedList<>();
        for (int pos = 1; pos <= ps.getN(); pos++) {
            List<List<List<List<List<State>>>>> thisPos = new LinkedList<>();
            for (String car : ps.getCarOrder()) {
                List<List<List<List<State>>>> thisCar = new LinkedList<>();
                for (String driver : ps.getDriverOrder()) {
                    List<List<List<State>>> thisDriver = new LinkedList<>();
                    for (Tire tire : ps.getTireOrder()) {
                        List<List<State>> thisTire = new LinkedList<>();
                        for (TirePressure tirePressure : TirePressure.values()) {
                            List<State> thisTirePressure = new LinkedList<>();
                            if (ps.getLevel().getLevelNumber() == 1) {
                                thisTirePressure.add(new State(ps, pos, car, driver, tire, tirePressure, 50));
                            } else {
                                for (int fuel = 0; fuel <= 50; fuel++) {
                                    thisTirePressure.add(new State(ps, pos, car, driver, tire, tirePressure, fuel));
                                }
                            }
                            thisTire.add(thisTirePressure);
                        }
                        thisDriver.add(thisTire);
                    }
                    thisCar.add(thisDriver);
                }
                thisPos.add(thisCar);
            }
            all.add(thisPos);
        }
    }

    public boolean load() {
        for (List<List<List<List<List<State>>>>> thisPos : all) {
            for (List<List<List<List<State>>>> thisCar : thisPos) {
                for (List<List<List<State>>> thisDriver : thisCar) {
                    for (List<List<State>> thisTire : thisDriver) {
                        for (List<State> thisTirePressure : thisTire) {
                            for (State state : thisTirePressure) {
                                state.expand(all);
                                state.init();
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean iterate() {
        for (List<List<List<List<List<State>>>>> thisPos : all) {
            for (List<List<List<List<State>>>> thisCar : thisPos) {
                for (List<List<List<State>>> thisDriver : thisCar) {
                    for (List<List<State>> thisTire : thisDriver) {
                        for (List<State> thisTirePressure : thisTire) {
                            for (State state : thisTirePressure) {
                                state.iterate();
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean update() {
        for (List<List<List<List<List<State>>>>> thisPos : all) {
            for (List<List<List<List<State>>>> thisCar : thisPos) {
                for (List<List<List<State>>> thisDriver : thisCar) {
                    for (List<List<State>> thisTire : thisDriver) {
                        for (List<State> thisTirePressure : thisTire) {
                            for (State state : thisTirePressure) {
                                state.update();
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public double getMax() {
        double max = -1;
        for (List<List<List<List<List<State>>>>> thisPos : all) {
            for (List<List<List<List<State>>>> thisCar : thisPos) {
                for (List<List<List<State>>> thisDriver : thisCar) {
                    for (List<List<State>> thisTire : thisDriver) {
                        for (List<State> thisTirePressure : thisTire) {
                            for (State state : thisTirePressure) {
                                if (state.diff > max) max = state.diff;
                            }
                        }
                    }
                }
            }
        }
        return max;
    }

    public Action search(State current) {
        int posIndex = current.pos - 1;
        int carIndex = ps.getCarIndex(current.car);
        int driverIndex = ps.getDriverIndex(current.driver);
        int tireIndex = ps.getTireIndex(current.tire);
        int tirePressureIndex = 0;
        if (current.tirePressure == TirePressure.SEVENTY_FIVE_PERCENT) tirePressureIndex = 1;
        if (current.tirePressure == TirePressure.ONE_HUNDRED_PERCENT) tirePressureIndex = 2;
        int fuelIndex = current.fuel;
        if (ps.getLevel().getLevelNumber() == 1) fuelIndex = 0;
        return all.get(posIndex).get(carIndex).get(driverIndex).get(tireIndex).get(tirePressureIndex).get(fuelIndex).aMax;
    }
}
