package model.objets;

import java.util.HashMap;
import java.util.Map;

public class Unite extends Objet {

    private DeplacementThread deplacementThread;
    private Position destination;

    private double vx, vy; // Vecteur vitesse
    private double VITESSE; //vitesse courrante
    private int heat_points;    //vie de l'unité


    public Unite(Position position, int rayon, double vitesse, int heat_points) {
        super(position, rayon);
        this.VITESSE = vitesse;
        this.destination = null;
        this.deplacementThread =null;
        this.vx = 0;
        this.vy = 0;
        this.heat_points = heat_points;
    }


    public synchronized Position getDestination() {
        return destination;
    }
    public int get_Hp() {return heat_points;}
    public void set_Hp(int hp) {this.heat_points = hp;}
    public double getVitesse() {return VITESSE;}

    public void setVitesse(double vitesse) {this.VITESSE = vitesse;}

    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public void setVx(double vx) { this.vx = vx; }
    public void setVy(double vy) { this.vy = vy; }

    public void setDestination(Position destination) {
        this.destination = destination;
        if (deplacementThread != null) {
            deplacementThread.stopThread();
        }
        if (destination != null) {
            deplacementThread = new DeplacementThread(this);
            deplacementThread.start();
        }
    }

    // methode estProcheDe //TODO : ajouter un perimetre de detection (peut etre)
    /*public boolean estProcheDe(Ressource ressource) {
        int dx = ressource.getPosition().getX() - this.getPosition().getX();
        int dy = ressource.getPosition().getY() - this.getPosition().getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= this.getRayon() + 30;
    }*/

    public String getInfo() {
        return "HP: " + get_Hp() + ", Vitesse: " + getVitesse();
    }


    public Map<String, String> getAttributes() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("HP", String.valueOf(get_Hp()));
        return attributes;
    }


}

