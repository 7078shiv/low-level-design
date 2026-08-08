package org.example.javaOpps.Collection;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.PriorityBlockingQueue;

public class Main {
    public static void main(String[] args) {
        ArrayDeque<Integer> deque = new ArrayDeque<>();
        deque.offer(1);
        deque.offer(2);
        deque.offer(3);

        System.out.println(deque.pollFirst());
        System.out.println(deque.pollLast());

        // Thread safe version of PriorityQueue
        PriorityBlockingQueue<Integer> queue = new PriorityBlockingQueue<>();

        // Thread safe version of ArrayDeque
        ConcurrentLinkedDeque<Integer> concurrentLinkedDeque = new ConcurrentLinkedDeque<>();

        HashMap<Integer,String> map = new HashMap<Integer,String>();

        map.put(null,"TEST");
        map.put(0,null);
        map.putIfAbsent(0,"TEST");


        for(Map.Entry<Integer,String> entry : map.entrySet()){
            Integer key = entry.getKey();
            String value = entry.getValue();
            System.out.println(key+":"+value);
        }


    }
}
