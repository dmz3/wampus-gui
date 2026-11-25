package com.wampus;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Hunter {
    private static final Point START = new Point(0, 0);

    private enum Action {
        TURN_LEFT, TURN_RIGHT, FORWARD, GRAB, SHOOT
    }

    private Point pos = START;
    private Direction dir = Direction.EAST;
    private boolean hasGold = false;
    private boolean hasArrow = true;
    private boolean alive = true;
    private boolean finished = false;
    private int score = 0;

    private final World world;
    private final KnowledgeBase kb;
    private final Pathfinder pathfinder;

    private Percept currentPercept;
    private final Queue<Action> actionQueue = new LinkedList<>();

    public Hunter(World world) {
        this.world = world;
        this.kb = new KnowledgeBase(world.getSize());
        this.pathfinder = new Pathfinder();
    }

    public void initStart() {
        currentPercept = world.getPercept(pos.x(), pos.y());
        kb.registerMove(pos, currentPercept);
        System.out.println("Охотник стартует в " + pos + ", направление: " + dir);
        System.out.println("Клетка " + pos + ". Сенсоры: " + currentPercept);
    }

    // Один вызов step() = ровно одно действие (поворот, шаг, выстрел, подбор или
    // выход)
    public void step() {
        if (!alive || finished)
            return;

        if (actionQueue.isEmpty()) {
            planNextActions();
        }

        if (!actionQueue.isEmpty()) {
            executeAction(actionQueue.poll());
        }
    }

    private void planNextActions() {
        // 1. В текущей клетке блеск — забираем золото
        if (currentPercept.glitter() && !hasGold) {
            actionQueue.add(Action.GRAB);
            return;
        }

        // 2. Точно знаем, где Вампус, и есть стрела — планируем выход на линию огня и
        // выстрел
        Point wumpusPos = kb.getDefiniteWumpus();
        if (hasArrow && wumpusPos != null) {
            if (planShootWumpus(wumpusPos)) {
                return;
            }
        }

        // 3. Ищем ближайшую безопасную непосещённую клетку
        List<Point> bestPath = findClosestPath(kb.getSafeUnvisitedCells());
        if (bestPath != null) {
            enqueuePath(bestPath);
            return;
        }

        // 4. Безопасных ходов нет - игра стоп
        System.out.println("Нет возможных ходов, пересоздайте мир. Итоговый счет: " + score);
        finished = true;
    }

    private void executeAction(Action action) {
        switch (action) {
            case TURN_LEFT -> turnLeft();
            case TURN_RIGHT -> turnRight();
            case FORWARD -> {
                moveForward();
                if (alive) {
                    currentPercept = world.getPercept(pos.x(), pos.y());
                    kb.registerMove(pos, currentPercept);
                    System.out.println("Клетка: " + pos + ". Сенсоры: " + currentPercept);
                    // Если по пути наткнулись на золото — сбрасываем текущий план, чтобы сразу
                    // забрать его
                    if (currentPercept.glitter() && !hasGold) {
                        actionQueue.clear();
                    }
                }
            }
            case GRAB -> {
                grabGold();
            }
            case SHOOT -> {
                shoot();
                currentPercept = world.getPercept(pos.x(), pos.y());
                kb.registerMove(pos, currentPercept);
            }
        }
    }

    // --- Планирование цепочек действий ---

    private Direction enqueuePath(List<Point> path) {
        Point simPos = pos;
        Direction simDir = dir;

        for (Point next : path) {
            Direction neededDir = getDirectionTo(simPos, next);
            simDir = enqueueTurns(simDir, neededDir);
            actionQueue.add(Action.FORWARD);
            simPos = next;
        }
        return simDir;
    }

    private Direction enqueueTurns(Direction currentDir, Direction targetDir) {
        if (currentDir == targetDir)
            return currentDir;
        if (currentDir.turnLeft() == targetDir) {
            actionQueue.add(Action.TURN_LEFT);
        } else if (currentDir.turnRight() == targetDir) {
            actionQueue.add(Action.TURN_RIGHT);
        } else {
            actionQueue.add(Action.TURN_RIGHT);
            actionQueue.add(Action.TURN_RIGHT);
        }
        return targetDir;
    }

    private boolean planShootWumpus(Point wumpusPos) {
        List<Point> shootingSpots = kb.getSafeCells().stream()
                .filter(p -> p.x() == wumpusPos.x() || p.y() == wumpusPos.y())
                .toList();

        List<Point> pathToSpot = findClosestPath(shootingSpots);
        if (pathToSpot == null)
            return false;

        Direction finalDir = enqueuePath(pathToSpot);
        Point shootFrom = pathToSpot.isEmpty() ? pos : pathToSpot.get(pathToSpot.size() - 1);
        Direction aim = getDirectionTo(shootFrom, wumpusPos);
        enqueueTurns(finalDir, aim);
        actionQueue.add(Action.SHOOT);
        return true;
    }

    private List<Point> findClosestPath(List<Point> targets) {
        List<Point> shortest = null;
        for (Point target : targets) {
            List<Point> path = pathfinder.findPath(pos, target, kb);
            if (path != null && (shortest == null || path.size() < shortest.size())) {
                shortest = path;
            }
        }
        return shortest;
    }

    private Direction getDirectionTo(Point from, Point to) {
        if (to.x() > from.x())
            return Direction.EAST;
        if (to.x() < from.x())
            return Direction.WEST;
        if (to.y() > from.y())
            return Direction.NORTH;
        return Direction.SOUTH;
    }

    // --- Элементарные действия (приводы) ---

    private void turnLeft() {
        dir = dir.turnLeft();
        score -= 1;
        System.out.println("Повернул налево (теперь смотрю на " + dir + ")");
    }

    private void turnRight() {
        dir = dir.turnRight();
        score -= 1;
        System.out.println("Повернул направо (теперь смотрю на " + dir + ")");
    }

    private void moveForward() {
        score -= 1;
        int nx = pos.x() + dir.dx;
        int ny = pos.y() + dir.dy;

        if (nx < 0 || nx >= world.getSize() || ny < 0 || ny >= world.getSize()) {
            System.out.println("Уперся в стену.");
            return;
        }

        pos = new Point(nx, ny);
        System.out.println("Вперед в " + pos);
        if (world.isDangerous(pos)) {
            alive = false;
            score -= 1000;
            System.out.println("Охотник погиб в " + pos + ". Итоговый счет: " + score);
        }
    }

    private void grabGold() {
        score -= 1;
        if (world.grabGold(pos)) {
            hasGold = true;
            score += 1000;
            finished = true;
            actionQueue.clear();
            System.out.println("Победа. Золото найден в " + pos + ". Итоговый счет: " + score);
        }
    }

    private void shoot() {
        if (!hasArrow)
            return;
        hasArrow = false;
        score -= 11; // -10 за выстрел и -1 за действие
        System.out.println("Выстрел из " + pos + " В сторону " + dir);
        boolean killed = world.shootArrow(pos, dir);
        if (killed) {
            System.out.println("Вампус убит.");
        } else {
            System.out.println("Промах, стрела попала в стену");
        }
    }

    // --- Геттеры для GUI ---

    public Point getPos() {
        return pos;
    }

    public Direction getDir() {
        return dir;
    }

    public KnowledgeBase getKb() {
        return kb;
    }

    public int getScore() {
        return score;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean hasGold() {
        return hasGold;
    }

    public boolean hasArrow() {
        return hasArrow;
    }
}
