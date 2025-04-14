package model.ressources;

import model.objets.Position;
import model.objets.Ressource;


public class Bague extends Ressource {
    public Bague(Position position) {
        super(position, 10, 30, 15,"Bague " );
        setImage("bague.png");
    }

}
