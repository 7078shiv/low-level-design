package org.example.lld.parkingLot;



public class VehicalFactory {
    public Vehical getVehical(String licensePlateNo, VehicelType vehicelType) {
        return switch (vehicelType) {
            case MOTORCYCLE -> new MoterCycle(licensePlateNo);
            case CAR -> new Car(licensePlateNo);
            case BUS -> new Bus(licensePlateNo);
        };
    }
}
