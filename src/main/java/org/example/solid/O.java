package org.example.solid;

public class O {
    // Open Close Principal

    // it states that Open for extension but close for modification.
    // You should be able to add new behaviour without editing existing code

    // bad code

    class DiscountService{
        public double getDiscount(String customerType){
            if("Regular".equals(customerType)){return 1.0;}
            else if("Premium".equals(customerType)){return 2.0;}
            else return 0;
        }
    }

    // Good Code

    public interface  DiscountServiceInterface{
        double getDiscount(String customerType);
    }

    class RegularDiscount implements DiscountServiceInterface{

        @Override
        public double getDiscount(String customerType) {
            return 1.0;
        }
    }

    class PremiumDiscount implements DiscountServiceInterface{
        @Override
        public double getDiscount(String customerType) {return 2.0;}
    }



}
