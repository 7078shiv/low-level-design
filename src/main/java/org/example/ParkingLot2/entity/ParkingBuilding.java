package org.example.ParkingLot2.entity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ParkingBuilding {
    List<ParkingLevel> parkingLevels;
    ParkingBuilding(List<ParkingLevel> parkingLevels){
        this.parkingLevels = parkingLevels;
    }

    Ticket allocateTicket(Vehicle vehicle){
        Ticket ticket = new Ticket();
        ticket.ticketId = UUID.randomUUID().toString();
        ticket.vehicle = vehicle;
        for (ParkingLevel parkingLevel : parkingLevels){
            if(parkingLevel.hasAvailableSpot()){
                ticket.parkingSpot=parkingLevel.parkVehicle(vehicle.getVehicleType());
                ticket.parkingLevel = parkingLevel;
                break;
            }
        }
        ticket.entryTime = LocalDateTime.now();
        return ticket;
    }

    void releaseTicket(Ticket ticket){
        ticket.parkingLevel.unPark(ticket.vehicle.getVehicleType(),ticket.parkingSpot);
    }


}
