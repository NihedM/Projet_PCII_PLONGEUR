package model.objets;

public class Position {
    private int x, y;           // vraie position

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position() {
        /*generate random position*/
        this.x = (int) (Math.random() * 600);       //TODO
        this.y = (int) (Math.random() * 600);       //TODO
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
