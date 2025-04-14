package model.ressources;

import model.objets.Position;
import model.objets.Ressource;




public class Tresor extends Ressource {


    public Tresor(Position position) {
        super(position, 20, 30, 15,"Trésor "  );
        setImage("tresor.png");
    }



}