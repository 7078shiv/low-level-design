package org.example.strategypattern;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

public class Strategy {
// pick an algorithm at runtime by swapping Interchangeable object. instead of hardcoding if/else

    // Google Map same goal going A to B. but strategy can we different like ,by walk, car, bike etc.

    // Bad code

    class PaymentService{
        void pay(int amount, String type){
            if("cash".equals(type)){
                // pay by cash
            } else if ("upi".equals(type)){
                // pay by upi
            } else if ("credit card".equals(type)){
                // pay by credit card
            }
        }
    }     // breaking SRP and OCP principal also

    // Good code using strategy pattern

    interface PaymentStrategy{
        void pay(int amount);
    }

    static class CreditCardPayment implements PaymentStrategy{
        @Override
        public void pay(int amount) {
            System.out.println("Pay via credit card");
        }
    }

    class UpiPayment implements PaymentStrategy{
        @Override
        public void pay(int amount) {
            System.out.println("Pay via upi");
        }
    }

    static class PaymentServicePayment{
        PaymentStrategy paymentStrategy;
        PaymentServicePayment(PaymentStrategy paymentStrategy){
            this.paymentStrategy = paymentStrategy;
        }
        void pay(int amount){
            paymentStrategy.pay(amount);
        }
    }



    public static void main(String[] args) {
        PaymentServicePayment creditCardPayment = new PaymentServicePayment(new CreditCardPayment());
        creditCardPayment.pay(500);
    }

    // springBoot how to use it

    interface DiscountStrategy{
        double apply(double price);
    }

    @Component("premium")
    class PremiumDiscount implements DiscountStrategy{
        @Override
        public double apply(double price) {
            return price * 0.8;
        }
    }

    @Component("regular")
    class RegularDiscount implements DiscountStrategy{
        @Override
        public double apply(double price) {
            return price * 0.95;
        }
    }

    @Service
    class CheckoutService{
        // Spring inject all implementation into the Map keyed by Bean name
        Map<String,DiscountStrategy> discountStrategies;

        CheckoutService(Map<String,DiscountStrategy> discountStrategies){
            this.discountStrategies = discountStrategies;
        }

        double checkout(String customerType, double price) {
            if (discountStrategies == null){ throw new IllegalStateException("discountStrategies is null"); }
            return discountStrategies.get(customerType).apply(price);
        }
    }


}
