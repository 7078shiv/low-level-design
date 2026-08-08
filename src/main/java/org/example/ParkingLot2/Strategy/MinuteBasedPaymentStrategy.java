package org.example.ParkingLot2.Strategy;

import org.example.ParkingLot2.entity.Ticket;

import java.time.LocalDateTime;

public class MinuteBasedPaymentStrategy implements FeeCalculation {
    @Override
    public int pay(Ticket ticket) {
        int min = LocalDateTime.now().getMinute() - ticket.getEntryTime().getMinute();
        return min * 5;
    }
}
