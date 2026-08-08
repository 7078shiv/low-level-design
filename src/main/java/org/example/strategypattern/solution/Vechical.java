package org.example.strategypattern.solution;

import org.example.strategypattern.solution.strategy.DriveStrategy;

public class Vechical {
    DriveStrategy driveStrategy;
    public Vechical(DriveStrategy driveStrategy) {
        this.driveStrategy = driveStrategy;
    }
    public  void drive() {
        driveStrategy.drive();
    }
}
