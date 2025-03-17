package model.objets;

public class CoordGrid {
    private int x, y;

    public CoordGrid(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public CoordGrid() {
        this.x = 0;
        this.y = 0;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
