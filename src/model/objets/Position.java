package model.objets;

import view.GamePanel;

import java.util.Random;

public class Position {
    private int x, y;           // vraie position

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position() {
        /*generate random position dans le terrain terrainwidth heigth */
        Random random = new Random();
        this.x = random.nextInt(GamePanel.TERRAIN_WIDTH);
        this.y = random.nextInt(GamePanel.TERRAIN_HEIGHT);


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

    public Position toTerrainPosition(Terrain terrain, int cameraX, int cameraY) {
        return terrain.panelToTerrain(this, cameraX, cameraY);
    }

    public Position toPanelPosition(Terrain terrain, int cameraX, int cameraY) {
        return terrain.terrainToPanel(this, cameraX, cameraY);
    }



}
