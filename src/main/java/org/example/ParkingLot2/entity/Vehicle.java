package org.example.ParkingLot2.entity;

import lombok.Getter;
import org.example.parkingLot.VehicleType;

public class Vehicle {
    @Getter
    private String vehicleNo;
    @Getter
    private VehicleType vehicleType;

    public Vehicle(String vehicleNo, VehicleType vehicleType) {
        this.vehicleNo = vehicleNo;
        this.vehicleType = vehicleType;
    }
}
