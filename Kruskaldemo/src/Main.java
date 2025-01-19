import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

import javax.swing.Timer;

public class Main extends JPanel {
    private Graph graph = new Graph();
    private List<Edge> mst = null;  // Cây khung nhỏ nhất
    private Queue<Edge> edgeQueue = new LinkedList<>();  // Hàng đợi các cạnh để hiển thị theo thứ tự
    private Map<Edge, String> edgeStates = new HashMap<>();  // Trạng thái của các cạnh: "selected" hoặc "skipped"
    private Point selectedVertex = null;
    private JTextArea edgeListArea;  // Hiển thị danh sách cạnh
    private Timer timer; // Bộ đếm thời gian để duyệt các cạnh
    private Iterator<Edge> edgeIterator; // Bộ lặp để duyệt từng cạnh trong hàng đợi

    public Main() {
        this.setLayout(new BorderLayout());

        // Khu vực hiển thị danh sách các cạnh
        edgeListArea = new JTextArea(15, 30);
        edgeListArea.setEditable(false);
        edgeListArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane edgeListScrollPane = new JScrollPane(edgeListArea);

        // Khu vực đồ thị
        JPanel graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGraph((Graphics2D) g);
            }
        };
        graphPanel.setPreferredSize(new Dimension(600, 400));
        graphPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleGraphClick(e.getPoint());
            }
        });

        // Các nút điều khiển
        JButton runKruskalButton = new JButton("Run Kruskal");
        JButton resetButton = new JButton("Reset");

        runKruskalButton.addActionListener(e -> runKruskalAnimation());
        resetButton.addActionListener(e -> resetGraph());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(runKruskalButton);
        buttonPanel.add(resetButton);

        // Thêm các thành phần vào cửa sổ chính
        this.add(graphPanel, BorderLayout.CENTER);
        this.add(edgeListScrollPane, BorderLayout.EAST);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

        private void handleGraphClick(Point clickedPoint) {
            boolean onExistingVertex = false;
        
            // Kiểm tra xem người dùng có nhấp vào một đỉnh đã tồn tại không
            for (Point vertex : graph.vertices) {
                if (vertex.distance(clickedPoint) < 20) {
                    onExistingVertex = true;
                    if (selectedVertex == null) {
                        // Chọn đỉnh đầu tiên
                        selectedVertex = vertex;
                    } else {
                        // Chọn đỉnh thứ hai và hiển thị hộp thoại nhập trọng số
                        int sourceIndex = graph.vertices.indexOf(selectedVertex);
                        int destIndex = graph.vertices.indexOf(vertex);
        
                        String message = String.format(
                            "Enter weight for edge between Vertex %d and Vertex %d:",
                            sourceIndex, destIndex
                        );
        
                        String weightStr = JOptionPane.showInputDialog(
                            this, message, "Edge Weight", JOptionPane.PLAIN_MESSAGE
                        );
        
                        if (weightStr != null && !weightStr.isEmpty()) {
                            try {
                                int weight = Integer.parseInt(weightStr);
        
                                if (weight < 0) {
                                    JOptionPane.showMessageDialog(
                                        this,
                                        "Weight cannot be negative!",
                                        "Invalid Input",
                                        JOptionPane.ERROR_MESSAGE
                                    );
                                } else {
                                    graph.addEdge(sourceIndex, destIndex, weight);
                                }
        
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(
                                    this,
                                    "Invalid weight! Please enter a valid number.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                                );
                            }
                        }
        
                        // Hủy trạng thái chọn nếu không nhập hoặc nhập không hợp lệ
                        selectedVertex = null;
                        repaint();
                        return;
                    }
                    break;
                }
            }
        
            // Nếu nhấp vào không phải đỉnh nào, thêm đỉnh mới
            if (!onExistingVertex) {
                graph.addVertex(clickedPoint);
                selectedVertex = null; // Hủy trạng thái chọn
                repaint();
            }
        }

    //Vẽ trên đồ thị
    private void drawGraph(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(2));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));

        // Vẽ các cạnh
        for (Edge edge : graph.edges) {
            Point source = graph.vertices.get(edge.source);
            Point destination = graph.vertices.get(edge.destination);

            if (edgeStates.containsKey(edge)) {
                if (edgeStates.get(edge).equals("selected")) {
                    g2d.setColor(Color.RED);  // Cạnh được chọn
                } else if (edgeStates.get(edge).equals("skipped")) {
                    g2d.setColor(Color.LIGHT_GRAY);  // Cạnh bị bỏ qua
                }
            } else {
                g2d.setColor(Color.BLACK);  // Cạnh chưa được duyệt
            }
            g2d.drawLine(source.x, source.y, destination.x, destination.y);
            g2d.drawString(
                    String.valueOf(edge.weight),
                    (source.x + destination.x) / 2,
                    (source.y + destination.y) / 2
            );
        }

        // Vẽ các đỉnh
        for (Point vertex : graph.vertices) {
            g2d.setColor(Color.BLUE);
            g2d.fillOval(vertex.x - 10, vertex.y - 10, 20, 20);
            g2d.setColor(Color.WHITE);
            g2d.drawString(
                    String.valueOf(graph.vertices.indexOf(vertex)),
                    vertex.x - 5, vertex.y + 5
            );
        }
    }

    private void updateEdgeListDisplay() {
        StringBuilder sb = new StringBuilder();
        for (Edge edge : graph.edges) {
            String state = edgeStates.getOrDefault(edge, "pending");
            sb.append(String.format("(%d - %d, w=%d) : %s\n",
                    edge.source, edge.destination, edge.weight, state));
        }
        edgeListArea.setText(sb.toString());
    }

    // Thực thi thuật toán
    private void runKruskalAnimation() {
        KruskalAlgorithm kruskal = new KruskalAlgorithm(graph);
        mst = kruskal.getMSTWithSteps(edgeQueue, edgeStates, new ArrayList<>()); // Lấy cây khung nhỏ nhất
        edgeIterator = edgeQueue.iterator(); // Tạo bộ lặp để duyệt từng cạnh
    
        // Tạo timer với khoảng thời gian 0.5 giây (500 ms)
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (edgeIterator.hasNext()) {
                    Edge currentEdge = edgeIterator.next();
                    boolean isInMST = mst.contains(currentEdge);
    
                    // Cập nhật trạng thái cạnh
                    if (isInMST) {
                        edgeStates.put(currentEdge, "selected"); // Cạnh được chọn
                    } else {
                        edgeStates.put(currentEdge, "skipped"); // Cạnh bị bỏ qua
                    }
    
                    // Cập nhật giao diện
                    repaint();
                    updateEdgeListDisplay();
    
                } else {
                    // Dừng timer khi duyệt xong tất cả các cạnh
                    ((Timer) e.getSource()).stop();
                }
            }
        });
    
        timer.start(); // Bắt đầu duyệt các cạnh
    }

    // Nút reset 
    private void resetGraph() {
        graph = new Graph();
        mst = null;
        edgeQueue.clear();
        edgeStates.clear();
        selectedVertex = null;
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        repaint();
        updateEdgeListDisplay();
    }

    // Chạy chương trình
    public static void main(String[] args) {
        JFrame frame = new JFrame("Kruskal's Algorithm Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        Main mainPanel = new Main();
        frame.add(mainPanel);
        frame.setVisible(true);
    }
}
