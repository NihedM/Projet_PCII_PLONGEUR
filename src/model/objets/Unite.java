package model.objets;

import view.Redessine;

import java.util.HashMap;
import java.util.Map;

public class Unite extends Objet {

    private DeplacementThread deplacementThread;
    private Position destination;

    private double vx, vy; // Vecteur vitesse
    private double vitesseCourante = 0.0; //vitesse courrante
    private double vitesseMax; //vitesse de l'unité
    private double acceleration = 0.1; //acceleration de l'unité
    private int heat_points;    //vie de l'unité


    public Unite(Position position, int rayon, double vitesse, int heat_points) {
        super(position, rayon);
        this.vitesseMax= vitesse;
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
    public double getVitesseCourante() {return vitesseCourante;}

    public void setVitesseCourante(double vitesse) {this.vitesseCourante = vitesse;}

    public double getVitesseMax() {return vitesseMax;}
    public void setVitesseMax(double vitesseMax) {this.vitesseMax = vitesseMax;}
    public double getAcceleration() {return acceleration;}
    public void setAcceleration(double acceleration) {this.acceleration = acceleration;}

    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public void setVx(double vx) { this.vx = vx; }
    public void setVy(double vy) { this.vy = vy; }

    public synchronized void setDestination(Position destination) {

        synchronized (this) {

            if (this.destination != null && this.destination.equals(destination)) {
                return;
            }



            if (deplacementThread != null) {
                deplacementThread.stopThread();
            }



            if (destination != null) {
                this.destination = new Position(destination.getX(), destination.getY());

                if (deplacementThread != null)
                    deplacementThread.stopThread();


                deplacementThread = new DeplacementThread(this);
                deplacementThread.start();
            }
            else this.destination = null;
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
        return "HP: " + get_Hp() + ", Vitesse: " + getVitesseCourante();
    }


    public Map<String, String> getAttributes() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("HP", String.valueOf(get_Hp()));
        return attributes;
    }


    public DeplacementThread getDeplacementThread() {return deplacementThread;}
}

