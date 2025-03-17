package model.ressources;

import model.objets.Position;
import model.objets.Ressource;

public class Collier extends Ressource {
    public Collier(Position position) {

        super(position, 5,100, 5); // Valeur de 100 points de victoire, temps de croissance de 10 secondes, rayon 5
    }

}
