package model.ressources;

import model.objets.Position;
import model.objets.Ressource;

public class Collier extends Ressource {
    public Collier(Position position) {

        super(position, 10,20, 20,"Collier ");
        setImage("collier.png");
    }

}
