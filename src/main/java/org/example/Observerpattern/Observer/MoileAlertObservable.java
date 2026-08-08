package org.example.Observerpattern.Observer;

import org.example.Observerpattern.Observable.StockObservable;

public class MoileAlertObservable implements NotificationObserver {
    String userName;
    StockObservable stockObservable;
    public MoileAlertObservable(String userName, StockObservable stockObservable) {
        this.userName = userName;
        this.stockObservable = stockObservable;
    }
    @Override
    public void update() {
        sendMsgViaMobile(userName,"Product is in stock hurry up!");
    }

    private void sendMsgViaMobile(String userName, String s) {
        System.out.println("message send to :- "+userName+" : "+s);
    }
}
