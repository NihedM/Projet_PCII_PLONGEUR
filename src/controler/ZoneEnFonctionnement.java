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
    private boolean isFinalized = false;


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

    public boolean isInside(Position position) {
        int x = position.getX();
        int y = position.getY();
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    public boolean overlaps(ZoneEnFonctionnement other) {
        return !(this.maxX < other.minX || this.minX > other.maxX ||
                this.maxY < other.minY || this.minY > other.maxY);
    }


    public void expandToInclude(int newMinX, int newMinY, int newMaxX, int newMaxY) {
        if (!isFinalized) {
            minX = Math.min(minX, newMinX);
            minY = Math.min(minY, newMinY);
            maxX = Math.max(maxX, newMaxX);
            maxY = Math.max(maxY, newMaxY);
        }
    }
    public void updateBounds(int newMinX, int newMinY, int newMaxX, int newMaxY) {
        if(this.equals(GamePanel.getInstance().getMainZone()))
            throw new IllegalArgumentException("Cannot update main zone bounds");


        if (!isFinalized) {
            int width = this.maxX - this.minX;
            int height = this.maxY - this.minY;


            if (width < 400) {
                this.minX = newMinX;
                this.maxX = newMaxX;
            }

            if (height < 400) {
                this.minY = newMinY;
                this.maxY = newMaxY;
            }

            width = this.maxX - this.minX;
            height = this.maxY - this.minY;

            if (width >= 400 && height >= 400) {
                isFinalized = true;
            }


        }
    }


    public void updateMainBounds(int newMinX, int newMinY, int newMaxX, int newMaxY) {
        minX = newMinX;
        minY = newMinY;
        maxX = newMaxX;
        maxY = newMaxY;
    }


        public void expandToIncludeUnit(UniteControlable unit, int buffer) {
        if (!isFinalized) {
            Position position = unit.getPosition();
            int unitMinX = position.getX() - buffer;
            int unitMinY = position.getY() - buffer;
            int unitMaxX = position.getX() + buffer;
            int unitMaxY = position.getY() + buffer;
            expandToInclude(unitMinX, unitMinY, unitMaxX, unitMaxY);
        }
    }
    public void finalizeZone() {
        isFinalized = true;
    }

    public void startZone() {
        isFinalized = false;
    }

}