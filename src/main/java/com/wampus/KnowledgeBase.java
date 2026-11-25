package com.wampus;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeBase {
    private final int size;
    private final AgentRoom[][] map;
    private Point definiteWumpusPos = null;
    private boolean wumpusKilled = false;

    public KnowledgeBase(int size) {
        this.size = size;
        this.map = new AgentRoom[size][size];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                map[x][y] = new AgentRoom();
            }
        }

        map[0][0].isSafe = true;
        map[0][0].noPit = true;
        map[0][0].noWumpus = true;
    }

    private AgentRoom getRoom(Point p) {
        return map[p.x()][p.y()];
    }

    public void registerMove(Point pos, Percept p) {
        if (p.scream() && !wumpusKilled) {
            wumpusKilled = true;
            definiteWumpusPos = null;
            markNoWumpusExcept(null);
        }

        AgentRoom current = getRoom(pos);
        current.isVisited = true;
        current.isSafe = true;
        current.noPit = true;
        current.noWumpus = true;
        current.observedBreese = p.breese();
        current.observedSmell = p.smell();

        List<Point> neighbors = getValidNeighbors(pos);

        // Анализ ветерка (ямы)
        if (!p.breese()) {
            for (Point n : neighbors) {
                AgentRoom neighbor = getRoom(n);
                neighbor.noPit = true;
                neighbor.checkSafe();
            }
        } else {
            for (Point n : neighbors) {
                AgentRoom neighbor = getRoom(n);
                if (!neighbor.noPit) {
                    neighbor.possiblePit = true;
                }
            }
        }

        // Анализ запаха (Вампус)
        if (!p.smell()) {
            for (Point n : neighbors) {
                AgentRoom neighbor = getRoom(n);
                neighbor.noWumpus = true;
                neighbor.checkSafe();
            }
        } else {
            for (Point n : neighbors) {
                AgentRoom neighbor = getRoom(n);
                if (!neighbor.noWumpus) {
                    neighbor.possibleWumpus = true;
                }
            }
        }

        deduceExactThreats();
    }

    private void deduceExactThreats() {
        boolean changed = true;

        while (changed) {
            changed = false;

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    Point pos = new Point(x, y);
                    AgentRoom room = getRoom(pos);

                    if (room.noPit && room.possiblePit) {
                        room.possiblePit = false;
                    }
                    if (room.noWumpus && room.possibleWumpus) {
                        room.possibleWumpus = false;
                    }

                    if (!room.isVisited)
                        continue;

                    List<Point> neighbors = getValidNeighbors(pos);

                    // Дедукция точной ямы (P!)
                    if (room.observedBreese) {
                        List<Point> pitCandidates = new ArrayList<>();
                        for (Point n : neighbors) {
                            if (!getRoom(n).noPit) {
                                pitCandidates.add(n);
                            }
                        }
                        if (pitCandidates.size() == 1) {
                            AgentRoom target = getRoom(pitCandidates.get(0));
                            if (!target.definitePit) {
                                target.definitePit = true;
                                target.possiblePit = false;
                                changed = true;
                            }
                        }
                    }

                    // Дедукция точного Вампуса (W!)
                    if (room.observedSmell) {
                        List<Point> wumpusCandidates = new ArrayList<>();
                        for (Point n : neighbors) {
                            if (!getRoom(n).noWumpus) {
                                wumpusCandidates.add(n);
                            }
                        }
                        if (wumpusCandidates.size() == 1) {
                            Point wPos = wumpusCandidates.get(0);
                            AgentRoom target = getRoom(wPos);
                            if (!target.definiteWumpus) {
                                target.definiteWumpus = true;
                                target.possibleWumpus = false;
                                definiteWumpusPos = wPos;
                                markNoWumpusExcept(wPos);
                                changed = true;
                            }
                        }
                    }
                }
            }
        }
    }

    private void markNoWumpusExcept(Point exclude) {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Point p = new Point(x, y);
                if (p.equals(exclude))
                    continue;

                AgentRoom room = getRoom(p);
                room.definiteWumpus = false;
                room.possibleWumpus = false;
                room.noWumpus = true;
                room.checkSafe();
            }
        }
    }

    public List<Point> getValidNeighbors(Point p) {
        List<Point> neighbors = new ArrayList<>();
        if (p.x() < size - 1)
            neighbors.add(new Point(p.x() + 1, p.y()));
        if (p.x() > 0)
            neighbors.add(new Point(p.x() - 1, p.y()));
        if (p.y() < size - 1)
            neighbors.add(new Point(p.x(), p.y() + 1));
        if (p.y() > 0)
            neighbors.add(new Point(p.x(), p.y() - 1));
        return neighbors;
    }

    public boolean isSafe(Point p) {
        return getRoom(p).isSafe;
    }

    public boolean isVisited(Point p) {
        return getRoom(p).isVisited;
    }

    // Пригодится охотнику, чтобы понять, куда стрелять
    public Point getDefiniteWumpus() {
        return definiteWumpusPos;
    }

    // Все безопасные клетки, где агент еще не был
    public List<Point> getSafeUnvisitedCells() {
        List<Point> result = new ArrayList<>();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (map[x][y].isSafe && !map[x][y].isVisited) {
                    result.add(new Point(x, y));
                }
            }
        }
        return result;
    }

    // Все известные безопасные клетки (нужно, чтобы найти позицию для выстрела)
    public List<Point> getSafeCells() {
        List<Point> result = new ArrayList<>();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (map[x][y].isSafe) {
                    result.add(new Point(x, y));
                }
            }
        }
        return result;
    }

}