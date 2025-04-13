package model.objets;

import view.GamePanel;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Terrain {
    private final int width;
    private final int height;
    private int[][] depthMap; // Carte des profondeurs
    private Map<Integer, DepthZone> depthZones; // Configuration des zones de profondeur

    private int[][] backgroundDepthMap;
    private Map<Integer, Image> depthBackgroundImages;


    private int cubeWidth;
    private int cubeHeight;


    public Terrain(int width, int height) {
        this.width = width;
        this.height = height;
        initializeDepthZones(); // Initialise les configurations de zones
        initializeDepthMap();   // Initialise la carte des profondeurs
        initializeBackground(this, 10, 10); // Initialise la carte de fond
        loadDepthBackgroundImages();
    }

    private void initializeDepthZones() {
        depthZones = new HashMap<>();
        // Configuration flexible des zones de profondeur
        depthZones.put(1, new DepthZone(1, 30));  // Profondeur 1: max 30 colliers
        depthZones.put(2, new DepthZone(2, 50));  // Profondeur 2: max 50 colliers
        depthZones.put(3, new DepthZone(3, 20));  // Profondeur 3: max 20 colliers
        depthZones.put(4, new DepthZone(4, 40));  // Profondeur 4: max 40 colliers
    }

    // Classe interne pour stocker les infos de chaque zone
    private static class DepthZone {
        int depthLevel;
        int maxResources;
        int currentResources = 0;

        public DepthZone(int depthLevel, int maxResources) {
            this.depthLevel = depthLevel;
            this.maxResources = maxResources;
        }

        public boolean canAddResource() {
            return currentResources < maxResources;
        }

        public void incrementResources() {
            currentResources++;
        }

        public void decrementResources() {
            currentResources--;
        }
    }

    private void initializeDepthMap() {
        depthMap = new int[width][height];
        Random rand = new Random(12345); // Seed fixe pour un motif cohérent

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Valeur diagonale de base (0 à 1)
                double diagonalValue = (x + y) / (double) (width + height);

                // Ajouter un petit bruit aléatoire
                double noise = rand.nextDouble() * 0.2 - 0.1; // Entre -0.1 et 0.1

                // Valeur finale avec légère variation
                double depthValue = diagonalValue + noise;

                // Déterminer les zones de profondeur
                if (depthValue < 0.25) {
                    depthMap[x][y] = 1; // Peu profond
                } else if (depthValue < 0.5) {
                    depthMap[x][y] = 2;
                } else if (depthValue < 0.75) {
                    depthMap[x][y] = 3;
                } else {
                    depthMap[x][y] = 4; // Très profond
                }
            }
        }
    }

    public int getDepthAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return -1;
        }
        return depthMap[x][y];
    }

    public boolean canAddResourceAt(int x, int y) {
        int depth = getDepthAt(x, y);
        if (depth == -1) return false;

        DepthZone zone = depthZones.get(depth);
        return zone != null && zone.canAddResource();
    }

    public void incrementResourcesAt(int x, int y) {
        int depth = getDepthAt(x, y);
        if (depth != -1) {
            DepthZone zone = depthZones.get(depth);
            if (zone != null) {
                zone.incrementResources();
            }
        }
    }

    public void decrementResourcesAt(int x, int y) {
        int depth = getDepthAt(x, y);
        if (depth != -1) {
            DepthZone zone = depthZones.get(depth);
            if (zone != null) {
                zone.decrementResources();
            }
        }
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

    public void configureDepthZone(int depthLevel, int maxResources) {
        DepthZone zone = depthZones.get(depthLevel);
        if (zone != null) {
            zone.maxResources = maxResources;
        } else {
            depthZones.put(depthLevel, new DepthZone(depthLevel, maxResources));
        }
    }

    public int getMaxResourcesForDepth(int depthLevel) {
        DepthZone zone = depthZones.get(depthLevel);
        return zone != null ? zone.maxResources : 0;
    }

    public int getCurrentResourcesForDepth(int depthLevel) {
        DepthZone zone = depthZones.get(depthLevel);
        return zone != null ? zone.currentResources : 0;
    }


    private void initializeBackground(Terrain terrain, int numCubesX, int numCubesY) {
        cubeWidth = terrain.getWidth() / numCubesX;
        cubeHeight = terrain.getHeight() / numCubesY;
        backgroundDepthMap = new int[numCubesX][numCubesY];

        for (int i = 0; i < numCubesX; i++) {
            for (int j = 0; j < numCubesY; j++) {
                int startX = i * cubeWidth;
                int startY = j * cubeHeight;
                backgroundDepthMap[i][j] = calculateAverageDepth(terrain, startX, startY, cubeWidth, cubeHeight);
            }
        }
    }

    private int calculateAverageDepth(Terrain terrain, int startX, int startY, int width, int height) {
        int totalDepth = 0;
        int count = 0;

        for (int x = startX; x < startX + width && x < terrain.getWidth(); x++) {
            for (int y = startY; y < startY + height && y < terrain.getHeight(); y++) {
                totalDepth += terrain.getDepthAt(x, y);
                count++;
            }
        }

        return count > 0 ? totalDepth / count : 0;
    }

    public int[][] getBackgroundDepthMap() {
        return backgroundDepthMap;
    }

    public int getCubeWidth() {
        return cubeWidth;
    }
    public int getCubeHeight() {
        return cubeHeight;
    }



    private void loadDepthBackgroundImages() {
        depthBackgroundImages = new HashMap<>();
        depthBackgroundImages.put(1, GamePanel.getCachedImage("sea0.png"));
        depthBackgroundImages.put(2, GamePanel.getCachedImage("sea1.png"));
        depthBackgroundImages.put(3, GamePanel.getCachedImage("sea2.png"));
        depthBackgroundImages.put(4, GamePanel.getCachedImage("sea3.png"));
    }

    public Image getBackgroundImageForDepth(int depth) {
        return depthBackgroundImages.getOrDefault(depth, null);
    }


}

