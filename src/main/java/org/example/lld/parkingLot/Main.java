package org.example.lld.parkingLot;

import java.util.List;
import java.util.UUID;

public class Main {
    // parking lot

    // Requirement

    // Need to ask clarifying questions

    //1) flow :- vehical --> gate--> allocate spot --> Ticket ---> parkingSpot--> parkVehical ---> payment --->  exit

    // 2) Requirement
    // types of Vehicals --> (car,truck,MoterCycle)
    // spot size (medium,small,large)
    // is Multiple Floor(Yes)
    // parking Types(nearest to entrance, Nearest to exit)
    // is Multiple gate(Yes)
    // pricing strategy(pay hourly and diff by vehical size)

    //3) Entities
    // Vehicle, Gate, Ticket, ParkingLot, ParkingFloor, Price
    //
    public static void main(String[] args) {


        ParkingSpot parkingSpot = new ParkingSpot(UUID.randomUUID().toString(), SpotType.SMALL);
        ParkingSpot parkingSpot1 = new ParkingSpot(UUID.randomUUID().toString(), SpotType.MEDIUM);
        ParkingSpot parkingSpot2 = new ParkingSpot(UUID.randomUUID().toString(), SpotType.LARGE);


        Car car = new Car(UUID.randomUUID().toString());
        Bus bus = new Bus(UUID.randomUUID().toString());
        MoterCycle moterCycle = new MoterCycle(UUID.randomUUID().toString());

        ParkingFloor parkingFloor1 = new ParkingFloor(1, List.of(parkingSpot, parkingSpot1, parkingSpot2));
        ParkingLot parkingLot = new ParkingLot(List.of(parkingFloor1), new HourlyBasedPricingStrategy(), new NearestToEntrance());
        Ticket ticket = parkingLot.parkVehical(car);
        Ticket ticket1 = parkingLot.parkVehical(bus);
        Ticket ticket2 = parkingLot.parkVehical(moterCycle);

        double payment = parkingLot.unparkVehical(ticket.getTicketId());
        System.out.println("pay amount:-"+payment);
        double payment1 = parkingLot.unparkVehical(ticket1.getTicketId());
        System.out.println("payment1 amount for vehical:-"+ticket1.getVehical().getVehicalType()+" "+payment1);

    }
}
