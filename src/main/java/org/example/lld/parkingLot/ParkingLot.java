package org.example.lld.parkingLot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// it should be depend on interfaces rather than concrete classes its DIP
public class ParkingLot {
    List<ParkingFloor> parkingFloors;
    PricingStrategy pricingStrategy;
    SpotAllocationStrategy spotAllocationStrategy;
    Map<String, Ticket> ticketIdToTicket;

    ParkingLot(List<ParkingFloor> parkingFloors, PricingStrategy pricingStrategy, SpotAllocationStrategy spotAllocationStrategy) {
        this.parkingFloors = parkingFloors;
        this.pricingStrategy = pricingStrategy;
        this.spotAllocationStrategy = spotAllocationStrategy;
        this.ticketIdToTicket = new ConcurrentHashMap<>();
    }

    public Ticket parkVehical(Vehical vehicle) {
        ParkingSpot spot = spotAllocationStrategy.allocateSpots(parkingFloors, vehicle);
        if(spot == null) {
            throw new RuntimeException("Parking Lot is full");
        }
        Ticket ticket = new Ticket(UUID.randomUUID().toString(),vehicle,spot);
        ticketIdToTicket.put(ticket.getTicketId(), ticket);
        return ticket;
    }

    public double unparkVehical(String ticketId) {
        Ticket ticket = ticketIdToTicket.remove(ticketId);
        if(ticket == null) {throw new RuntimeException("Invalid ticket id");}
        ticket.markExit();
        ticket.getParkingSpot().unparkVehical();
        return pricingStrategy.calculatePrice(ticket.getVehical(),ticket.getParkedHours());
    }

}
