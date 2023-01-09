package io.github.ababism.dependencySortApp.Graph;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Класс реализующий ориентированный граф через список смежности
 */
public class Graph {
    // список смежности
    private List<List<Integer>> adjacencyList;
    // количество вершин
    private final int numberOfVertexes;

    /**
     * Конструктор
     *
     * @param numberOfVertexes количество вершин в графе
     */
    public Graph(int numberOfVertexes) {
        this.numberOfVertexes = numberOfVertexes;
        adjacencyList = new ArrayList<>();
        for (int i = 0; i < numberOfVertexes; ++i) {
            adjacencyList.add(new ArrayList<>());
        }
    }

    /**
     * Добавить ребро из а в b
     *
     * @param a откуда выходит
     * @param b куда ведет
     * @return Вышло ли добавить
     */
    public boolean addEdge(int a, int b) {
        if (a > numberOfVertexes - 1 || b > numberOfVertexes - 1) {
            return false;
        }
        adjacencyList.get(a).add(b);
        return true;
    }

    /**
     * Проверка ориентированного графа на наличие циклов используя DFS, рекурсию в findCycleFrom
     *
     * @return ацикличен ли граф
     */
    public boolean isAcyclic() {
        boolean[] visited = new boolean[numberOfVertexes];
        boolean[] inCurrentTraversal = new boolean[numberOfVertexes];
        for (int i = 0; i < numberOfVertexes; ++i) {
            if (findCycleFrom(i, visited, inCurrentTraversal)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Метод реализующий топологическую графа без циклов (опять же с DFS)
     *
     * @return отсортированную деку
     */
    public Deque<Integer> topologicalSort() {
        // вместо стека, так как стек в джаве кажется вообще всегда хуже деки
        Deque<Integer> resultStack = new ArrayDeque<>();
        boolean[] traversed = new boolean[numberOfVertexes];

        for (var vertexIndex = 0; vertexIndex < numberOfVertexes; ++vertexIndex) {
            if (!traversed[vertexIndex]) {
                topSortFunc(vertexIndex, traversed, resultStack);
            }
        }

        return resultStack;
    }

    /**
     * Вспомогательный метод для топсорта, реализует DFS (рекурсию вглубь графа) и выкладку в стек
     *
     * @param currentVertexIndex индекс текущей вершины
     * @param traversed          список вершин которые вы посещали
     * @param resultStack        отсортированный список вершин (по сути он стек)
     */
    private void topSortFunc(int currentVertexIndex, boolean[] traversed, Deque<Integer> resultStack) {
        traversed[currentVertexIndex] = true;
        List<Integer> currentAdjacencies = adjacencyList.get(currentVertexIndex);

        for (Integer adjacentVertex : currentAdjacencies) {
            if (!traversed[adjacentVertex]) {
                topSortFunc(adjacentVertex, traversed, resultStack);
            }
        }

        resultStack.addFirst(currentVertexIndex);
    }

    /**
     * Вспомогательный метод для проверки на циклы, реализует DFS (рекурсию вглубь графа)
     *
     * @param index              индекс текущей вершины
     * @param visited            список вершин которые вы проверяли
     * @param inCurrentTraversal список вершин которые мы прошли в данном заходе
     * @return есть ли цикл
     */
    private boolean findCycleFrom(int index, boolean[] visited, boolean[] inCurrentTraversal) {
        // Если наступили себе на "хвост" то есть цикл
        if (inCurrentTraversal[index]) {
            return true;
        }
        // не проверяем повторно
        if (visited[index]) {
            return false;
        }
        // отмечаем, что проходим вершину и что соответственно ее проверили
        inCurrentTraversal[index] = true;
        visited[index] = true;
        List<Integer> adjacent = adjacencyList.get(index);
        for (var nextVertexIndex : adjacent) {
            if (findCycleFrom(nextVertexIndex, visited, inCurrentTraversal)) {
                return true;
            }
        }

        // Заметаем хвост оказавшийся без циклов
        inCurrentTraversal[index] = false;
        return false;
    }

}

