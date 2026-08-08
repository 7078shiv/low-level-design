package org.example.decoratorpattern;

public class Main {
    public static void main(String[] args) {
        BasePizza pizza = new Mushroom(new Extracheese(new FarmHouse()));
        System.out.println(pizza.cost());
        BasePizza pizza1 = new Mushroom(new Extracheese(new Margheirta()));
        System.out.println(pizza1.cost());
    }
}
