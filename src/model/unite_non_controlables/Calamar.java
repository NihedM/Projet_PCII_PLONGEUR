package model.unite_non_controlables;

import controler.GestionCollisions;
import model.objets.Position;
import model.objets.Ressource;

import java.util.ArrayList;

public class Calamar extends Enemy {

    ArrayList<Object> inventaire = new ArrayList<Object>();
    public Ressource objectifCourrant;
    private ArrayList<Ressource> ressourcesDisponibles;

    public Calamar(Position position) {
        super(position,5, 5, 10);
        action();
    }

    public void setRessourcesDisponibles(ArrayList<Ressource> ressourcesDisponibles){
        this.ressourcesDisponibles = ressourcesDisponibles;
    }

    public void selectionneRessourcePlusProche(ArrayList<Ressource> ressources) {
        //trouver la ressource la plus proche
        Ressource ressourcePlusProche = null;
        double distanceMin = Double.MAX_VALUE;
        for (Ressource ressource : ressources) {
            double distance = this.distance(ressource);
            if (distance < distanceMin) {
                distanceMin = distance;
                ressourcePlusProche = ressource;
            }
        }
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
    public void action() {
        //si l'objet est à portée, le ramasser
        if(GestionCollisions.collisionCC(this, objectifCourrant) > -1 ) {
            if (    getEtat() == Etat.VADROUILLE
                    && ressourcesDisponibles.contains(objectifCourrant)
                    && objectifCourrant.estRecoltable()) {

                inventaire.add(objectifCourrant);
                ressourcesDisponibles.remove(objectifCourrant);
                objectifCourrant = null;
            }
            if (!ressourcesDisponibles.isEmpty())
                selectionneRessourcePlusProche(ressourcesDisponibles);
            else fuit();
        }
    }

}

