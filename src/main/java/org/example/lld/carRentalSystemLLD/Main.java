package org.example.lld.carRentalSystemLLD;

import org.example.ParkingLot2.enums.VehicalType;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        User user = new User(1,"shivang","1234");
        Vehical car = new Car(1,VehicalType.TwoWheeler,VehicalStatus.AVAILABLE);
        VehicalInventry vehicalInventry = new VehicalInventry();
        vehicalInventry.AddVehical(car);
        Location location = new Location();
        Store store = new Store(1,vehicalInventry,location);
        VehicalRentalSystem vehicalRentalSystem = new VehicalRentalSystem(List.of(user),List.of(store));

    }
}
