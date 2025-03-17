package model.objets;

import controler.GestionCollisions;
import controler.ProximityChecker;
import controler.ThreadManager;
import view.GamePanel;

import java.util.concurrent.CopyOnWriteArrayList;

public class DeplacementThread extends Thread {
    private model.objets.Unite unite;
    private GamePanel panel;
    private static final int DELAY = 10;
    private volatile boolean running = true;
    public DeplacementThread(Unite unite) {
        this.unite = unite;
        // this.panel = panel;
    }

    public void stopThread() {
        running = false;
    }

    @Override
    public void run() {
        ThreadManager.incrementThreadCount("DeplacementThread");
        System.out.println("Thread de déplacement démarré pour l'unité ");
        while (running) {
            // Synchroniser l'accès à la destination
            Position destination;
            synchronized(unite) {
                destination = unite.getDestination();
            }

            if (destination == null) {
                // Si la destination est null, sortir  du thread
                break;
            }

            int dx = destination.getX() - unite.getPosition().getX();
            int dy = destination.getY() - unite.getPosition().getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance > unite.getRayon()) {
                double ratio = unite.getVitesse() / distance;
                int deplacementX = (int) (dx * ratio);
                int deplacementY = (int) (dy * ratio);
                unite.getPosition().setX(unite.getPosition().getX() + deplacementX);
                unite.getPosition().setY(unite.getPosition().getY() + deplacementY);
                GamePanel.getInstance().repaint();
            } else {
                // Destination atteinte, arrêter le déplacement
                unite.setDestination(null);
                boolean collisionDetected = false;
                if(unite instanceof UniteControlable) {
                    CopyOnWriteArrayList<model.objets.Objet> voisins = ProximityChecker.getInstance().getObjetDansMemeTile(unite);
                    for (Objet voisin : voisins) {
                        if (GestionCollisions.collisionCC(unite, voisin) != -1) {
                            collisionDetected = true;
                            GestionCollisions.preventOverlap(unite, voisin, GestionCollisions.collisionCC(unite, voisin));
                        }
                    }
                    if (!collisionDetected) {
                        int newX = unite.getPosition().getX() + dx;
                        int newY = unite.getPosition().getY() + dy;
                        unite.getPosition().setX(newX);
                        unite.getPosition().setY(newY);
                    }
                    GamePanel.getInstance().repaint();
                }
            }

            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread interrupted");
            }
        }
        ThreadManager.decrementThreadCount("DeplacementThread");
    }



}

