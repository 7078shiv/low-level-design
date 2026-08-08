package org.example.strategypattern.solution;

import org.example.ParkingLot2.entity.Vehicle;
import org.example.strategypattern.solution.strategy.NormalDrive;

public class Main {
    public static void main(String[] args) {
        Vechical vechical = new OffRoadVehical();
        vechical.drive();
        Vechical v1 = new PassengerVehical();
        v1.drive();

    }
}
