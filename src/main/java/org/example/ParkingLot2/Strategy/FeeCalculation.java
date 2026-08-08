package org.example.ParkingLot2.Strategy;

import org.example.ParkingLot2.entity.Ticket;

public interface FeeCalculation {
    int pay(Ticket ticket);
}
