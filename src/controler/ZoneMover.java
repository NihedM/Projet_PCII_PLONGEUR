package controler;

import model.objets.Position;
import view.GamePanel;

public class ZoneMover extends Thread {
    private volatile boolean running = true;
    private final int updateInterval = 50;

    @Override
    public void run() {
        while (running) {
            //System.out.println("size: "+ GamePanel.getInstance().getDynamicZones().size());
            for (ZoneEnFonctionnement zone : GamePanel.getInstance().getDynamicZones()) {
                if (zone.getUnit() != null) {
                    updateDynamicZoneBounds(zone);
                }
            }
            GamePanel.getInstance().getMinimapPanel().repaint();
            try {
                Thread.sleep(updateInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void updateDynamicZoneBounds(ZoneEnFonctionnement dynamicZone) {
        Position position = dynamicZone.getUnit().getPosition().toTerrainPosition(GamePanel.getInstance().getTerrain(), GamePanel.getInstance().getCameraX(), GamePanel.getInstance().getCameraY());
        int halfSize = 200; // Half of the fixed size (400 / 2)

        int newMinX = position.getX() - halfSize;
        int newMinY = position.getY() - halfSize;
        int newMaxX = position.getX() + halfSize;
        int newMaxY = position.getY() + halfSize;

        dynamicZone.updateBounds(newMinX, newMinY, newMaxX, newMaxY);
        int width = newMaxX - newMinX;
        int height = newMaxY - newMinY;
        System.out.println("Updated Zone Position: MinX=" + newMinX + ", MinY=" + newMinY + ", MaxX=" + newMaxX + ", MaxY=" + newMaxY);
        System.out.println("Zone Width: " + width + ", Zone Height: " + height);

        GamePanel.getInstance().getMinimapPanel().repaint();
    }

    public void stopRunning() {
        running = false;
    }
}