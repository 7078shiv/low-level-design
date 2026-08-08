package org.example.lld.carRentalSystemLLD;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BillManager {
    BillingStrategy billingStrategy;

    Map<Integer,Bill> billMap = new ConcurrentHashMap<>();

    public BillManager(BillingStrategy billingStrategy) {
        this.billingStrategy = billingStrategy;
    }

    public Bill generateBill(Reservation reservation){
        Bill bill = billingStrategy.generateBill(reservation);
        billMap.put(bill.billId,bill);
        return bill;
    }

    public Bill getBill(int billId) {
        return billMap.get(billId);
    }

}
