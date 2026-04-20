import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

// ================= MAIN =================
public class ACOVisualizer {
    public static void main(String[] args) {
        // Chạy GUI trên luồng Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setTitle("Ant Colony Optimization - TSP Visualizer");
            frame.setSize(1200, 800);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}

// ================= FRAME =================
class MainFrame extends JFrame {
    DrawPanel drawPanel;         // Panel vẽ đồ thị + đường đi
    JPanel controlPanel;         // Panel chứa các nút điều khiển
    JButton startBtn, stopBtn, resetBtn, randomNodesBtn; // Nút chức năng
    JSpinner antsSpinner, alphaSpinner, betaSpinner, rhoSpinner, speedSpinner, nodesSpinner; // Tham số
    JLabel bestLabel, iterLabel; // Hiển thị kết quả tốt nhất và số vòng lặp
    JTextArea pathArea;          // Hiển thị đường đi tốt nhất
    ACOWorker worker;            // Luồng chạy thuật toán ACO

    MainFrame() {
        // Khởi tạo panel chính
        drawPanel = new DrawPanel();
        controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Các nút chức năng
        startBtn = new JButton("Start");
        stopBtn = new JButton("Stop");
        resetBtn = new JButton("Reset");
        randomNodesBtn = new JButton("Random Nodes");

        // Các spinner để nhập tham số
        antsSpinner  = new JSpinner(new SpinnerNumberModel(30, 1, 500, 1));   // Số lượng kiến
        alphaSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 10.0, 0.1)); // α
        betaSpinner  = new JSpinner(new SpinnerNumberModel(5.0, 0.0, 20.0, 0.1)); // β
        rhoSpinner   = new JSpinner(new SpinnerNumberModel(0.5, 0.0, 1.0, 0.01)); // ρ
        speedSpinner = new JSpinner(new SpinnerNumberModel(50, 1, 500, 1));      // Tốc độ vẽ
        nodesSpinner = new JSpinner(new SpinnerNumberModel(15, 4, 100, 1));     // Số node (thành phố)

        // Nhãn hiển thị kết quả
        bestLabel = new JLabel("Best: N/A");
        iterLabel = new JLabel("Iter: 0");

        // Ô văn bản hiển thị đường đi tốt nhất
        pathArea  = new JTextArea(5,20);
        pathArea.setEditable(false);
        pathArea.setLineWrap(true);
        pathArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(pathArea);

        // Bố trí giao diện bằng GridBagLayout
        c.insets = new Insets(4,4,4,4);

        c.gridx = 0; c.gridy = 0; controlPanel.add(startBtn, c);
        c.gridx = 1; controlPanel.add(stopBtn, c);
        c.gridx = 2; controlPanel.add(resetBtn, c);
        c.gridx = 3; controlPanel.add(randomNodesBtn, c);

        c.gridx = 0; c.gridy = 1; controlPanel.add(new JLabel("Nodes:"), c);
        c.gridx = 1; controlPanel.add(nodesSpinner, c);

        c.gridx = 0; c.gridy = 2; controlPanel.add(new JLabel("Ants"), c);
        c.gridx = 1; controlPanel.add(antsSpinner, c);

        c.gridx = 0; c.gridy = 3; controlPanel.add(new JLabel("α"), c);
        c.gridx = 1; controlPanel.add(alphaSpinner, c);

        c.gridx = 0; c.gridy = 4; controlPanel.add(new JLabel("β"), c);
        c.gridx = 1; controlPanel.add(betaSpinner, c);

        c.gridx = 0; c.gridy = 5; controlPanel.add(new JLabel("ρ"), c);
        c.gridx = 1; controlPanel.add(rhoSpinner, c);

        c.gridx = 0; c.gridy = 6; controlPanel.add(new JLabel("Speed"), c);
        c.gridx = 1; controlPanel.add(speedSpinner, c);

        c.gridx = 0; c.gridy = 7; controlPanel.add(iterLabel, c);
        c.gridx = 1; controlPanel.add(bestLabel, c);

