package model.objets;

import controler.ThreadManager;
import view.GamePanel;
import model.unite_non_controlables.Calamar;



public class DeplacementThread extends Thread {
    private model.objets.Unite unite;

    private static final int DELAY = 30;
    private volatile boolean running = true;
    public DeplacementThread(Unite unite) {
        this.unite = unite;
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
                double angle = Math.atan2(dy, dx);
                unite.setVx(unite.getVitesse() * Math.cos(angle));
                unite.setVy(unite.getVitesse() * Math.sin(angle));

                unite.getPosition().setX(unite.getPosition().getX() + (int) unite.getVx());
                unite.getPosition().setY(unite.getPosition().getY() + (int) unite.getVy());
                GamePanel.getInstance().repaint();

            } else {
                // Destination atteinte, arrêter le déplacement
                unite.setDestination(null);

                boolean collisionDetected = false;
                if(unite instanceof UniteControlable) {

                    if (!collisionDetected) {
                        int newX = unite.getPosition().getX() + dx;
                        int newY = unite.getPosition().getY() + dy;
                        unite.getPosition().setX(newX);
                        unite.getPosition().setY(newY);
                    }
                    GamePanel.getInstance().repaint();
                }
            }


                //temporaire faut gérer la mort des unités

            if (unite instanceof Calamar && !GamePanel.getInstance().isWithinTerrainBounds(unite.getPosition())) {
                GamePanel.getInstance().removeObjet(unite, unite.getCoordGrid());
                break;
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

