package org.example.ParkingLot2.entity;

import org.example.ParkingLot2.enums.VehicalType;
import org.example.parkingLot.VehicleType;

import java.util.Map;

public class ParkingLevel {
    int levelNo;
    Map<VehicalType,ParkingSpotManager> managers;
    public ParkingLevel(int levelNo, Map<VehicalType,ParkingSpotManager> managers) {
        this.levelNo = levelNo;
        this.managers = managers;
    }
    public boolean hasAvailableSpot() {
        for(VehicalType type : managers.keySet()){
            ParkingSpotManager manager = managers.get(type);
            if(manager != null && manager.hasFreeSpot()){
                return true;
            }
        }
        return false;
    }

    public ParkingSpot parkVehicle(VehicleType type){
        ParkingSpotManager parkingSpotManager = managers.get(type);
        if(parkingSpotManager == null){
            return null;
        }
        return parkingSpotManager.park();
    }

    public void unPark(VehicleType type, ParkingSpot spot){
        ParkingSpotManager parkingSpotManager = managers.get(type);
        if(parkingSpotManager == null){
            return;
        }
        parkingSpotManager.unPark(spot);
    }
}
