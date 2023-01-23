import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static Draw draw;
    private static int[][] graph1 = new int[][] { // Neigbour S0, neighbour S1, neighbour S2, neighbour S3
        {1},
        {2, 3, 0, 255},     // 1
        {0, 4, 5, 1},       // 2
        {0, 1, 5, 6},       // 3
        {0, 7, 2, 0},       // 4
        {2, 7, 8, 3},       // 5
        {3, 8, 0, 0},       // 6
        {4, 0, 9, 5},       // 7
        {0, 0, 5, 9},       // 8
        {0, 0, 8, 7}        // 9
    };

    private static int[][] graph2 = new int[][] { // Neigbour S0, neighbour S1, neighbour S2, neighbour S3
        {1},
        {0, 255, 2, 3},     // 1
        {0, 4, 1, 0},       // 2
        {1, 4, 0, 0},       // 3
        {5, 3, 2, 0},       // 4
        {6, 0, 0, 4},       // 5
        {0, 5, 0, 7},       // 6
        {8, 0, 9, 6},       // 7
        {7, 0, 0, 10},      // 8
        {11, 0, 0, 7},      // 9
        {0, 8, 0, 12},      // 10
        {9, 0, 13, 0},      // 11
        {0, 14, 0, 10},     // 12
        {11, 0, 15, 0},     // 13
        {0, 12, 0, 16},     // 14
        {0, 17, 0, 13},     // 15
        {14, 0, 0, 18},     // 16
        {0, 15, 18, 0},     // 17
        {16, 0, 17, 0}      // 18
    };

    public static int OFFSET = 200;
    public static double SIZE = 0.5;
    
    public static void main(String[] args) throws InterruptedException {
        setup();       
        
        StackCabinet stackCabinet = new StackCabinet();
        stackCabinet.initializeStackCabinet(graph2);
        stackCabinet.draw();

        stackCabinet.bfs();
        //bfs(stackCabinet, graph.length);
    }

    private static void setup() {
        draw = new Draw();
        draw.setXscale(0, 1000/SIZE);
        draw.setYscale(0, 1000/SIZE);
        draw.clear(new Color(255, 255, 255));
        draw.setPenRadius(0.005);
        draw.setPenColor(255, 0, 0);
        draw.setFont(new Font("SansSerif", Font.PLAIN, (int)(15*SIZE)));
    }

    private static void bfs(StackCabinet stackCabinet, int size) {
        int[][] aux = new int[size][];

        for(int i = 0; i < size; i++) {
            aux[i] = new int[] {0, 0, 0, 0};
        }

        aux[0] = new int[] {1};

        for(int i = 0; i < size; i++) {

        }

        for(int[] i : aux) {
            System.out.println(Arrays.toString(i));
        }
    }
}