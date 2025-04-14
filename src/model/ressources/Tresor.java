package model.ressources;

import model.objets.Position;
import model.objets.Ressource;




public class Tresor extends Ressource {


    public Tresor(Position position) {
        super(position, 10, 30, 15,"Trésor "  ); // Exemple : nom et valeur de la ressource
        setImage("tresor.png");
    }



}