package org.example.Observerpattern;

import org.example.Observerpattern.Observable.IphoneObservableImpl;
import org.example.Observerpattern.Observable.StockObservable;
import org.example.Observerpattern.Observer.EmailAlertObserverImpl;
import org.example.Observerpattern.Observer.MoileAlertObservable;
import org.example.Observerpattern.Observer.NotificationObserver;

public class Main {
    public static void main(String[] args) {
        StockObservable iphoneStockObservable = new IphoneObservableImpl();
        NotificationObserver o1 = new MoileAlertObservable("xyz@123",iphoneStockObservable);
        NotificationObserver o2 = new EmailAlertObserverImpl("shivangshrivastava123@gmail.com",iphoneStockObservable);

        iphoneStockObservable.add(o1);
        iphoneStockObservable.add(o2);
        iphoneStockObservable.setStockCount(10);

    }
}
