package org.example.ParkingLot2.entity;

import org.example.ParkingLot2.Strategy.FeeCalculation;

public class ExitGate {
    FeeCalculation feeCalculation;
    public ExitGate(FeeCalculation feeCalculation) {
        this.feeCalculation = feeCalculation;
    }
    public int payFee(Ticket ticket) {
        return feeCalculation.pay(ticket);
    }
    public void releaseReserveSpot(Ticket ticket) {
        ticket.parkingSpot.releaseSpot();
    }
}
