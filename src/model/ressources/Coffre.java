package model.ressources;

import model.objets.Position;
import model.objets.Ressource;

public class Coffre extends Ressource {
    public Coffre(Position position) {
        super(position, 20, 30, 15,"Coffre" );
        setImage("coffre.png");
    }
}
