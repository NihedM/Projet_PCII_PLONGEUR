package model.constructions;

import model.objets.Position;
import model.unite_controlables.SousMarin;

import java.util.ArrayList;
import java.util.List;

public class Base extends Construction {


    //on a un cercle et un rectangle

    private final int largeur, longueur;
    private final Position topLeft, topRight, bottomLeft, bottomRight;

    private List<SousMarin> submarines;


    public Base(Position position, int rayon) {
        super(position, rayon);
        this.largeur = rayon * 8;
        this.longueur = rayon * 8;

        this.topLeft = new Position(position.getX() - largeur / 2, position.getY() - longueur / 2);
        this.topRight = new Position(position.getX() + largeur / 2, position.getY() - longueur / 2);
        this.bottomLeft = new Position(position.getX() - largeur / 2, position.getY() + longueur / 2);
        this.bottomRight = new Position(position.getX() + largeur / 2, position.getY() + longueur / 2);

        submarines = new ArrayList<>();

        setImage("base.png");

    }

    public int getLargeur() {
        return largeur;
    }

    public int getLongueur() {
        return longueur;
    }



    public Position[] getCoints() {
        return new Position[]{topLeft, topRight, bottomLeft, bottomRight};
    }



}
