package controler;

import model.objets.Position;
import model.objets.UniteControlable;
import view.GamePanel;

import java.util.ArrayList;
import java.util.List;

public class ZoneEnFonctionnement {
    public static final int MAX_WIDTH = 800 + GamePanel.UNIT_BUFFER;
    public static final int MAX_HEIGHT = 800 + GamePanel.UNIT_BUFFER;
    private int minX, minY, maxX, maxY;
    private UniteControlable unit;


    public ZoneEnFonctionnement(int minX, int minY, int maxX, int maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public void setUnite(UniteControlable unit) {
        this.unit = unit;
    }

    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }

    public UniteControlable getUnit() {
        return unit;
    }

    public boolean isInsideMain(Position position) {
        return GestionCollisions.estDans(minX, minY, maxX, maxY, position.getX(), position.getY());
    }

    public boolean isInsideDynamic(Position position) {
        int x = position.getX();
        int y = position.getY();
        return GestionCollisions.estDans(minX, minY, maxX, maxY, x, y);
    }

    public boolean overlaps(ZoneEnFonctionnement other) {
        return !(this.maxX < other.minX || this.minX > other.maxX ||
                this.maxY < other.minY || this.minY > other.maxY);
    }


    public void updateBounds(int newMinX, int newMinY, int newMaxX, int newMaxY) {
        if(this.equals(GamePanel.getInstance().getMainZone()))
            throw new IllegalArgumentException("Cannot update main zone bounds");

        this.minX = newMinX;
        this.minY = newMinY;
        this.maxX = newMaxX;
        this.maxY = newMaxY;
    }


    public void updateMainBounds(int newMinX, int newMinY, int newMaxX, int newMaxY) {
        minX = newMinX;
        minY = newMinY;
        maxX = newMaxX;
        maxY = newMaxY;
    }

}