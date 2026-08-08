package org.example.lld.carRentalSystemLLD;

import org.example.ParkingLot2.enums.VehicalType;

public class Bike extends Vehical {
    Bike(int id, VehicalType vehicalType, VehicalStatus status) {
        super(id, vehicalType, status);
        this.rentCost = 50;
    }
}
