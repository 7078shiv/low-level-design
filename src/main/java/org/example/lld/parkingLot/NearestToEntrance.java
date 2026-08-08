package org.example.lld.parkingLot;

import java.util.List;

public class NearestToEntrance implements SpotAllocationStrategy{
    @Override
    public ParkingSpot allocateSpots(List<ParkingFloor> parkingFloorList, Vehical vehical) {
        for(ParkingFloor parkingFloor : parkingFloorList){
            ParkingSpot spot = parkingFloor.parkVehical(vehical);
            if(spot != null){
                return spot;
            }
        }
        return null;
    }
}
