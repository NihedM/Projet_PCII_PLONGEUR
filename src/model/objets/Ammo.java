package model.objets;

import controler.GestionCollisions;
import controler.AmmoManager;
import view.GamePanel;

public class Ammo extends Objet {
    private DeplacementThread deplacementThread;
    private Unite target;
    private int damage, speed;
    private boolean reachedTarget = false;
    private final Position destination;

    public Ammo(Position position, int rayon, Unite target, int damage, int speed) {
        super(position, rayon);
        this.target = target;
        this.destination = new Position(target.getPosition().getX(), target.getPosition().getY());
        this.damage = damage;
        this.speed = speed;
        AmmoManager.getInstance().addAmmo(this);
    }

    public Position getDestination() {
        return destination;
    }

    public int getVitesse() {
        return speed;
    }
    public int getDamage() {
        return damage;
    }
    public Unite getTarget() {
        return target;
    }
    public void stop(){this .reachedTarget = true;}

    public boolean reachedDestination(){return reachedTarget;}





    public void deplacementAmmo(){
        if (reachedTarget || target == null || !target.isAlive()) {
            reachedTarget = true;
            return;
        }

        if(GestionCollisions.collisionCC(this, target) > -1){
            target.takeDamage(getDamage());
            if(target.get_Hp() <= 0)
                GamePanel.getInstance().killUnite(target);
            reachedTarget = true;
            return;
        }

        // Calcul du vecteur direction et de la distance à parcourir
        int dx = destination.getX() - getPosition().getX();
        int dy = destination.getY() - getPosition().getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.atan2(dy, dx);

        int newX = getPosition().getX() + (int) (speed * Math.cos(angle));
        int newY = getPosition().getY() + (int) (speed * Math.sin(angle));
        getPosition().setX(newX);
        getPosition().setY(newY);

        if(distance < getRayon()){
            reachedTarget = true;
            return;
        }


        GamePanel.getInstance().repaint();


    }

}