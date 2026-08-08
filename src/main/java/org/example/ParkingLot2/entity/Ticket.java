package org.example.ParkingLot2.entity;

import lombok.Data;
import org.example.ParkingLot2.entity.Vehicle;

import java.sql.Timestamp;
import java.time.LocalDateTime;
@Data
public class Ticket {
    String ticketId;
    Vehicle vehicle;
    ParkingLevel parkingLevel;
    ParkingSpot parkingSpot;
    LocalDateTime entryTime;
}
