package com.wampus;


public enum Direction {
    NORTH(0, 1),  // Вверх (y + 1)
    EAST(1, 0),   // Вправо (x + 1)
    SOUTH(0, -1), // Вниз (y - 1)
    WEST(-1, 0);  // Влево (x - 1)

    public final int dx;
    public final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public Direction turnRight() {
        return values()[(ordinal() + 1) % 4];
    }

    public Direction turnLeft() {
        return values()[(ordinal() + 3) % 4];
    }
}