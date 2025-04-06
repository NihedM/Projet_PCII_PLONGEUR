package controler;

import model.objets.Ammo;


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
            } else {
                bullet.deplacementAmmo();
            }
        }
    }

    public CopyOnWriteArrayList<Ammo> getActiveAmmo() {
        return activeBullets;
    }
}
