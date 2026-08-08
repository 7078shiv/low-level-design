package org.example.decoratorpattern.DecoratorDesign;

public class Decorator {

    // Decorator pattern

    // Wrap up an object to add exact behaviour at runtime. Without changing its class to exploded or exploded in subclasses

//    You sell coffee. Base coffee + optional add-ons (milk, sugar, caramel, whip...).
//
//    Bad (subclass explosion):
//
//    java
//    class Coffee { }
//    class CoffeeWithMilk extends Coffee { }
//    class CoffeeWithMilkAndSugar extends Coffee { }
//    class CoffeeWithMilkAndSugarAndCaramel extends Coffee { }

    interface Coffee{
        int price();
        void description();
    }

    static class PlainCoffee implements Coffee{
        @Override
        public int price() {
            return 50;
        }

        @Override
        public void description() {
            System.out.println("Coffee is a plain coffee");
        }
    }

    abstract static class CoffeeDecorator implements Coffee{
        Coffee coffee;
        public CoffeeDecorator(Coffee coffee) {
            this.coffee = coffee;
        }
    }

    static class MilkDecorator extends CoffeeDecorator{
        public MilkDecorator(Coffee coffee) {
            super(coffee);
        }

        @Override
        public int price() {
            return coffee.price()+10;
        }

        @Override
        public void description() {
            coffee.description();
            System.out.println("Addon MilK");
        }
    }


    static class Capachenu extends CoffeeDecorator{
        public Capachenu(Coffee coffee) {
            super(coffee);
        }

        @Override
        public int price() {
            return coffee.price()+15;
        }

        @Override
        public void description() {
            coffee.description();
            System.out.println("Addon Capachenu");
        }
    }

    public static void main(String[] args) {
        Coffee coffee = new Capachenu(new MilkDecorator(new PlainCoffee()));
        System.out.println(coffee.price());
        coffee.description();
    }


}
