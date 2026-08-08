package org.example.strategypattern.solution;

import org.example.strategypattern.solution.strategy.NormalDrive;

public class PassengerVehical extends Vechical {
    public PassengerVehical() {
        super(new NormalDrive());
    }
}
