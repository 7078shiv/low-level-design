package org.example.parkingLot;

public class ParkingSlot {
    int id;
    boolean isOccupied;
    Vehicle vehicle;
    VehicleType vehicleType;

    public ParkingSlot(int id, VehicleType vehicleType) {
        this.id = id;
        this.vehicleType = vehicleType;
        this.isOccupied = false;
    }

    public boolean park(Vehicle vehicle) {
        if(!isOccupied && vehicle.type == vehicleType) {
            this.vehicle = vehicle;
            this.isOccupied = true;
            return true;
        }
        return false;
    }

    public void unpark() {
        this.vehicle = null;
        this.isOccupied = false;
    }
}
