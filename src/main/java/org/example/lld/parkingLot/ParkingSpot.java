package org.example.lld.parkingLot;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicBoolean;

public class ParkingSpot {
    @Getter
    String id;
    Vehical vehical;
    @Getter
    SpotType spotType;
    AtomicBoolean isOccupy = new AtomicBoolean(false);

    ParkingSpot(String id, SpotType spotType){
        this.id = id;
        this.spotType = spotType;
    }

    public boolean parkVehical(Vehical vehical){
        if(!canFit(vehical)) return false;
        this.vehical = vehical;
        return isOccupy.compareAndSet(false,true);
    }

    public boolean unparkVehical(){
        isOccupy.compareAndSet(true,false);
        this.vehical = null;
        return true;
    }

    public boolean canFit(Vehical vehical){
        return switch(vehical.vehicalType){
            case CAR -> this.spotType != SpotType.SMALL;
            case BUS -> this.spotType == SpotType.LARGE;
            case MOTORCYCLE -> true;
        };
    }

}
