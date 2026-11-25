package com.wampus;

public class AgentRoom {
    public boolean isVisited = false;
    public boolean isSafe = false;

    public boolean noPit = false;
    public boolean noWumpus = false;

    public boolean possiblePit = false;
    public boolean possibleWumpus = false;

    public boolean definitePit = false;
    public boolean definiteWumpus = false;

    public boolean observedBreese = false;
    public boolean observedSmell = false;

    public void checkSafe() {
        if (noPit && noWumpus) {
            isSafe = true;
            possiblePit = false;
            possibleWumpus = false;
        }
    }
}