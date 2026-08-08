package org.example.lld.carRentalSystemLLD;

import org.example.ParkingLot2.enums.VehicalType;

public abstract class Vehical {
    int id;
    VehicalType vehicalType;
    VehicalStatus status;
    int rentCost;

    Vehical(int id, VehicalType vehicalType, VehicalStatus status) {
        this.id = id;
        this.vehicalType = vehicalType;
        this.status = status;
    }
}
