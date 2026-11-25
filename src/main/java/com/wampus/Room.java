package com.wampus;

public class Room {
    private boolean hasWampus;
    private boolean hasPit;
    private boolean hasGold;

    public Room() {
        this.hasWampus = false;
        this.hasPit = false;
        this.hasGold = false;
    }

    public boolean hasWampus() {
        return hasWampus;
    }
    public void setWampus(boolean hasWampus) {
        this.hasWampus = hasWampus;
    }


    public boolean hasPit() {
        return hasPit;
    }
    public void setPit(boolean hasPit) {
        this.hasPit = hasPit;
    }


    public boolean hasGold() {
        return hasGold;
    }
    public void setGold(boolean hasGold) {
        this.hasGold = hasGold;
    }
}
