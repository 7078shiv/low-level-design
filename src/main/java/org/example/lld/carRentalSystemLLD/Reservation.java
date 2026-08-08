package org.example.lld.carRentalSystemLLD;

import java.time.LocalDateTime;

public class Reservation {
    int reservationId;
    User user;
    Vehical vehicle;
    LocalDateTime bookingDate;
    LocalDateTime fromBookingDate;
    LocalDateTime toBookingDate;
    ReservationStatus reservationStatus;

    Reservation(User user, Vehical vehicle, LocalDateTime fromBookingDate, LocalDateTime toBookingDate) {
        this.user = user;
        this.vehicle = vehicle;
        this.bookingDate = LocalDateTime.now();
        this.fromBookingDate = fromBookingDate;
        this.toBookingDate = toBookingDate;
        this.reservationStatus = ReservationStatus.SCHEDULED;
    }
}
