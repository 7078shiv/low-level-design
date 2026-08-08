package org.example.lld.parkingLot;

import lombok.Getter;

import java.sql.Timestamp;
@Getter
public class Ticket {
    String ticketId;
    Vehical vehical;
    ParkingSpot parkingSpot;
    Timestamp entryTime;
    Timestamp exitTime;

    Ticket(String ticketId,Vehical vehical,ParkingSpot parkingSport){
        this.ticketId = ticketId;
        this.vehical = vehical;
        this.parkingSpot = parkingSport;
        this.entryTime = new Timestamp(System.currentTimeMillis());
    }

    public void markExit(){
        this.exitTime = new Timestamp(System.currentTimeMillis());
    }

    public int getParkedHours(){
        return (int) (exitTime.getTime() - entryTime.getTime());
    }

}
