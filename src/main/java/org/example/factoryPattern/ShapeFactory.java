package org.example.factoryPattern;

public class ShapeFactory {
    Shape getShape(String shapeType){
        return switch (shapeType) {
            case "CIRCLE" -> new Circle();
            case "Rectangle" -> new Rectangle();
            default -> null;
        };
    }
    // here adding a new shape means editing a factory -->  mild ocp voilation that what factory method fix

    // fix
    // Factory Pattern
    interface Burger{
        void serve();
    }

    static class VegBurger implements Burger{
        @Override
        public void serve() {
            System.out.println("VegBurger");
        }
    }

    static class NonVegBurger implements Burger{
        @Override
        public void serve() {
            System.out.println("NonVegBurger");
        }
    }

    static class NorthVegBurger implements Burger{
        @Override
        public void serve() {
            System.out.println("NorthVegBurger");
        }
    }



    abstract static class Resturant{
        abstract Burger getBurger();

        public void serve(){
            Burger burger = getBurger();
            burger.serve();
        }
    }

    static class VegResturant extends Resturant{
        @Override
        public Burger getBurger() {return new VegBurger();}
    }

    static class NonVegResturant extends Resturant{
        @Override
        Burger getBurger() {
            return new NonVegBurger();
        }
    }

    static class NorthVegResturant extends Resturant{
        @Override
        public Burger getBurger() {return new NorthVegBurger();}
    }

    public static void main(String[] args) {
        Resturant resturant = new VegResturant();
        resturant.serve();
        Resturant resturant1 = new NonVegResturant();
        resturant1.serve();
    }

    // now we can add a new class zero changes change in exsting class follow ocp.
}
