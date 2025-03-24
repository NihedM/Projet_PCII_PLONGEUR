package model.objets;

import controler.TileManager;

import java.util.HashMap;
import java.util.Map;

public class Objet {
    private Position position;
    private CoordGrid coordGrid;

    private final int rayon;                //TODO faire les constantes(rayons) de tout les objets


    public Objet(Position position, int rayon) {
        this.position = position;
        this.rayon = rayon;
        this.coordGrid = TileManager.transformePos_to_Coord(position);

    }


    public Position getPosition() {return position;}

    public CoordGrid getCoordGrid() {return coordGrid;}

    public int getRayon() {return rayon;}


    public double distance(model.objets.Objet other) {
        int dx = position.getX() - other.getPosition().getX();
        int dy = position.getY() - other.getPosition().getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public synchronized void updatePosition() {
        coordGrid = TileManager.transformePos_to_Coord(position);
    }

    public Map<String, String> getAttributes() {
        Map<String, String> attributes = new HashMap<>();
        return attributes;
    }
}

