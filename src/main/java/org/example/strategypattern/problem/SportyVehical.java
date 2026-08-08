package org.example.strategypattern.problem;

public class SportyVehical extends Vechecle {
    @Override
    public void drive(){
        System.out.println("special drive"); // problem is that code duplicacy --> this method is same as in OffRoad vevhecle class.
    }
}
