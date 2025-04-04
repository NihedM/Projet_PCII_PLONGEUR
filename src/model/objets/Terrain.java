package model.objets;

public class Terrain {
    private final int width;
    private final int height;
    private int[][] depthMap; // Carte des profondeurs

    public Terrain(int width, int height) {
        this.width = width;
        this.height = height;
        initializeDepthMap();
    }

    private void initializeDepthMap() {
        depthMap = new int[width][height];

        // Calcul des profondeurs
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Calcul de la position relative (0 à 1)
                float xRatio = (float)x / width;
                float yRatio = (float)y / height;

                // Calcul de la profondeur basée sur la position diagonale
                float depthValue = xRatio + yRatio; // varie de 0 à 2

                // Division en 4 zones
                if (depthValue < 0.5f) {
                    depthMap[x][y] = 1; // Zone 1 (petite)
                } else if (depthValue < 1.0f) {
                    depthMap[x][y] = 2; // Zone 2 (grande)
                } else if (depthValue < 1.5f) {
                    depthMap[x][y] = 3; // Zone 3 (grande)
                } else {
                    depthMap[x][y] = 4; // Zone 4 (petite)
                }
            }
        }
    }

    public int getDepthAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return -1; // Hors du terrain
        }
        return depthMap[x][y];
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