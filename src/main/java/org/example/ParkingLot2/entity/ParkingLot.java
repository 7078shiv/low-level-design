package org.example.ParkingLot2.entity;

public class ParkingLot {
    ParkingBuilding parkingBuilding;
    EntranceGate entranceGate;
    ExitGate  exitGate;
    ParkingLot(ParkingBuilding parkingBuilding, EntranceGate entranceGate,ExitGate exitGate) {
        this.parkingBuilding = parkingBuilding;
        this.entranceGate = entranceGate;
        this.exitGate = exitGate;
    }

}
