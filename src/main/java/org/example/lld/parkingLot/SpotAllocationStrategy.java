package org.example.lld.parkingLot;


import java.util.List;

public interface SpotAllocationStrategy {
    ParkingSpot allocateSpots(List<ParkingFloor> parkingFloors, Vehical vehical);
}
