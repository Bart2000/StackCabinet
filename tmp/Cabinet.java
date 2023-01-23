import java.awt.Color;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Cabinet {
    protected int id;
    protected int x;
    protected int y;
    protected List<Cabinet> adj;
    protected boolean visited;
    protected Direction dir;
    protected boolean isBase;
    protected int SPACING = 10;
    protected int TEXT_OFFSET = 35;
    protected Color color = Color.RED; 
    protected boolean showId = false;
    protected int[][] vectors = {
        {0, 1},     // UP
        {1, 0},     // RIGHT
        {0, -1},    // DOWN
        {-1, 0}     // LEFT
    };

    public Cabinet() {
        this.adj = new ArrayList<>();
        this.visited = false;
        this.dir = Direction.UP;
        this.isBase = false;
    }

    public Cabinet(int x, int y) {
        this();
        this.x = x;
        this.y = y;
    } 

    public Cabinet(Cabinet[] adj) {
        super();
        this.adj = Arrays.asList(adj);
    }

    public abstract void draw();
    
    public abstract void calculatePosition(Cabinet adj, int gate);

    public abstract void highlight(Color color);

    public int getId() 
    {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isVisited() {
        return this.visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public List<Cabinet> getAdj() {
        return adj;
    }

    public void setAdj(List<Cabinet> adj) {
        this.adj = adj;
    }

    public void addAdj(Cabinet c) {
        this.adj.add(c);
    }

    public Direction getDir() {
        return dir;
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }

    public boolean isBase() {
        return isBase;
    }

    public void setBase(boolean isBase) {
        this.isBase = isBase;
    }

    public void showId() {
        this.showId = true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");

        for(Cabinet c : this.adj) {
            builder.append((c != null) ? c.getId() + ", " : "0, ");
        }

        builder.append("]");

        return builder.toString();
    }
}
