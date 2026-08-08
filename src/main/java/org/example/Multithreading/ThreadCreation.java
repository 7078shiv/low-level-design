package org.example.Multithreading;

public class ThreadCreation implements Runnable{
    @Override
    public void run() {
        System.out.println("Thread creation started "+Thread.currentThread().getName());
    }
}
