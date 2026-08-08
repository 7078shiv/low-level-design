package org.example.lld.elevatorSystem;

public class Main {
    // Clarify Requirement

    // No of elevator (design for N not for one)
    // How Many Floors (M no of floors)
    // Request Type (we can press Button from outside(with direction) and inside(floor no we need to press) both)
    // Scheduling Rule (nearest available lift) or with direction. -->. flag this as a strategy
    // Capacity --> weight capacity/person capacity
    // Door Open and Close timing(How much time door will open then close) -->. assume fix dwell time
    // Emergency stop / maintenance mode --->. Mention as a state

    // Assumptions

    // N elevators, M floors, inside/outside, scheduling, fixed capacity, fix door open time, elevator runs independently

    // Entities :-Direction, ElevatorState, Request (internal/external), ElevatorCar, Door, Display, ElevatorController, ElevatorSystem.

    public static void main(String[] args) throws InterruptedException {
        ElevatorSystem elevatorSystem = new ElevatorSystem(new NearestElevator(),2);
        elevatorSystem.SubmitRequest(Request.externalRequest(11,Direction.UP));
        elevatorSystem.SubmitRequest(Request.externalRequest(30,Direction.UP));
        elevatorSystem.SubmitRequest(Request.internalRequest(1));
        elevatorSystem.SubmitRequest(Request.internalRequest(50));
        for (int t = 0; t < 12; t++) elevatorSystem.tick();
    }
}