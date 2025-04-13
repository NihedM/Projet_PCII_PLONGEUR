package model.objets;

import controler.GestionCollisions;
import controler.AmmoManager;
import view.GamePanel;

public class Ammo extends Objet {
    private Position target;
    private int damage, speed;
    private boolean reachedTarget = false;
    private final Position destination;
    private final UniteControlable shooter;
    private static final int MAX_LIFETIME = 5000;//5 secondes
    private final long creationTime;

    public Ammo(Position position, int rayon, Position target, int damage, int speed, UniteControlable shooter) {
        super(position, rayon);
        this.creationTime = System.currentTimeMillis();
        this.shooter = shooter;
        this.target = target;
        this.destination = new Position(target.getX(), target.getY());
        this.damage = damage;
        this.speed = speed;
        AmmoManager.getInstance().addAmmo(this);

        setImage("ammo.png");

    }

    public boolean hasExceededLifetime() {
        return System.currentTimeMillis() - creationTime > MAX_LIFETIME;
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
    public Position getTarget() {
        return target;
    }
    public void stop(){this .reachedTarget = true;}

    public boolean reachedDestination() {
        return this.distance(target) <= getRayon() || reachedTarget;
    }

    public boolean checkCollision(Objet objet) {
        if (objet == shooter) {
            return false;
        }
        return GestionCollisions.collisionCC(this, objet) > -1;
    }

    public void applyDamage(Objet objet) {
        if (objet instanceof Unite) {
            ((Unite) objet).takeDamage(damage);
            System.out.println("Bullet hit " + objet + "! Damage dealt: " + damage);

        }
    }

    public void deplacementAmmo(){
        if (reachedTarget || target == null ) {
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