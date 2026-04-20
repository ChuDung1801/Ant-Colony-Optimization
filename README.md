# Ant Colony Optimization (ACO) - TSP Visualizer

## Giới thiệu
Đây là ứng dụng Java sử dụng Swing GUI để trực quan hóa thuật toán **Ant Colony Optimization (ACO)** giải bài toán **Travelling Salesman Problem (TSP)**.

Ứng dụng cho phép:
- Sinh các thành phố ngẫu nhiên.
- Quan sát đàn kiến tìm đường.
- Điều chỉnh tham số thuật toán theo thời gian thực.
- Theo dõi đường đi tối ưu nhất.

## Tính năng chính
- Giải bài toán TSP bằng ACO.
- Giao diện trực quan bằng Java Swing.
- Animation chuyển động của kiến.
- Tùy chỉnh tham số:
  - **Ants**: số lượng kiến  
  - **α (alpha)**: mức độ ảnh hưởng của pheromone  
  - **β (beta)**: mức độ ảnh hưởng của heuristic (khoảng cách)  
  - **ρ (rho)**: hệ số bay hơi pheromone  
  - **Speed**: tốc độ animation
- Hiển thị:
  - Best path.
  - Độ dài đường đi.
  - Số vòng lặp.

## Nguyên lý hoạt động

### 1. Xác suất chọn thành phố tiếp theo
$$
P_{ij}^k =
\frac{
(\tau_{ij})^{\alpha} \cdot (\eta_{ij})^{\beta}
}{
\sum_{l \in N_k}
(\tau_{il})^{\alpha} \cdot (\eta_{il})^{\beta}
}
$$

Trong đó:
- $\tau_{ij}$: lượng pheromone trên cạnh $(i, j)$  
- $\eta_{ij} = \frac{1}{d_{ij}}$: heuristic, với $d_{ij}$ là khoảng cách giữa hai thành phố  
- $\alpha$: độ ảnh hưởng pheromone  
- $\beta$: độ ảnh hưởng heuristic  
- $N_k$: tập các thành phố chưa đi của kiến $k$

### 2. Cập nhật pheromone
$$
\tau_{ij} = (1 - \rho)\cdot \tau_{ij} + \sum_{k=1}^{m}\Delta \tau_{ij}^k
$$

Trong đó:
- $$\rho$$: hệ số bay hơi.
- $$m$$: số lượng kiến.

### 3. Lượng pheromone đóng góp
$$
\Delta \tau_{ij}^k = \frac{Q}{L_k}
$$

Trong đó:
- $$Q$$: hằng số.
- $$L_k$$: độ dài tour của kiến $$k$$.

### 4. Độ dài tour
$$
L = \sum_{i=1}^{n-1} d_{i,i+1} + d_{n,1}
$$

## Kiến trúc chương trình

| Class | Mô tả |
|---|---|
| `ACOVisualizer` | Entry point của chương trình |
| `MainFrame` | Giao diện chính |
| `DrawPanel` | Vẽ đồ thị và animation |
| `ACOEngine` | Cài đặt thuật toán ACO |
| `AntState` | Trạng thái của từng con kiến |
| `ACOWorker` | Chạy thuật toán ở background |

## Cách chạy

### 1. Compile
```bash
javac ACOVisualizer.java
```

### 2. Run
```bash
java ACOVisualizer
```

## Hướng dẫn sử dụng
- Nhấn **Random Nodes** để tạo các thành phố.
- Điều chỉnh tham số:
  - Ants
  - $$\alpha$$, $$\beta$$, $$\rho$$
  - Speed
- Nhấn **Start** để chạy mô phỏng.
- Quan sát:
  - Kiến di chuyển.
  - Đường tốt nhất hiển thị màu xanh.
- Nhấn:
  - **Stop** để dừng.
  - **Reset** để làm lại.

## Gợi ý tham số

| Tham số | Giá trị gợi ý |
|---|---|
| Ants | 20 - 50 |
| $$\alpha$$ | 1 |
| $$\beta$$ | 3 - 7 |
| $$\rho$$ | 0.3 - 0.7 |
| Q | 100 |

## Độ phức tạp
Mỗi vòng lặp có độ phức tạp xấp xỉ:

$$
O(m \times n^2)
$$

Trong đó:
- $$m$$: số kiến.
- $$n$$: số thành phố.

## Hướng phát triển
Có thể mở rộng thêm:
- Elitist ACO.
- Max-Min ACO.
- Lưu / load dataset.
- So sánh với:
  - Genetic Algorithm.
  - Simulated Annealing.
