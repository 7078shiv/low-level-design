package org.example.parkingLot;

import java.util.List;

public class Floor {
    int floorNumber;
    List<ParkingSlot> parkingSlots;

    public Floor(int floorNumber, List<ParkingSlot> parkingSlots) {
        this.floorNumber = floorNumber;
        this.parkingSlots = parkingSlots;
    }

    public ParkingSlot getAvailableParkingSlot(VehicleType vehicleType) {
        for (ParkingSlot parkingSlot : parkingSlots) {
            if(!parkingSlot.isOccupied && vehicleType == parkingSlot.vehicleType){
                return parkingSlot;
            }
        }
        return null;
    }
}
