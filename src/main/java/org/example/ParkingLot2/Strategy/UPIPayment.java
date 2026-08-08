package org.example.ParkingLot2.Strategy;

public class UPIPayment implements Payment {
    @Override
    public void pay() {
        System.out.println("Payment successful Via UPI Payment");
    }
}
