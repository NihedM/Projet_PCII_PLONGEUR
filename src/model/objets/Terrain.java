package model.objets;

public class Terrain {
    private final int width;
    private final int height;

    public Terrain(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Position panelToTerrain(Position panelPosition, int cameraX, int cameraY) {
        int terrainX = panelPosition.getX() + cameraX;
        int terrainY = panelPosition.getY() + cameraY;
        return new Position(terrainX, terrainY);
    }

    public Position terrainToPanel(Position terrainPosition, int cameraX, int cameraY) {
        int panelX = terrainPosition.getX() - cameraX;
        int panelY = terrainPosition.getY() - cameraY;
        return new Position(panelX, panelY);
    }
}