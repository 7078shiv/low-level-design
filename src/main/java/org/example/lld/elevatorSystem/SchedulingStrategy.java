package org.example.lld.elevatorSystem;

import java.util.List;

public interface SchedulingStrategy {
    ElevatorCar getElevatorCar(List<ElevatorCar> elevatorCars,Request request);
}
