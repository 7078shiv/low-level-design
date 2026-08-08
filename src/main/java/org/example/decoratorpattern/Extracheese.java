package org.example.decoratorpattern;

public class Extracheese extends ToppingDecorator {
    BasePizza pizza;
    public Extracheese(BasePizza pizza) {
        this.pizza = pizza;
    }
    @Override
    public int cost() {
        return pizza.cost()+10;
    }
}
