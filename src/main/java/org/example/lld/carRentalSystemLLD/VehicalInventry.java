package org.example.lld.carRentalSystemLLD;

import java.util.ArrayList;
import java.util.List;

public class VehicalInventry {
    List<Vehical> vehicalList;

    VehicalInventry(){
        vehicalList = new ArrayList<>();
    }

    void AddVehical(Vehical vehical){
        vehicalList.add(vehical);
    }

    Vehical GetVehical(int id){
        for(Vehical vehical : vehicalList){
            if(vehical.id == id){
                return vehical;
            }
        }
        return null;
    }

    Vehical removeVehical(int id){
        for(Vehical vehical : vehicalList){
            if(vehical.id == id){
                vehicalList.remove(vehical);
            }
        }
        return null;
    }
}
