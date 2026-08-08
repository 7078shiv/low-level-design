package org.example.Observerpattern;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObserverDesign {
    // Observer design pattern
    // when one object change its state then its subscriber will notify automatically

    // The problem it solves
    // a temperature will change then it should be display to both its phoneDisplay and Web-display
// bad design
//    class WheatherStation{
//        private PhoneDisplay phoneDisplay;
//        private WebDashbaord webDashbaord;
//
//        private void setTemperature(double temperature){
//            phoneDisplay.update(temperature);
//            webDashbaord.update(temperature);
//        }
//    }

    interface Observer{
        void update(double temperature);
    }

    interface Subject{
        void subscribe(Observer observer);
        void unsubscribe(Observer observer);
        void notifyObservers();
    }

    static class WheatherStation implements Subject{
        List<Observer> observers = new ArrayList<>();
        double temperature;

        @Override
        public void subscribe(Observer observer) {
            observers.add(observer);
        }

        @Override
        public void unsubscribe(Observer observer) {
            observers.remove(observer);
        }

        @Override
        public void notifyObservers() {
            for (Observer observer : observers) {
                observer.update(temperature);
            }
        }

        void settemperature(double temperature){
            this.temperature = temperature;
            notifyObservers();
        }
    }

    // concreate observer

    static class PhoneObserver implements Observer{
        @Override
        public void update(double temperature) {
            System.out.println("Phone temperature is :-"+temperature);
        }
    }

    static class WeberObserver implements Observer{
        @Override
        public void update(double temperature) {
            System.out.println("Web temperature is :-"+temperature);
        }
    }

    public static void main(String[] args) {
        WheatherStation wheatherStation = new WheatherStation();
        Observer phone = new PhoneObserver();
        Observer web = new WeberObserver();
        wheatherStation.subscribe(phone);
        wheatherStation.subscribe(web);
        wheatherStation.settemperature(30);  // this will notify both phone and web


        // notify me

        NotificationManager notificationManager = new NotificationManager();
        Observer sms = new SMS();
        Observer email = new EmailNotification();

        notificationManager.subscribe(sms);
        notificationManager.subscribe(email);

        notificationManager.updateStock(5);
    }


    // 2

    // Design Notify me like in Amazone website if any product is out of stock so if any user press notify me button so
    // whenever that product come back in stock it should be notify to all its users who pressed notify me button

    static class SMS implements Observer{
        @Override
        public void update(double quantity) {
            System.out.println("product back Stock quantity is :-"+quantity);
        }
    }

    static class EmailNotification implements Observer{
        @Override
        public void update(double quantity) {
            System.out.println("product back Stock quantity is :-"+quantity);
        }
    }

    static class NotificationManager implements Subject{
        List<Observer> observers = new CopyOnWriteArrayList<>();
        int productQuantity;

        @Override
        public void subscribe(Observer observer) {
            observers.add(observer);
        }

        @Override
        public void unsubscribe(Observer observer) {
            observers.remove(observer);
        }

        @Override
        public void notifyObservers() {
            for (Observer observer : observers) {
                observer.update(productQuantity);
            }
            observers.clear();  // after notify no need to notify it again and again
        }

        public void updateStock(int productQuantity){
            boolean isOutOfStock = this.productQuantity == 0;
            if(isOutOfStock && productQuantity > 0) {
                this.productQuantity = productQuantity;
                notifyObservers();
            }
        }
    }



}
