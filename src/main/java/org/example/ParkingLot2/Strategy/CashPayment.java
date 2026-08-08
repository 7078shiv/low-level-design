package org.example.ParkingLot2.Strategy;

public class CashPayment implements Payment {
    @Override
    public void pay() {
        System.out.println("Payment successful Via Cash Payment");
    }
}
