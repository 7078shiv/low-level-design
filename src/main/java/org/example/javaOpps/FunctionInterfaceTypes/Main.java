package org.example.javaOpps.FunctionInterfaceTypes;

public class Main {
    public static void main(String[] args) {
        Consumer<Integer> consumer = (Integer v)-> {
            if(v>10){
                System.out.println("logging");
            }
        };
        consumer.accept(1);

        Supplier<String> supplier = ()-> "this is supplier";

        supplier.get();

        Functional<Integer,String> isEven = (Integer s)-> {
            if(s%2==0){
                return "even";
            }
            return "odd";
        };

        isEven.apply(2);

        Predicate<Integer> isEvenNo = (Integer i)-> i % 2 == 0;

        isEvenNo.test(2);
    }
}
