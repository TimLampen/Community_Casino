package me.timlampen.cc.jackpot;

import java.util.UUID;

/**
 * Created by Primary on 9/4/2016.
 */
public class JackpotRange {

    double min;
    double max;
    String name;
    UUID uuid;

    public JackpotRange(UUID uuid, String name, double min, double max){
        this.min = min;
        this.max = max;
        this.name = name;
        this.uuid = uuid;
    }
}
