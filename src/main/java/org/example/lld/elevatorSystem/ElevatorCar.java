package org.example.lld.elevatorSystem;

import lombok.Getter;

import java.util.Collections;
import java.util.PriorityQueue;
// Heart
public class ElevatorCar implements Runnable{
    @Getter
    int id;
    @Getter
    Direction direction = Direction.IDLE;
    @Getter
    int currentFloor = 0;
    ElevatorState state = ElevatorState.STOPPED;
    PriorityQueue<Integer> upQueue;
    PriorityQueue<Integer> downQueue;
    ElevatorCar(int id){
        this.id = id;
        upQueue = new PriorityQueue<>();
        downQueue = new PriorityQueue<>(Collections.reverseOrder());
    }

    synchronized void addRequest(int floor){
        if(floor > currentFloor){
            upQueue.add(floor);
        } else if(floor < currentFloor){
            downQueue.add(floor);
        }

        if(Direction.IDLE.equals(direction)){
            direction = floor > currentFloor ? Direction.UP : Direction.DOWN;
        }
    }

    synchronized void Step() throws InterruptedException {
        if(direction.equals(Direction.UP)){
            if(upQueue.isEmpty()){flipOrIdeal(); return;}
            int floor = upQueue.poll();
            currentFloor++;
            if(currentFloor == floor){
                openDoor();
            }
        } else if(direction.equals(Direction.DOWN)){
            if(downQueue.isEmpty()){flipOrIdeal(); return;}
            int floor = downQueue.poll();
            currentFloor--;
            if(currentFloor == floor){
                openDoor();
            }
        }
    }

    private void openDoor() throws InterruptedException {
        state = ElevatorState.DOOR_OPEN;
        System.out.println("Elevator stopped at floor " + currentFloor);

        // some time we will use like interval to open lift 10 sec
        Thread.sleep(10000);
        state = ElevatorState.STOPPED;
    }

    private void flipOrIdeal(){
        if(direction.equals(Direction.UP) && !downQueue.isEmpty()){
            direction = Direction.DOWN;
        } else if(direction.equals(Direction.DOWN) && !upQueue.isEmpty()){
            direction = Direction.UP;
        } else {
            direction = Direction.IDLE;
        }
    }


    @Override
    public void run() {

    }
}
