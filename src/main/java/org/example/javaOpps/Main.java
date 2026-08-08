package org.example.javaOpps;

public class Main {
    public static void main(String[] args) {
        //String[] s = new String[1000000000*1000000000*999999999*999999999];

        // implementation of functional interface using anonymous class
        Bird bird = new Bird(){
            @Override
            public void fly() {
                System.out.println("Flying!");
            }
        };  // known as anonymous class


        // implementation of functional interface using lamda expression

        Bird bird2 = ()-> System.out.println("Flying!");
        bird.fly();

    }
}
