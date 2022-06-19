package mcts;

import problem.*;

public class Search {

    int level;
    private OrNode root;

    public Search(ProblemSpec ps, State start) {
        level = ps.getLevel().getLevelNumber();
        root = new OrNode(ps, start, null, 0);
    }

    public Action search(double maxTime, boolean debug) {
        /* Max time in second */
        double startTime = System.currentTimeMillis() / 1000;
        while (System.currentTimeMillis() / 1000 - startTime <= maxTime) {
            OrNode on = root;
            Action action = root.select();
            AndNode an = root.enter(action);
            boolean trigger = false;
            while (an != null) {
                on = an.sample();
                if (on.end()) {
                    an.backPropagate(on.RS);
                    trigger = true;
                    break;
                }
                action = on.select();
                an = on.enter(action);
            }
            if (trigger) continue;
            an = on.expand(action);
            an.simulate();
        }
        Action aMax = null;
        double reward = -1;
        for (Action a : root.QSA.keySet()) {
            double value = root.QSA.get(a);
            if (value > reward) {
                aMax = a;
                reward = value;
            }
        }
        /* For debug purpose */
        if (debug && aMax != null) {
            for (Action a : root.NSA.keySet()) {
                System.out.println(a.getText() + ": " + root.NSA.get(a) + " " + root.QSA.get(a));
            }
            System.out.println(root.NS);
            System.out.println(aMax.getText());
        }
        /* If level is at least 3, need to check if every action is explored similar. */
        if (aMax.getActionType() != ActionType.MOVE && level >= 3) {
            if (Heuristic.has_similar_reward(root.NSA)) {
                //Return MOVE.
                System.out.println("Heuristic 1 used.");
                for (Action a : root.availableActions) {
                    if (a.getActionType() == ActionType.MOVE) return a;
                }
            }
        }
        return aMax;
    }

    public void prune(Action action, State nextState) {
        for (OrNode on : root.children.get(action).availableChildren) {
            if (two_states_equal(on.state, nextState)) {
                on.parent = null;
                root = on;
                return;
            }
        }
        System.out.println("Error pruning!");
    }

    private boolean two_states_equal(State s1, State s2) {
        return s1.pos == s2.pos &&
                s1.terrain == s2.terrain &&
                s1.carType.equals(s2.carType) &&
                s1.driverType.equals(s2.driverType) &&
                s1.tireType.asString().equals(s2.tireType.asString()) &&
                s1.fuel == s2.fuel &&
                s1.tirePressure.asString().equals(s2.tirePressure.asString()) &&
                s1.step == s2.step;
    }
}
