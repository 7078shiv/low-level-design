package org.example.javaOpps;

@FunctionalInterface
public interface Bird {
    void fly();

    default void swim(){
        System.out.println("I am swimming");
    }

    static void run(){
        System.out.println("I am running");
    }

    String toString();  // its a Object class Method no need to override it in any class
}
