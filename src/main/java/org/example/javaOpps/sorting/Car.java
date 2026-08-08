package org.example.javaOpps.sorting;

import java.util.Comparator;

public class Car implements Comparator<Car> {
    public String carType;
    public String name;

    public Car(String carType, String name) {
        this.carType = carType;
        this.name = name;
    }
    public Car() {}


    @Override
    public int compare(Car o1, Car o2) {
        return o1.carType.compareTo(o2.carType);
    }
}
