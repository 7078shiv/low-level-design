package org.example.ParkingLot2.Strategy;

import org.example.ParkingLot2.entity.Ticket;

import java.time.LocalDateTime;

public class HourlyPaymentStrategy implements FeeCalculation {
    @Override
    public int pay(Ticket ticket) {
        int hours = LocalDateTime.now().getHour() - ticket.getEntryTime().getHour();
        return hours * 10;
    }
}
