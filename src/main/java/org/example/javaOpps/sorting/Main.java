package org.example.javaOpps.sorting;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Integer[] arr = {1,5,4,3,2,6,7,9,2,3,3,3,3,4,1,1,2};
        int[][] arr1 = new int[2][3];
        Arrays.sort(arr1, Comparator.comparingInt(a -> a[0]));
        Arrays.sort(arr,(Integer a,Integer b)->b-a);

        List<Car> cars = new ArrayList<>();
        cars.add(new Car("Honda","ve"));
        cars.add(new Car("Maruti Suzuki","s-presso"));
        cars.add(new Car("Tata","Thar"));

        Collections.sort(cars,(c1,c2)->c2.name.compareTo(c1.name));

        cars.sort((c1, c2) -> c2.name.compareTo(c1.name));

        cars.sort(new CarNameComparator());

        cars.sort(new Car());
    }
}
