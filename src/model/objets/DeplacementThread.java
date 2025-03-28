package model.objets;

import controler.ThreadManager;
import controler.TileManager;
import view.GamePanel;
import model.unite_non_controlables.Calamar;

import java.util.logging.Level;
import java.util.logging.Logger;


public class DeplacementThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(DeplacementThread.class.getName());

    private model.objets.Unite unite;

    private static final int DELAY = 30;
    private volatile boolean running = true;

    private double distanceInitiale = 0;

    public DeplacementThread(Unite unite) {
        this.unite = unite;
    }

    public void stopThread() {
        running = false;
        this.interrupt();
    }


    public double setDistanceInitiale(double distanceInitiale) {
        return this.distanceInitiale = distanceInitiale;
    }


    public double getDistanceInitiale() {
        return distanceInitiale;
    }



    @Override
    public void run() {
        ThreadManager.incrementThreadCount("DeplacementThread");
        try {
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
                synchronized (unite) {
                    destination = unite.getDestination();
                }

                if (destination == null) {
                    // Si la destination est null, sortir du thread
                    break;
                }

               if (unite.getAcceleration() < 0.1 ) {
                    unite.setDestination(null);
                    unite.setVitesseCourante(0);
                    unite.setAcceleration(0.1);
                    unite.setVx(0);
                    unite.setVy(0);
                    break;
                }


                // Calcul du vecteur direction et de la distance à parcourir
                int dx = destination.getX() - unite.getPosition().getX();
                int dy = destination.getY() - unite.getPosition().getY();
                double distance = Math.sqrt(dx * dx + dy * dy);
                double angle = Math.atan2(dy, dx);

                if (distanceInitiale == 0) {
                    distanceInitiale = distance;
                }


                // Si la distance est très faible, fixer la position directement à la destination
                /*if (distance < 2) {
                    unite.getPosition().setX(destination.getX());
                    unite.getPosition().setY(destination.getY());
                    unite.setVx(0);
                    unite.setVy(0);
                    unite.setVitesseCourante(0);
                    unite.setAcceleration(0.1);
                    unite.setDestination(null);
                    GamePanel.getInstance().repaint();
                    return;
                }*/

                // Récupération de la vitesse courante et de l'accélération
                double vitesseCourante = unite.getVitesseCourante();
                double accel = unite.getAcceleration();

                if (distance > unite.getRayon()) {

                    // Phase d'accélération ou de maintien
                    if (distance > distanceInitiale / 2) {
                        // Accélération jusqu'à la vitesse maximale
                        vitesseCourante = Math.min(vitesseCourante + accel, unite.getVitesseMax());
                    } else {
                        vitesseCourante = Math.max(vitesseCourante - accel, accel);
                    }

                    // Mise à jour de la vitesse courante
                    unite.setVitesseCourante(vitesseCourante);

                    // Calcul des composantes du vecteur vitesse
                    double newVx = vitesseCourante * Math.cos(angle);
                    double newVy = vitesseCourante * Math.sin(angle);
                    unite.setVx(newVx);
                    unite.setVy(newVy);

                    // Mise à jour de la position
                    int newX = unite.getPosition().getX() + (int) newVx;
                    int newY = unite.getPosition().getY() + (int) newVy;
                    if (GamePanel.getInstance().isWithinTerrainBounds(new Position(newX, newY))) {
                        unite.getPosition().setX(newX);
                        unite.getPosition().setY(newY);
                    } else {
                        unite.setDestination(null);
                    }



                    if(vitesseCourante <= 0.001){
                        unite.setDestination(null);
                    }




                    GamePanel.getInstance().repaint();

                } else {

                    // Arrêt progressif lorsque la destination est atteinte
                    /*unite.setVx(unite.getVx() * 0.85);
                    unite.setVy(unite.getVy() * 0.85);
                    if (Math.abs(unite.getVx()) < 0.2 && Math.abs(unite.getVy()) < 0.2) {
                        unite.setVx(0);
                        unite.setVy(0);

                        // Destination atteinte, arrêter le déplacement
                    }
                    GamePanel.getInstance().repaint();
*/


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

                    unite.setVitesseCourante(0);
                    unite.setAcceleration(0.1);
                    unite.setDestination(null);

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
                    //Thread.currentThread().interrupt();
                    System.out.println("Thread interrupted");
                }
            }
        }finally {
            ThreadManager.decrementThreadCount("DeplacementThread");
        }
    }
}
