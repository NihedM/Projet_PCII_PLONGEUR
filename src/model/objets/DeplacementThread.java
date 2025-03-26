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
        while (running) {
            // Si le jeu est en pause, le thread attend
            while (GamePanel.getInstance().isPaused()) {
                try {
                    Thread.sleep(50);  // Petite pause / ralentissement
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            // Récupération sécurisée de la destination
            Position destination;
            synchronized(unite) {
                destination = unite.getDestination();
            }

            if (destination == null) {
                // Si la destination est null, sortir du thread
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

                unite.setDestination(null);

                if (unite instanceof model.unite_controlables.Plongeur) {
                    model.unite_controlables.Plongeur p = (model.unite_controlables.Plongeur) unite;
                    if (p.getTargetResource() != null) {
                        int dxRes = p.getPosition().getX() - p.getTargetResource().getPosition().getX();
                        int dyRes = p.getPosition().getY() - p.getTargetResource().getPosition().getY();
                        double distRes = Math.sqrt(dxRes * dxRes + dyRes * dyRes);
                        if (distRes <= p.getRayon() + p.getTargetResource().getRayon()) {
                            boolean collected = p.recolter(p.getTargetResource());
                            if (collected) {
                                // Une fois collectée, on réinitialise la cible et désactive le flag targeted
                                p.getTargetResource().setTargeted(false);
                                p.setTargetResource(null);
                            }
                        }
                    }
                    //Vérifie si le plongeur est arrivé à la base pour livrer son backpack
                    Position base = GamePanel.BASE_POSITION;
                    int dxBase = p.getPosition().getX() - base.getX();
                    int dyBase = p.getPosition().getY() - base.getY();
                    double distBase = Math.sqrt(dxBase * dxBase + dyBase * dyBase);
                    if (distBase <= 20) { // seuil de proximité à ajuster si nécessaire
                        p.deliverBackpack();
                    }
                }
                GamePanel.getInstance().repaint();
            }

            // Gestion de la mort du Calamar si hors terrain
            if (unite instanceof model.unite_non_controlables.Calamar &&
                    !GamePanel.getInstance().isWithinTerrainBounds(unite.getPosition())) {
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

