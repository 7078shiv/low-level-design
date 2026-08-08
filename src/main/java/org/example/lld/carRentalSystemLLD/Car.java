package org.example.lld.carRentalSystemLLD;

import org.example.ParkingLot2.enums.VehicalType;

public class Car extends Vehical {
    Car(int id, VehicalType vehicalType, VehicalStatus status) {
        super(id, vehicalType, status);
        this.rentCost = 100;
    }
}
