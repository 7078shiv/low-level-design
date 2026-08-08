package org.example.ParkingLot2.entity;

public class ParkingSpot {
    private final String spotId;
    private boolean isFree;
    ParkingSpot(String spotId) {
        this.spotId = spotId;
        this.isFree = true;
    }

    public String getSpotId() {
        return spotId;
    }
    public boolean isFree() {
        return isFree;
    }
    public void occupySpot(){
        isFree = false;
    }
    public void releaseSpot() {
        isFree = true;
    }
}
