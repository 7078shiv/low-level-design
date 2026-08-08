package org.example.parkingLot;

import lombok.Data;

@Data
public class Vehicle {
    VehicleType type;
    String number;
    public Vehicle(VehicleType type, String number) {
        this.type = type;
        this.number = number;
    }
}
