package org.example.ParkingLot2.entity;

import org.example.ParkingLot2.Strategy.ParkingSpotLookUpStrategy;

import java.util.List;

public class FourWheelerSpotManager extends ParkingSpotManager {
    List<ParkingSpot> parkingSpots;
    ParkingSpotLookUpStrategy lookUpStrategy;
    public FourWheelerSpotManager(List<ParkingSpot> parkingSpots,ParkingSpotLookUpStrategy lookUpStrategy) {
        super(lookUpStrategy,parkingSpots);
    }
    @Override
    public ParkingSpot park() {
        return lookUpStrategy.getParkingSpot(parkingSpots);
    }

    @Override
    public boolean hasFreeSpot() {
        for (ParkingSpot spot : parkingSpots) {
            if (spot.isFree()) {
                return true;
            }
        }
        return false;
    }
}
