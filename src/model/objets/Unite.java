package model.objets;

public class Unite extends Objet {

    private model.objets.DeplacementThread deplacementThread;
    private Position destination;
    private int VITESSE;


    public Unite(Position position, int rayon, int vitesse) {
        super(position, rayon);
        this.VITESSE = vitesse;
        this.destination = null;
        this.deplacementThread =null;
    }


    public synchronized Position getDestination() {
        return destination;
    }
    public int getVitesse() {return VITESSE;}

    public void setVitesse(int vitesse) {this.VITESSE = vitesse;}
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