        c.gridx = 0; c.gridy = 8; c.gridwidth = 10;
        controlPanel.add(scroll, c);

        // Chia giao diện thành 2 phần: panel vẽ và panel điều khiển
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, drawPanel, controlPanel);
        split.setDividerLocation(800);
        add(split);

        // Gán sự kiện cho các nút
        startBtn.addActionListener(e -> start());
        stopBtn.addActionListener(e -> stop());
        resetBtn.addActionListener(e -> reset());
        randomNodesBtn.addActionListener(e -> {
            int n = (Integer) nodesSpinner.getValue();
            drawPanel.generateRandomNodes(n);
            bestLabel.setText("Best: N/A");
            iterLabel.setText("Iter: 0");
            pathArea.setText("");
        });

        // Sinh các node ngẫu nhiên ban đầu
        drawPanel.generateRandomNodes((Integer)nodesSpinner.getValue());
    }

    // Bắt đầu chạy ACO
    private void start() {
        if (worker != null && !worker.isDone()) return;
        int ants = (Integer) antsSpinner.getValue();
        double alpha = (Double) alphaSpinner.getValue();
        double beta = (Double) betaSpinner.getValue();
        double rho = (Double) rhoSpinner.getValue();
        int speed = (Integer) speedSpinner.getValue();

        // Khởi tạo ACO Engine
        ACOEngine engine = new ACOEngine(drawPanel.getNodes());
        engine.antsCount = ants;
        engine.alpha = alpha;
        engine.beta = beta;
        engine.rho = rho;
        engine.Q = 100.0;
        engine.resetPheromone(1.0);

        // Chạy ACO trong background (SwingWorker)
        worker = new ACOWorker(engine, drawPanel, speed, this);
        worker.execute();
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        resetBtn.setEnabled(false);
    }

    // Dừng ACO
    private void stop() {
        if (worker != null) worker.cancel(true);
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        resetBtn.setEnabled(true);
    }

    // Reset giao diện
    private void reset() {
        if (worker != null) worker.cancel(true);
        drawPanel.clear();
        bestLabel.setText("Best: N/A");
        iterLabel.setText("Iter: 0");
        pathArea.setText("");
        startBtn.setEnabled(true);
    }
}

// ================= DRAW PANEL =================
class DrawPanel extends JPanel {
    List<Point> nodes = new ArrayList<>();     // Danh sách các thành phố
    List<Point> bestTour = new ArrayList<>();  // Đường đi tốt nhất
    List<AntState> ants = new ArrayList<>();   // Vị trí của kiến (để vẽ động)

    // Sinh node ngẫu nhiên
    void generateRandomNodes(int n) {
        Random rand = new Random();
        nodes.clear();
        for (int i=0; i<n; i++) {
            nodes.add(new Point(rand.nextInt(750)+20, rand.nextInt(700)+20));
        }
        bestTour.clear();
        ants.clear();
        repaint();
    }

    List<Point> getNodes() { return nodes; }

    void setBestTour(List<Point> tour) {
        bestTour = new ArrayList<>(tour);
    }

    void setAnts(List<AntState> ants) {
        this.ants = new ArrayList<>(ants);
        repaint();
    }

    void clear() {
        nodes.clear();
        bestTour.clear();
        ants.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Vẽ tất cả các cạnh (màu xanh nhạt)
        g.setColor(new Color(173, 216, 230));
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                Point a = nodes.get(i), b = nodes.get(j);
                g.drawLine(a.x, a.y, b.x, b.y);
            }
        }

        // Vẽ best path (màu xanh đậm)
        g.setColor(new Color(0, 0, 255));
        for (int i = 0; i < bestTour.size() - 1; i++) {
            Point a = bestTour.get(i);
            Point b = bestTour.get(i + 1);
            g.drawLine(a.x, a.y, b.x, b.y);
        }
        if (bestTour.size() > 1) {
            Point a = bestTour.get(bestTour.size() - 1);
            Point b = bestTour.get(0);
            g.drawLine(a.x, a.y, b.x, b.y);
        }

        // Vẽ node (màu đen + chỉ số node)
        g.setColor(Color.BLACK);
        for (int i = 0; i < nodes.size(); i++) {
            Point p = nodes.get(i);
            g.drawOval(p.x - 6, p.y - 6, 12, 12);
            g.drawString(String.valueOf(i), p.x + 10, p.y);
        }

        // Vẽ kiến (màu đỏ)
        g.setColor(Color.RED);
        for (AntState ant : ants) {
            Point pos = ant.getPosition(nodes);
            g.fillOval(pos.x - 3, pos.y - 3, 10, 10);
        }
    }
}

