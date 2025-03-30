package model.unite_non_controlables;

import controler.GestionCollisions;
import model.objets.Objet;
import model.objets.Position;
import model.objets.Ressource;
import view.GamePanel;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Calamar extends Enemy {

    ArrayList<Object> inventaire = new ArrayList<Object>();
    private Ressource objectifCourrant;
    private CopyOnWriteArrayList<Ressource> ressourcesDisponibles;

    public Calamar(Position position) {
        super(position,5, 5, 5);
        this.ressourcesDisponibles = new CopyOnWriteArrayList<>();
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
            this.setDestination(new Position(-1, y));
        } else if (minDistance == distanceToRightEdge) {
            this.setDestination(new Position(GamePanel.TERRAIN_WIDTH, y));
        } else if (minDistance == distanceToTopEdge) {
            this.setDestination(new Position(x, -1));
        } else {
            this.setDestination(new Position(x, GamePanel.TERRAIN_HEIGHT));
        }

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
    public void action() {
        //si l'objet est à portée, le ramasser
        if(getEtat() == Etat.FUITE)return;
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

