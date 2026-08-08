package org.example.Observerpattern.Observable;

import org.example.Observerpattern.Observer.NotificationObserver;

public interface StockObservable {
    public void add(NotificationObserver observer);
    public void remove(NotificationObserver observer);
    public void notifySubscribers();
    public void setStockCount(int stockCount);
    public int getStockCount();
}
