package org.example.ParkingLot2.Strategy;

import org.example.ParkingLot2.entity.ParkingSpot;

import java.util.List;

public interface ParkingSpotLookUpStrategy {
    ParkingSpot getParkingSpot(List<ParkingSpot> parkingSpots);
}
