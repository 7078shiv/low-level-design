package org.example.parkingLot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        // Step 1: Create Slots
        List<ParkingSlot> floor1Slots = new ArrayList<>();
        floor1Slots.add(new ParkingSlot(1, VehicleType.CAR));
        floor1Slots.add(new ParkingSlot(2, VehicleType.BIKE));

        List<ParkingSlot> floor2Slots = new ArrayList<>();
        floor2Slots.add(new ParkingSlot(3, VehicleType.CAR));
        floor2Slots.add(new ParkingSlot(4, VehicleType.TRUCK));

        // Step 2: Create Floors
        Floor floor1 = new Floor(1, floor1Slots);
        Floor floor2 = new Floor(2, floor2Slots);

        // Step 3: Create Parking Lot
        ParkingLot parkingLot = new ParkingLot(Arrays.asList(floor1, floor2));

        // Step 4: Create Vehicle
        Vehicle car = new Vehicle( VehicleType.CAR,"KA-01-1234");
        Vehicle bike = new Vehicle( VehicleType.CAR,"KA-01-1234");
        // Step 5: Park Vehicle
        Ticket ticket = parkingLot.parkVehicle(car);
        Ticket ticket2 = parkingLot.parkVehicle(bike);

        if (ticket != null) {
            System.out.println("Vehicle parked. Ticket ID: " + ticket.getTicketId());
        } else {
            System.out.println("Parking Full");
        }

        if (ticket != null) {
            System.out.println("Vehicle parked. Ticket ID: " + ticket.getTicketId());
        } else {
            System.out.println("Parking Full");
        }

        // Step 6: Unpark Vehicle
        parkingLot.unpark(ticket);
        System.out.println("Vehicle unparked.");
    }

//    public static int[][] merge(int[][] intervals) {
//        if (intervals.length == 1) {
//            return intervals;
//        }
//        Arrays.sort(intervals, (a, b) -> {
//            if (a[0] == b[0])
//                return a[1] - b[1];
//            else return a[0] - b[0];
//        });
//        List<int[]> result = new ArrayList<>();
//        result.add(intervals[0]);
//        for (int i = 1; i < intervals.length; i++) {
//            int[] curr = intervals[i];
//            int[] last = result.get(result.size() - 1);
//            if (curr[0] > last[1]) {
//                result.add(intervals[i]);
//            } else if (curr[1] >= last[1]) {
//                last[1] = Math.max(last[1], intervals[i][1]);
//            }
//        }
//        int[][] ans = new int[result.size()][2];
//        for (int i = 0; i < result.size(); i++) {
//            ans[i] = result.get(i);
//        }
//        return ans;
//    }

//    public int rob(int[] nums) {
//
//    }
}