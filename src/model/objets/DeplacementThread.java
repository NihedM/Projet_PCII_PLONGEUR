package model.objets;

import controler.GestionCollisions;
import controler.ThreadManager;
import controler.TileManager;
import view.GamePanel;
import model.unite_non_controlables.Calamar;

import java.util.logging.Level;
import java.util.logging.Logger;


public class DeplacementThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(DeplacementThread.class.getName());

    private Unite unite;

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
                    if (vitesseCourante < unite.getVitesseMax()) {
                        // Accélération progressive jusqu'à la vitesse maximale
                        vitesseCourante = Math.min(vitesseCourante + accel, unite.getVitesseMax());
                    } else if (distance > distanceInitiale * 0.5) {
                        // Maintien de la vitesse maximale jusqu'à 40% de la distance initiale
                        vitesseCourante = unite.getVitesseMax();
                    } else {
                        // Décélération rapide lorsque très proche de la destination
                        vitesseCourante = Math.max(vitesseCourante - accel * 2, 0.1);
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

                    //if (GamePanel.getInstance().isWithinTerrainBounds(new Position(newX, newY))) {        !!!!NOOOOOOO NEVER
                    unite.getPosition().setX(newX);
                    unite.getPosition().setY(newY);


                    /*if(vitesseCourante <= 0.01 && unite.getDestination() != null){
                        unite.setVitesseCourante(0.1);
                        unite.setAcceleration(0.2);
                    }*/

                    /*if (unite.getAcceleration() < 0.1 ) {
                        unite.setDestination(null);
                        unite.setVitesseCourante(0);
                        unite.setAcceleration(0.1);
                        unite.setVx(0);
                        unite.setVy(0);
                        break;
                    }*/




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
                    // Si la distance est très faible, fixer la position directement à la destination
                    unite.getPosition().setX(destination.getX());
                    unite.getPosition().setY(destination.getY());
                    unite.setVx(0);
                    unite.setVy(0);
                    unite.setVitesseCourante(0);
                    unite.setAcceleration(0.1);
                    unite.setDestination(null);
                    GamePanel.getInstance().repaint();


                    if (unite instanceof model.unite_controlables.Plongeur) {
                        model.unite_controlables.Plongeur p = (model.unite_controlables.Plongeur) unite;
                        if (p.getTargetResource() != null) {
                            if(GestionCollisions.collisionCC(p, p.getTargetResource()) > -1){
                                if(p.getTargetResource().estRecoltable()) {
                                    boolean collected = p.recolter(p.getTargetResource());
                                    if (collected) {
                                        // Une fois collectée, on réinitialise la cible et désactive le flag targeted
                                        p.getTargetResource().setTargeted(false);
                                        p.setTargetResource(null);
                                    }
                                }
                            }
                        }

                    }

                    unite.setVitesseCourante(0);
                    unite.setAcceleration(0.1);
                    unite.setDestination(null);

                    GamePanel.getInstance().repaint();
                }


                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    //Thread.currentThread().interrupt();
                    //System.out.println("Thread interrupted");
                }
            }
        }finally {
            ThreadManager.decrementThreadCount("DeplacementThread");
        }
    }

}




