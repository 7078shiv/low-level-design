package org.example.lld.carRentalSystemLLD;

import java.util.ArrayList;
import java.util.List;

public class Store {
    int id;
    VehicalInventry vehicalInventry;
    Location location;
    List<Reservation> reservationList;

    Store(int id, VehicalInventry vehicalInventry, Location location) {
        this.id = id;
        this.vehicalInventry = vehicalInventry;
        this.location = location;
        this.reservationList = new ArrayList<>();
    }
}
