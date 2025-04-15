package model.unite_non_controlables;

import controler.GameMaster;
import controler.GestionCollisions;
import model.objets.Objet;
import model.objets.Position;
import model.objets.Ressource;
import view.GamePanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Calamar extends Enemy {

    ArrayList<Object> inventaire = new ArrayList<Object>();
    private Ressource objectifCourrant;
    private CopyOnWriteArrayList<Ressource> ressourcesDisponibles;

    private boolean isTimerPaused = false;

    public Calamar(Position position) {
        super(position,10, 45, 10);
        this.ressourcesDisponibles = new CopyOnWriteArrayList<>(GamePanel.getInstance().getRessourcesMap());

        setEtat(Etat.VADROUILLE);

        setImage("calamar.png");
        setMovingImage("calamar.png");
        setUnitIcon(new ImageIcon(GamePanel.getCachedImage("calamarIcon.png")));

    }

    public void setRessourcesDisponibles(CopyOnWriteArrayList<Ressource> ressourcesDisponibles){
        this.ressourcesDisponibles = ressourcesDisponibles;
    }

    public void selectionneRessourcePlusProche(CopyOnWriteArrayList<Ressource> ressources) {
        Ressource ressourcePlusProche = (Ressource)(super.selectClosest(new CopyOnWriteArrayList<Objet>(ressources)));
        //definir la ressource la plus proche comme objectif
        if (ressourcePlusProche != null) {
            this.setDestination(ressourcePlusProche.getPosition());
            //System.out.println("Ressource " + ressourcePlusProche.getPosition().getX() + " " + ressourcePlusProche.getPosition().getY());
            this.objectifCourrant = ressourcePlusProche;
        }

    }

    public void verifierObjectif() {
        if (objectifCourrant == null || !ressourcesDisponibles.contains(objectifCourrant)) {
            selectionneRessourcePlusProche(ressourcesDisponibles);
        }
        if(ressourcesDisponibles.isEmpty())
            fuit();
    }


    /*public void collecterObjet() {
        //si l'objet est à portée, le ramasser
        if (objectifCourrant != null && getEtat() == Etat.VADROUILLE && ressourcesDisponibles.contains(objectifCourrant)) {
            inventaire.add(objectifCourrant);
            ressourcesDisponibles.remove(objectifCourrant);
            objectifCourrant = null;
        }
        if(!ressourcesDisponibles.isEmpty())
            collectePlusProche(ressourcesDisponibles);
        else fuit();
    }
*/

    @Override
    public void fuit() {
        fuire();
        stopTimer();
        Position currentPosition = this.getPosition();
        int x = currentPosition.getX();
        int y = currentPosition.getY();

        int distanceToLeftEdge = x - GamePanel.TERRAIN_MIN_X;
        int distanceToRightEdge = GamePanel.TERRAIN_MAX_X - x;
        int distanceToTopEdge = y - GamePanel.TERRAIN_MIN_Y;
        int distanceToBottomEdge = GamePanel.TERRAIN_MAX_Y - y;

        int minDistance = Math.min(Math.min(distanceToLeftEdge, distanceToRightEdge), Math.min(distanceToTopEdge, distanceToBottomEdge));

        if (minDistance == distanceToLeftEdge) {
            this.setDestination(new Position(-500, y));
        } else if (minDistance == distanceToRightEdge) {
            this.setDestination(new Position(GamePanel.TERRAIN_WIDTH + 500, y));
        } else if (minDistance == distanceToTopEdge) {
            this.setDestination(new Position(x, -500));
        } else {
            this.setDestination(new Position(x, GamePanel.TERRAIN_HEIGHT + 500));
        }
        setVitesseCourante(getVitesseMax());

        this.objectifCourrant = null;

    }


    @Override
    public void setup(CopyOnWriteArrayList<Objet> interactionTargets) {
        this.ressourcesDisponibles = new CopyOnWriteArrayList<>();
        for (Objet obj : interactionTargets) {
            if (obj instanceof Ressource) {
                this.ressourcesDisponibles.add((Ressource) obj);
            }
        }
    }
    @Override
    public void stopAction() {
        super.stopAction(); // Call the parent class's stopAction logic
        stopTimer(); // Pause the timer
        isTimerPaused = true; // Mark the timer as paused
    }


    @Override
    public void action() {

        if (isTimerPaused) {
            startTimer();
            isTimerPaused = false;
            ressourcesDisponibles = new CopyOnWriteArrayList<>(GamePanel.getInstance().getRessourcesMap());
            selectionneRessourcePlusProche(ressourcesDisponibles);
        }



        if(getEtat() != Etat.VADROUILLE)return;


        if (objectifCourrant == null || !ressourcesDisponibles.contains(objectifCourrant)) {
                // Sélectionner une nouvelle ressource comme cible
            ressourcesDisponibles = new CopyOnWriteArrayList<>(GamePanel.getInstance().getRessourcesMap());
            selectionneRessourcePlusProche(ressourcesDisponibles);
        }

        if (ressourcesDisponibles.isEmpty()) {
            fuit();
            return;
        }


        if(GestionCollisions.collisionCC(this, objectifCourrant) > -1 ) {
            if (ressourcesDisponibles.contains(objectifCourrant)) {

                inventaire.add(objectifCourrant);
                ressourcesDisponibles.remove(objectifCourrant);
                GamePanel.getInstance().removeObjet(objectifCourrant, objectifCourrant.getCoordGrid());
                GamePanel.getInstance().checkAndClearResourcePanel(objectifCourrant);

                objectifCourrant = null;
            }

        }

        if (!ressourcesDisponibles.isEmpty())
            selectionneRessourcePlusProche(ressourcesDisponibles);
        else fuit();

        /*sur le terminale on affiche les ressources disponibles*/
        //System.out.println("Ressources disponibles: " + ressourcesDisponibles.size());

    }


}

