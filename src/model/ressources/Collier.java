package model.ressources;

import model.objets.Position;
import model.objets.Ressource;

public class Collier extends Ressource {
    private static int compteur = 0;
    public Collier(Position position) {

        super(position, 10,20, 20,"Collier "); // Valeur de 100 points de victoire, temps de croissance de 10 secondes, rayon 5
        setImage("collier.png");
    }

}
