package org.example.Observerpattern.Observer;

import org.example.Observerpattern.Observable.StockObservable;

public class EmailAlertObserverImpl implements NotificationObserver {
    String email;
    StockObservable stockObservable;
    public EmailAlertObserverImpl(String email, StockObservable stockObservable) {
        this.email = email;
        this.stockObservable = stockObservable;
    }
    @Override
    public void update() {
        sendMail(email,"product is in stock hurry up");
    }

    private void sendMail(String email, String s) {
        System.out.println("Sending email to " + email);
        System.out.println(s);
    }
}
