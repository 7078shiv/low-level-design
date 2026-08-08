package org.example.Observerpattern.Observable;

import org.example.Observerpattern.Observer.NotificationObserver;

import java.util.ArrayList;
import java.util.List;

public class IphoneObservableImpl implements StockObservable {

    List<NotificationObserver> observers = new ArrayList<>();
    int stockCount = 0;

    @Override
    public void add(NotificationObserver observer) {
        observers.add(observer);
    }

    @Override
    public void remove(NotificationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifySubscribers() {
        for (NotificationObserver observer : observers) {
            observer.update();
        }
    }

    @Override
    public void setStockCount(int stockCount) {
        if(this.stockCount == 0){
            notifySubscribers();
        }
        this.stockCount += stockCount;
    }

    @Override
    public int getStockCount() {
        return stockCount;
    }
}
