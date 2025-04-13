package controler;

import model.objets.Ammo;
import model.objets.CoordGrid;
import model.objets.Objet;
import view.GamePanel;


import java.util.concurrent.CopyOnWriteArrayList;

public class AmmoManager extends Thread{
    private final CopyOnWriteArrayList<Ammo> activeBullets = new CopyOnWriteArrayList<>();
    private static volatile AmmoManager instance;
    private volatile boolean running = true;
    private static final int UPDATE_INTERVAL = 16; // ~60 FPS


    public AmmoManager() {
        instance = this;
    }

    public static synchronized AmmoManager getInstance() {

        return instance;
    }

    public void addAmmo(Ammo ammo) {
        activeBullets.add(ammo);
    }

    private CopyOnWriteArrayList<Objet> getVoisins(Ammo ammo) {
        CopyOnWriteArrayList<Objet> voisins = new CopyOnWriteArrayList<>();
        ammo.updatePosition();
        CoordGrid coord = ammo.getCoordGrid();

        CopyOnWriteArrayList<Objet> objetsDansTile = GamePanel.getInstance().getObjetsMap().get(coord);
        if (objetsDansTile != null) {
            voisins.addAll(objetsDansTile);
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;

                CoordGrid neighborCoord = new CoordGrid(coord.getX() + dx, coord.getY() + dy);
                CopyOnWriteArrayList<Objet> objetsDansTileVoisine = GamePanel.getInstance().getObjetsMap().get(neighborCoord);
                if (objetsDansTileVoisine != null) {
                    voisins.addAll(objetsDansTileVoisine);
                }
            }
        }

        return voisins;
    }

    @Override
    public void run() {
        while (running) {
            long startTime = System.currentTimeMillis();

            updateBullets();

            // Maintain consistent update rate
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed < UPDATE_INTERVAL) {
                try {
                    Thread.sleep(UPDATE_INTERVAL - elapsed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void updateBullets() {
        for (Ammo bullet : activeBullets) {
            if (bullet.reachedDestination()) {
                activeBullets.remove(bullet);
                GamePanel.getInstance().repaint();
            } else {
                bullet.deplacementAmmo();
                CopyOnWriteArrayList<Objet> voisins = getVoisins(bullet);
                for (Objet voisin : voisins) {
                    if (bullet.checkCollision(voisin)) {
                        // Apply damage and destroy the bullet
                        bullet.applyDamage(voisin);
                        activeBullets.remove(bullet);
                        GamePanel.getInstance().repaint();

                        break; // Stop checking after a collision
                    }
                }
            }

            if(bullet.getVitesse() == 0) {
                activeBullets.remove(bullet);
                GamePanel.getInstance().repaint();
            }
        }
    }

    public CopyOnWriteArrayList<Ammo> getActiveAmmo() {
        return activeBullets;
    }
}
