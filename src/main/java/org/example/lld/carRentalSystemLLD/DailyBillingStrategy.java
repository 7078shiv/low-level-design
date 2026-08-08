package org.example.lld.carRentalSystemLLD;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DailyBillingStrategy implements BillingStrategy {
    private final AtomicInteger counter = new AtomicInteger(5000);
    @Override
    public Bill generateBill(Reservation reservation) {
        long days = ChronoUnit.DAYS.between(
                reservation.fromBookingDate,
                reservation.toBookingDate
        ) + 1;
        int amount = Math.toIntExact(days * reservation.vehicle.rentCost);
        return new Bill(counter.getAndIncrement(),reservation.reservationId,amount);
    }
}
