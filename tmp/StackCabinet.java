import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class StackCabinet {
    private ArrayList<Cabinet> cabinets;
    private Cabinet base;

    public StackCabinet() {
        this.cabinets = new ArrayList<>();
    }
    
    public void initializeStackCabinet(int[][] graph) {
        // Initialize cabinets
        for(int i = 0; i < graph.length; i++) {
            Cabinet c = new Square();
            c.setId(i);
            addCabinet(c);
        }

        ArrayList<Cabinet> cabinets = getCabinets();
        Cabinet base = cabinets.get(0);
        base.setId(255);
        base.setX(Main.OFFSET);
        base.setY(Main.OFFSET);
        base.setDir(Direction.RIGHT);
        base.setBase(true);
        base.setVisited(true);

        // Set cabinet nodes
        for(int i = 0; i < graph.length; i++) {
           int[] nodes = graph[i];
           Cabinet c = cabinets.get(i);
            
           for(int n : nodes) {
               c.adj.add(getCabinetById(n));
           }
        }

        calculatePositions();
    }

    public void bfs() throws InterruptedException {
        ArrayList<Integer> visisted = new ArrayList<>();

        for(Cabinet c : cabinets) {
            c.highlight(Color.BLUE);

            for(Cabinet a : c.getAdj()) {
                // No response
                if(a != null) {
                    while(!Main.draw.isMousePressed()){
                        Thread.sleep(1);
                    }

                    Thread.sleep(200);

                    a.highlight(Color.GREEN);

                    while(!Main.draw.isMousePressed()){
                        Thread.sleep(1);
                    }
                    
                    Main.draw.setPenColor(Color.RED);
                    a.showId();
                    a.draw();
                }
            }

            c.highlight(Color.RED);
        }
    }

    public void calculatePositions() {

        for(int i = 0; i < cabinets.size(); i++) {
            Cabinet c = cabinets.get(i);
            List<Cabinet> adj = c.getAdj();

            for(int j = 0; j < adj.size(); j++) {
                Cabinet a = adj.get(j);

                if(a != null && !a.isVisited()) {
                    a.calculatePosition(c, j);
                    a.setVisited(true);
                }
            }
        }
    }

    public void draw() {
        for(Cabinet c : this.cabinets) {
            c.draw();
        }
    }

    public Cabinet getCabinetById(int id) {
        for(Cabinet c : this.cabinets) {
            if(c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public void addCabinet(Cabinet c) {
        this.cabinets.add(c);
    }

    public ArrayList<Cabinet> getCabinets() {
        return cabinets;
    }

    public void setBase(Cabinet base) {
        this.base = base;
    }

    @Override
    public String toString() 
    {
        StringBuilder builder = new StringBuilder("StackCabinet:");

        for(Cabinet c : this.cabinets) {
            builder.append("\n");
            builder.append(c.getId() + " - " + c.toString());
        }

        return builder.toString();
    }
}
