package com.wampus;

import java.util.ArrayList;
import java.util.Random;
import java.util.List;

public class World {
    private final int size;
    private final Room[][] grid;
    private final Random random;
    private boolean wampusAlive = true;

    public World(int size) {
        this.size = size;
        this.grid = new Room[size][size];
        this.random = new Random();
        initWorld();
    }

    private void initWorld() {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                grid[x][y] = new Room();
            }
        }

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (x == 0 && y == 0) {
                    continue;
                }
                if (random.nextDouble() < 0.2) {
                    grid[x][y].setPit(true);
                }
            }
        }

        int x1, y1;
        do {
            x1 = random.nextInt(size);
            y1 = random.nextInt(size);
        } while (x1 == 0 && y1 == 0);
        grid[x1][y1].setWampus(true);

        int[][] offsets = {{1,0},{-1,0},{0,1},{0,-1}};
        List<Point> candidates = new ArrayList<>();

        for (int[] off : offsets) {
            int x2 = x1 + off[0];
            int y2 = y1 + off[1];
            if (x2 >= 0 && x2 < size && y1 >=0 && y2 < size && !(x2 == 0 && y2 == 0)){
                candidates.add(new Point(x2, y2));
            }
        }
        Point goldPos = candidates.get(random.nextInt(candidates.size()));
        grid[goldPos.x()][goldPos.y()].setGold(true);
    }

    public Percept getPercept(int x, int y) {
        boolean smell = false;
        boolean breese = false;
        boolean glitter = grid[x][y].hasGold();
        boolean bump = false;
        boolean scream = !wampusAlive;

        int[][] neighbors = { { x, y + 1 }, { x, y - 1 }, { x + 1, y }, { x - 1, y } };

        for (int[] n : neighbors) {
            int nx = n[0];
            int ny = n[1];

            if (nx >= 0 && nx < size && ny >= 0 && ny < size) {
                if (grid[nx][ny].hasWampus()) {
                    smell = true;
                }
                if (grid[nx][ny].hasPit()) {
                    breese = true;
                }
            }
        }

        return new Percept(smell, breese, glitter, bump, scream);
    }

    public Room getRoom(int x, int y) {
        return grid[x][y];
    }

    public int getSize() {
        return size;
    }

    public boolean isDangerous(Point p) {
        Room r = grid[p.x()][p.y()];
        return r.hasPit() || (r.hasWampus() && wampusAlive);
    }

    public boolean grabGold(Point p) {
        if (grid[p.x()][p.y()].hasGold()) {
            grid[p.x()][p.y()].setGold(false);
            return true;
        }
        return false;
    }

    // Стрела летит по прямой до Вампуса или до стены
    public boolean shootArrow(Point start, Direction dir) {
        int x = start.x() + dir.dx;
        int y = start.y() + dir.dy;

        while (x >= 0 && x < size && y >= 0 && y < size) {
            if (grid[x][y].hasWampus() && wampusAlive) {
                wampusAlive = false;
                return true; // Попали! (Scream)
            }
            x += dir.dx;
            y += dir.dy;
        }
        return false; // Стрела улетела в стену
    }

}
