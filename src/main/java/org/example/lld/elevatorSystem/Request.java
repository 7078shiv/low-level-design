package org.example.lld.elevatorSystem;

import lombok.Getter;

public class Request {
    @Getter
    int floor;
    @Getter
    Direction direction;
    boolean isInternal;

    Request(int floor, Direction direction, boolean isInternal) {
        this.floor = floor;
        this.direction = direction;
        this.isInternal = isInternal;
    }

    public static Request internalRequest(int floor){
        return new Request(floor,null,true);
    }

    public static Request externalRequest(int floor, Direction direction){
        return new Request(floor,direction,false);
    }
}
