package com.wampus;

import java.util.*;

public class Pathfinder {

    public List<Point> findPath(Point start, Point goal, KnowledgeBase kb) {
        if (start.equals(goal)) {
            return Collections.emptyList();
        }
        if (!kb.isSafe(goal)) {
            return null;
        }

        Queue<Point> queue = new LinkedList<>();       // Очередь клеток на проверку (волна)
        Set<Point> visited = new HashSet<>();          // Уже проверенные клетки, чтобы не ходить кругами
        Map<Point, Point> cameFrom = new HashMap<>();  // "Клетка -> откуда мы в неё пришли"

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            if (current.equals(goal)) {
                return buildPath(cameFrom, start, goal);
            }

            for (Point neighbor : kb.getValidNeighbors(current)) {
                if (!visited.contains(neighbor) && kb.isSafe(neighbor)) {
                    visited.add(neighbor);
                    cameFrom.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        return null; // Безопасного пути нет
    }

    private List<Point> buildPath(Map<Point, Point> cameFrom, Point start, Point goal) {
        List<Point> path = new ArrayList<>();
        Point step = goal;

        while (!step.equals(start)) {
            path.add(step);
            step = cameFrom.get(step);
        }

        Collections.reverse(path);
        return path;
    }
}