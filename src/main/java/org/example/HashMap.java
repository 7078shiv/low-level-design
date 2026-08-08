package org.example;

import java.util.ArrayList;
import java.util.List;

public class HashMap<K,V> {
    public class Node{
        K key;
        V value;
        Node next;
        Node(K key,V value){
            this.key = key;
            this.value = value;
        }
    }
    int size;
    List<Node> bucket = new ArrayList<>();
    public HashMap(){
        this(4);
    }
    public HashMap(int n){
        for(int i=0;i<n;i++){
            bucket.add(null);
        }
    }

    public void put(K key, V value) {
        int index = getBucketIndex(key);
        Node oldNode = bucket.get(index);
        while (oldNode != null) {
            if (oldNode.key.equals(key)) {
                oldNode.value = value;
                return;
            }
            oldNode = oldNode.next;
        }
        Node newNode = new Node(key, value);
        newNode.next = bucket.get(index);
        bucket.set(index, newNode);
        size++;
        double thresHoldFactor = 2.0;
        if(thresHoldFactor < (double) size /bucket.size()){
            // rehashing
        }
    }

    private int getBucketIndex(K key) {
        int index = key.hashCode()%bucket.size();
        return index<0 ? index+bucket.size() : index;
    }

}
