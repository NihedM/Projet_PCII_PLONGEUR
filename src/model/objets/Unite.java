package model.objets;

public class Unite extends Objet {

    private model.objets.DeplacementThread deplacementThread;
    private Position destination;

    private double vx, vy; // Vecteur vitesse
    private int VITESSE;


    public Unite(Position position, int rayon, int vitesse) {
        super(position, rayon);
        this.VITESSE = vitesse;
        this.destination = null;
        this.deplacementThread =null;
        this.vx = 0;
        this.vy = 0;
    }


    public synchronized Position getDestination() {
        return destination;
    }
    public int getVitesse() {return VITESSE;}

    public void setVitesse(int vitesse) {this.VITESSE = vitesse;}

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



}

