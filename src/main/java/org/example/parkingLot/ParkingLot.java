package org.example.parkingLot;
import java.util.List;
import java.util.UUID;

public class ParkingLot {
    List<Floor> floors;

    public ParkingLot(List<Floor> floors) {
        this.floors = floors;
    }

    public Ticket parkVehicle(Vehicle vehicle) {
        for(Floor floor : floors) {
            ParkingSlot parkingSlot = floor.getAvailableParkingSlot(vehicle.type);
            if(parkingSlot != null) {
                parkingSlot.park(vehicle);
                return new Ticket(UUID.randomUUID().toString(),parkingSlot,vehicle);
            }
        }
        return null;
    }

    public void unpark(Ticket ticket) {
        ticket.parkingSlot.unpark();
    }
}
