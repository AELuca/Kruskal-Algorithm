import java.util.*;

public class KruskalAlgorithm {
    private Graph graph;

    public KruskalAlgorithm(Graph graph) {
        this.graph = graph;
    }

    private int find(int[] parent, int vertex) {
        if (parent[vertex] != vertex) {
            parent[vertex] = find(parent, parent[vertex]);
        }
        return parent[vertex];
    }

    private void union(int[] parent, int[] rank, int root1, int root2) {
        if (rank[root1] < rank[root2]) {
            parent[root1] = root2;
        } else if (rank[root1] > rank[root2]) {
            parent[root2] = root1;
        } else {
            parent[root2] = root1;
            rank[root1]++;
        }
    }

    public List<Edge> getMSTWithSteps(
            Queue<Edge> edgeQueue, Map<Edge, String> edgeStates, List<String> steps) {
        List<Edge> mst = new ArrayList<>();
        int verticesCount = graph.vertices.size();
        int[] parent = new int[verticesCount];
        int[] rank = new int[verticesCount];

        for (int i = 0; i < verticesCount; i++) {
            parent[i] = i;
            rank[i] = 0;
        }

        Collections.sort(graph.edges);

        for (Edge edge : graph.edges) {
            int root1 = find(parent, edge.source);
            int root2 = find(parent, edge.destination);

            if (root1 != root2) {
                mst.add(edge);
                edgeStates.put(edge, "selected");
                edgeQueue.add(edge);
                union(parent, rank, root1, root2);
            } else {
                edgeStates.put(edge, "skipped");
                edgeQueue.add(edge);
            }

            if (mst.size() == verticesCount - 1) break;
        }
        return mst;
    }
}
