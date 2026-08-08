package org.example.lld.parkingLot;

public abstract class Vehical {
    VehicelType vehicalType;
    String licensePlateNo;

    Vehical(VehicelType vehicalType, String licensePlateNo) {
        this.vehicalType = vehicalType;
        this.licensePlateNo = licensePlateNo;
    }
    public VehicelType getVehicalType() {
        return vehicalType;
    }
    public String getLicensePlateNo() {
        return licensePlateNo;
    }
}
