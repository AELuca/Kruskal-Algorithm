import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Graph {
    public List<Point> vertices;
    public List<Edge> edges;

    public Graph() {
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public void addVertex(Point vertex) {
        vertices.add(vertex);
    }

    public void addEdge(int source, int destination, int weight) {
        edges.add(new Edge(source, destination, weight));
    }
}
