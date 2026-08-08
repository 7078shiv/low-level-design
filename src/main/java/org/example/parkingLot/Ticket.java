package org.example.parkingLot;

import lombok.Data;

@Data
public class Ticket {
    String ticketId;
    long entryTime;
    Vehicle vehicle;
    ParkingSlot parkingSlot;

    public Ticket(String ticketId, ParkingSlot parkingSlot, Vehicle vehicle) {
        this.ticketId = ticketId;
        this.entryTime = System.currentTimeMillis();
        this.parkingSlot = parkingSlot;
        this.vehicle = vehicle;
    }
}
