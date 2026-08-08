package org.example.lld.carRentalSystemLLD;

public class Bill {
    int billId;
    int reservationId;
    int amount;
    boolean isPaid;
    public Bill(int billId, int reservationId, int amount) {
        this.billId = billId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.isPaid = false;
    }
}
