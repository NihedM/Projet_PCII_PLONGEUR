package model.objets;

import java.awt.*;

public class UniteControlable extends Unite {
    private final int id;
    private boolean selected = false;
    private boolean dynamicZoneCreated = false;
    private model.objets.DeplacementThread movementThread;


    public UniteControlable(int id, Position position, int rayon, int vitesse, int hp) {
        super(position, rayon, vitesse,hp);
        this.id = id;
    }
    public int getId() {return id;}

    public boolean isSelected() {return selected;}
    public boolean contains(Point point) {
        int x = getPosition().getX();
        int y = getPosition().getY();
        int rayon = getRayon();
        return point.distance(x, y) <= rayon;
    }

    public void setSelected(boolean selected) {this.selected = selected;}

    public boolean isDynamicZoneCreated() {return dynamicZoneCreated;}
    public void setDynamicZoneCreated(boolean dynamicZoneCreated) {this.dynamicZoneCreated = dynamicZoneCreated;}
    public void setMovementThread(model.objets.DeplacementThread dt) {
        this.movementThread = dt;
    }
    public DeplacementThread getMovementThread() {
        return this.movementThread;
    }
}

