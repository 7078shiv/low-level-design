package org.example.lld.elevatorSystem;

import java.util.List;

public class NearestElevator implements SchedulingStrategy {

    @Override
    public ElevatorCar getElevatorCar(List<ElevatorCar> elevatorCars, Request request) {
        ElevatorCar bestElevatorCar = null;
        int distance = Integer.MAX_VALUE;
        for (ElevatorCar car : elevatorCars) {
            int currentDis = calculateDistance(car,request);
            if (currentDis < distance) {
                distance = currentDis;
                bestElevatorCar = car;
            }
        }
        return bestElevatorCar;
    }

    private int calculateDistance(ElevatorCar car, Request request) {
        int dis = Math.abs(car.getCurrentFloor() - request.getFloor());
        if(car.state.equals(ElevatorState.STOPPED)) return dis;

        if(request.getFloor() > car.currentFloor && car.direction.equals(Direction.UP) ||
        request.getFloor() < car.currentFloor && car.direction.equals(Direction.DOWN)){
            return dis;
        }
        return dis+1000;  // it should be complete its round then fulfill your request.
    }
}
