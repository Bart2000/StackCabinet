import java.awt.Color;

public class Square extends Cabinet {
    private int WIDTH = 100;


    public Square() {
        super();
    }

    public Square(int x, int y) {
        super(x, y);
    }

    @Override
    public void draw() {        
        int gate = dir.ordinal();
        Main.draw.square(x, y, WIDTH-SPACING);

        int UP = (4 + (0 - gate)) % 4;
        int RIGHT = (4 + (1 - gate)) % 4;
        int DOWN = (4 + (2 - gate)) % 4;
        int LEFT = (4 + (3 - gate)) % 4;

        Main.draw.text(x, y+WIDTH-TEXT_OFFSET, (!this.isBase() || UP == 0) ? "S" + UP : ""); // UP
        Main.draw.text(x+WIDTH-TEXT_OFFSET, y, (!this.isBase() || RIGHT == 0) ? "S" + RIGHT : ""); // RIGHT
        Main.draw.text(x, y-WIDTH+TEXT_OFFSET, (!this.isBase() || DOWN == 0) ? "S" + DOWN : ""); // DOWN
        Main.draw.text(x-WIDTH+TEXT_OFFSET, y, (!this.isBase() || LEFT == 0) ? "S" + LEFT : ""); // LEFT

        if(showId) {
            Main.draw.text(x, y, String.valueOf(this.getId()));
        }
    }

    @Override
    public void highlight(Color color) {
        Main.draw.setPenColor(color);
        draw();
    }

    @Override
    public void calculatePosition(Cabinet c, int gate) {
        int pos = (c.getDir().ordinal() + gate) % 4;
        int[] vector = vectors[pos];
        int receiving_gate = adj.indexOf(c);
        int d = ((pos + 2) + (4 - receiving_gate)) % 4;
        this.dir = Direction.values()[d];
        
        this.x = c.getX() + WIDTH*2*vector[0];
        this.y = c.getY() + WIDTH*2*vector[1];
    }
}