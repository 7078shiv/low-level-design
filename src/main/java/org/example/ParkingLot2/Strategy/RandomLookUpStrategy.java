package org.example.ParkingLot2.Strategy;

import org.example.ParkingLot2.entity.ParkingSpot;

import java.util.List;

public class RandomLookUpStrategy implements ParkingSpotLookUpStrategy {
    @Override
    public ParkingSpot getParkingSpot(List<ParkingSpot> parkingSpots) {
        for(ParkingSpot spot : parkingSpots){
            if(spot.isFree()) return spot;
        }
        return null;
    }
}
