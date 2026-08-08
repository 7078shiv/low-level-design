package org.example.lld.parkingLot;
public interface PricingStrategy {
    double calculatePrice(Vehical vehicle,int hours);
}
