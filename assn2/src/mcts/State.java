package mcts;

import problem.*;

public class State {

    public int pos;
    public int terrain;
    public String carType;
    public String driverType;
    public Tire tireType;
    public int fuel;
    public TirePressure tirePressure;
    public int step;

    public State(int pos, int terrain, String carType, String driverType,
                 Tire tireType, int fuel, TirePressure tirePressure, int step) {
        this.pos = pos;
        this.terrain = terrain;
        this.carType = carType;
        this.driverType = driverType;
        this.tireType = tireType;
        this.fuel = fuel;
        this.tirePressure = tirePressure;
        this.step = step;
    }

    public State clone() {
        return new State(pos, terrain, carType, driverType, tireType,
                fuel, tirePressure, step);
    }
}
