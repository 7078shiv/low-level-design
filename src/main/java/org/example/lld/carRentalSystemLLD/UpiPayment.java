package org.example.lld.carRentalSystemLLD;

public class UpiPayment implements Payment{
    Bill bill;
    @Override
    public int payBill() {
        System.out.println("Bill paid amount :-"+bill.amount);
        return bill.amount;
    }
}
