package org.example.strategypattern.solution;

import org.example.strategypattern.solution.strategy.SpecialDrive;

public class OffRoadVehical extends Vechical{
    public OffRoadVehical() {
        super(new SpecialDrive());
    }
}