// ================= ANT STATE =================
class AntState {
    List<Integer> tour; // Hành trình của kiến (theo chỉ số node)
    int edgeIndex;      // Đang đi qua cạnh nào
    double progress;    // Tiến độ trên cạnh (0..1)

    AntState(List<Integer> tour) {
        this.tour = tour;
        this.edgeIndex = 0;
        this.progress = 0.0;
    }

    // Tính vị trí hiện tại của kiến theo progress
    Point getPosition(List<Point> nodes) {
        int a = tour.get(edgeIndex);
        int b = (edgeIndex+1 < tour.size()) ? tour.get(edgeIndex+1) : tour.get(0);
        Point pa = nodes.get(a);
        Point pb = nodes.get(b);
        int x = (int)(pa.x + (pb.x - pa.x) * progress);
        int y = (int)(pa.y + (pb.y - pa.y) * progress);
        return new Point(x,y);
    }

    // Tiến thêm một bước
    void advance(double step) {
        progress += step;
        if (progress >= 1.0) {
            progress = 0.0;
            edgeIndex++;
            if (edgeIndex >= tour.size()) edgeIndex = 0;
        }
    }
}

// ================= ACO ENGINE =================
class ACOEngine {
    List<Point> nodes;           // Danh sách các node (thành phố)
    double[][] pheromone;        // Ma trận pheromone
    double[][] dist;             // Ma trận khoảng cách
    int antsCount = 30;          // Số lượng kiến
    double alpha = 1.0, beta = 5.0, rho = 0.5, Q = 100.0; // Tham số

    List<Point> bestTour = new ArrayList<>();      // Đường đi tốt nhất
    List<Integer> bestTourIdx = new ArrayList<>(); // Index của best tour
    double bestLength = Double.MAX_VALUE;          // Độ dài tốt nhất
    List<List<Integer>> lastAntTours = new ArrayList<>(); // Tour của tất cả kiến vòng hiện tại

    ACOEngine(List<Point> nodes) {
        this.nodes = nodes;
        int n = nodes.size();
        pheromone = new double[n][n];
        dist = new double[n][n];
        for (int i=0;i<n;i++) {
            for (int j=0;j<n;j++) {
                if (i==j) dist[i][j] = 0;
                else dist[i][j] = nodes.get(i).distance(nodes.get(j));
            }
        }
    }

    // Reset pheromone về giá trị ban đầu
    void resetPheromone(double init) {
        for (int i=0;i<nodes.size();i++) Arrays.fill(pheromone[i], init);
    }

