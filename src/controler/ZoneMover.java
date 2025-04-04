package controler;

import model.objets.Position;
import view.GamePanel;

public class ZoneMover extends Thread {
    private volatile boolean running = true;
    private final int updateInterval = 100;

    @Override
    public void run() {
        while (running) {
            //System.out.println("size: "+ GamePanel.getInstance().getDynamicZones().size());
            int i = 0;
            for (ZoneEnFonctionnement zone : GamePanel.getInstance().getDynamicZones()) {
                if (zone.getUnit() != null) {
                    i++;
                    //System.out.println("Updating zone bounds for unit: " + zone.getUnit().getClass().getSimpleName()+ " " + i);
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
        Position position = dynamicZone.getUnit().getPosition();

        int newMinX = position.getX() - GamePanel.UNIT_BUFFER;
        int newMinY = position.getY() - GamePanel.UNIT_BUFFER;
        int newMaxX = position.getX() + GamePanel.UNIT_BUFFER;
        int newMaxY = position.getY() +GamePanel.UNIT_BUFFER;


        dynamicZone.updateBounds(newMinX, newMinY, newMaxX, newMaxY);
    }

    public static boolean isInsideAnyZone(Position position) {
        GamePanel gamePanel = GamePanel.getInstance();
        if (gamePanel.getMainZone().isInsideMain(position)) {
            return true;
        }
        for (ZoneEnFonctionnement zone : gamePanel.getDynamicZones()) {
            if (zone.isInsideDynamic(position)) {
                return true;
            }
        }
        return false;
    }

}