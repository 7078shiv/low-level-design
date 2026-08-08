package org.example.ParkingLot2.entity;

import org.example.ParkingLot2.Strategy.ParkingSpotLookUpStrategy;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ParkingSpotManager {
    private final List<ParkingSpot> parkingSpots;
    private final ParkingSpotLookUpStrategy parkingSpotLookUpStrategy;
    private final ReentrantLock lock = new ReentrantLock(true);

    ParkingSpotManager(ParkingSpotLookUpStrategy parkingSpotLookUpStrategy,List<ParkingSpot> parkingSpots) {
        this.parkingSpotLookUpStrategy = parkingSpotLookUpStrategy;
        this.parkingSpots = parkingSpots;
    }

    public ParkingSpot park(){
        lock.lock();
        try {
            ParkingSpot parkingSpot = parkingSpotLookUpStrategy.getParkingSpot(parkingSpots);
            if (parkingSpot == null) {
                return null;
            }
            parkingSpot.occupySpot();
            return parkingSpot;
        } finally {
            lock.unlock();
        }
    }

    public void unPark(ParkingSpot spot) {
        lock.lock();
        try {
            spot.releaseSpot();
        } finally {
            lock.unlock();
        }
    }

    public boolean hasFreeSpot() {
        lock.lock();
        try {
            return parkingSpots.stream().anyMatch(ParkingSpot::isFree);
        } finally {
            lock.unlock();
        }
    }

}
