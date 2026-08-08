package org.example.lld.elevatorSystem;

import java.util.ArrayList;
import java.util.List;

public class ElevatorSystem {
    List<ElevatorCar> elevatorCars;
    private final SchedulingStrategy schedulingStrategy;
    public ElevatorSystem(SchedulingStrategy schedulingStrategy,int noOfElevators) {
        this.schedulingStrategy = schedulingStrategy;
        elevatorCars = new ArrayList<>();
        for(int i=0;i<noOfElevators;i++){
            elevatorCars.add(new ElevatorCar(i));
        }
    }

    void SubmitRequest(Request request){
        ElevatorCar elevatorCar = schedulingStrategy.getElevatorCar(elevatorCars, request);
        elevatorCar.addRequest(request.floor);
        System.out.println("Request For Floor:- " + request.getFloor()+ " Assigned To Elevator :- " + elevatorCar.getId());
    }

    void tick() throws InterruptedException {
        for (ElevatorCar elevatorCar : elevatorCars) {
            elevatorCar.Step();
        }
    }
}
