package org.example.lld.parkingLot;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ParkingFloor {
    @Getter
    int floorNumber;
    List<ParkingSpot> parkingSpots;

    ParkingFloor(int floorNumber, List<ParkingSpot> parkingSpots) {
        this.floorNumber = floorNumber;
        this.parkingSpots = parkingSpots;
    }

     ParkingSpot parkVehical(Vehical vehicle) {
        for (ParkingSpot parkingSpot : parkingSpots) {
            if(parkingSpot.parkVehical(vehicle)){
                return parkingSpot;
            }
        }
        return null;
     }

}
