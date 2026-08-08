package org.example.strategypattern.solution;

import org.example.strategypattern.solution.strategy.SpecialDrive;

public class Sporty extends Vechical {
    public Sporty() {
        super(new SpecialDrive());
    }
}
