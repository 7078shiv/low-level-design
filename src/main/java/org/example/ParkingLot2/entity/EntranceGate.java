package org.example.ParkingLot2.entity;

public class EntranceGate {
    Ticket entry(ParkingBuilding parkingBuilding, Vehicle vehicle){
        return parkingBuilding.allocateTicket(vehicle);
    }
}