    // Thực hiện 1 vòng lặp ACO
    void iterate() {
        int n = nodes.size();
        lastAntTours.clear();
        List<Double> allLengths = new ArrayList<>();

        // Mỗi con kiến xây dựng 1 tour
        for (int k=0;k<antsCount;k++) {
            List<Integer> tour = new ArrayList<>();
            boolean[] visited = new boolean[n];
            int start = new Random().nextInt(n); // chọn node xuất phát ngẫu nhiên
            tour.add(start);
            visited[start] = true;
            for (int step=1; step<n; step++) {
                int current = tour.get(tour.size()-1);
                int next = selectNext(current, visited); // chọn node tiếp theo
                tour.add(next);
                visited[next] = true;
            }
            lastAntTours.add(tour);
            double length = tourLength(tour);
            allLengths.add(length);

            // Cập nhật best tour
            if (length < bestLength) {
                bestLength = length;
                bestTourIdx = new ArrayList<>(tour);
                bestTour.clear();
                for (int idx : tour) bestTour.add(nodes.get(idx));
            }
        }

        // Bay hơi pheromone
        evaporate();

        // Cập nhật pheromone từ tất cả các tour
        for (int k=0;k<lastAntTours.size();k++) {
            double contrib = Q / allLengths.get(k);
            List<Integer> t = lastAntTours.get(k);
            for (int i=0;i<t.size()-1;i++) {
                int a = t.get(i), b = t.get(i+1);
                pheromone[a][b] += contrib;
                pheromone[b][a] += contrib;
            }
            int a = t.get(t.size()-1), b = t.get(0);
            pheromone[a][b] += contrib;
            pheromone[b][a] += contrib;
        }
    }

    // Bay hơi pheromone
    private void evaporate() {
        for (int i=0;i<nodes.size();i++)
            for (int j=0;j<nodes.size();j++)
                pheromone[i][j] *= (1.0 - rho);
    }

    // Chọn node tiếp theo dựa trên xác suất
    private int selectNext(int current, boolean[] visited) {
        int n = nodes.size();
        double[] prob = new double[n];
        double sum = 0;
        for (int j=0;j<n;j++) {
            if (!visited[j]) {
                prob[j] = Math.pow(pheromone[current][j], alpha) * Math.pow(1.0/dist[current][j], beta);
                sum += prob[j];
            }
        }
        double r = Math.random()*sum, cum=0;
        for (int j=0;j<n;j++) {
            if (!visited[j]) {
                cum += prob[j];
                if (cum>=r) return j;
            }
        }
        for (int j=0;j<n;j++) if (!visited[j]) return j;
        return 0;
    }

    // Tính độ dài 1 tour
    private double tourLength(List<Integer> tour) {
        double length=0;
        for (int i=0;i<tour.size()-1;i++) length += dist[tour.get(i)][tour.get(i+1)];
        length += dist[tour.get(tour.size()-1)][tour.get(0)];
        return length;
    }
}

// ================= WORKER =================
class ACOWorker extends SwingWorker<Void, Void> {
    ACOEngine engine;   // Bộ máy ACO
    DrawPanel panel;    // Panel vẽ
    int delay;          // Độ trễ (tốc độ animation)
    MainFrame frame;    // Frame chính (để update label)
    int iter=0;         // Số vòng lặp
    List<AntState> ants = new ArrayList<>();

    ACOWorker(ACOEngine e, DrawPanel p, int speed, MainFrame f) {
        engine=e; panel=p; delay=speed; frame=f;
    }

    @Override
    protected Void doInBackground() throws Exception {
        while (!isCancelled() && iter < 3000) { // Giới hạn vòng lặp
            engine.iterate(); // chạy ACO 1 vòng
            ants.clear();
            for (List<Integer> tour : engine.lastAntTours) {
                ants.add(new AntState(tour));
            }
            for (int step=0; step<100 && !isCancelled(); step++) {
                for (AntState ant : ants) ant.advance(0.02); // di chuyển kiến
                panel.setAnts(ants);
                panel.setBestTour(engine.bestTour);

                // update nhãn hiển thị
                frame.iterLabel.setText("Iter: " + iter);
                frame.bestLabel.setText(String.format("Best: %.2f", engine.bestLength));

                // update đường đi tốt nhất
                if (!engine.bestTourIdx.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i=0;i<engine.bestTourIdx.size();i++) {
                        sb.append(engine.bestTourIdx.get(i));
                        if (i < engine.bestTourIdx.size()-1) sb.append(" → ");
                    }
                    frame.pathArea.setText(
                        "Path: " + sb
                    );
                }

                iter++;
                Thread.sleep(delay); // delay animation
            }
        }
        return null;
    }
}
