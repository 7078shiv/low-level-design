package org.example.lld.parkingLot;

public class HourlyBasedPricingStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(Vehical vehicle,int hours) {
        return switch (vehicle.vehicalType){
            case CAR -> 10*hours;
            case BUS -> 20*hours;
            case MOTORCYCLE -> 50*hours;
        };
    }
}
