package org.example.lld.carRentalSystemLLD;

public interface BillingStrategy {
    Bill generateBill(Reservation reservation);
}
