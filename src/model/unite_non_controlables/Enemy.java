package model.unite_non_controlables;

import controler.TileManager;
import model.objets.*;

import java.util.Random;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;

public class Enemy extends Unite implements UniteNonControlableInterface {
    protected enum Etat {
        VADROUILLE, FUITE, ATTENTE
    }
    public static final double VITESSE_ATTENTE = 2.0;
    private static final int ATTENTE_RANGE = TileManager.TILESIZE*2;


    private Etat etat;
    private boolean insideZone = true;

    private int secondesRestant;
    private Timer timer = new Timer();
    private Random random = new Random();


    public Enemy(Position position, int rayon, int secondesRestant, double vitesse) {
        super(position, rayon, vitesse, 5);
        this.etat = Etat.VADROUILLE;
        this.secondesRestant = secondesRestant;
        timer.schedule(new model.objets.Fuite(this), secondesRestant * 1000); // tempsRestant en secondes
    }

    public double getVitesseAttente() {return VITESSE_ATTENTE;}
    public boolean isInsideZone() {return insideZone;}
    public void setInsideZone(boolean insideZone) {this.insideZone = insideZone;}


    public Random getRandom() {return random;}

    public Etat getEtat() {return etat;}
    public void setEtat(Etat etat) {this.etat = etat;}
    public int getTempsRestant() {return secondesRestant;}

    public void fuit() {        //version default:  pas oublier de faire override sinon!!!
        this.etat = Etat.FUITE;
        this.setDestination(new Position(-1, -1));
    }
    public void vadrouille() { this.etat = Etat.VADROUILLE;}
    public void fuire(){this.etat= Etat.FUITE;}


    public void decrementerTemps(int t){secondesRestant -= t;}
    public void stopTimer(){timer.cancel();}
    public void startTimer(){
        timer.schedule(new model.objets.Fuite(this), secondesRestant * 1000); // tempsRestant en secondes
    }


    public Objet selectClosest(CopyOnWriteArrayList<Objet> objets){
        //trouver l'objet le plus proche
        Objet objetPlusProche = null;
        double distanceMin = Double.MAX_VALUE;
        for (Objet objet : objets) {
            double distance = this.distance(objet);
            if (distance < distanceMin) {
                distanceMin = distance;
                objetPlusProche = objet;
            }
        }

        return objetPlusProche;
    }



    public void attente(){
        setVitesseCourante(VITESSE_ATTENTE);

        if(getDestination() != null)return;
        int dx = random.nextInt(2*ATTENTE_RANGE)* (random.nextBoolean() ? 1 : -1);
        int dY = random.nextInt(2*ATTENTE_RANGE)* (random.nextBoolean() ? 1 : -1);
        this.setDestination(new Position(getPosition().getX() + dx, getPosition().getY() + dY));
    }

    public void stopAllThreads() {
        if (getDeplacementThread() != null) {
            getDeplacementThread().stopThread();
            setDestination(null);
        }
        stopTimer();
    }


    @Override
    public void setup(CopyOnWriteArrayList<Objet> interactionTargets) {
        //lancer une erreur de sort que si on a pas override la methode
        //throw new UnsupportedOperationException("Override this method");

    }
    public void action(){}



}

