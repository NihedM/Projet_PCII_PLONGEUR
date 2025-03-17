package model.unite_non_controlables;

import model.objets.Position;
import model.objets.Unite;

import java.util.Timer;

public class Enemy extends Unite {
    protected enum Etat {
        VADROUILLE, FUITE
    }

    private Etat etat;
    private int secondesRestant;
    private Timer timer = new Timer();

    public Enemy(Position position, int rayon, int secondesRestant, int vitesse) {
        super(position, rayon, vitesse);
        this.etat = Etat.VADROUILLE;
        this.secondesRestant = secondesRestant;
        timer.schedule(new model.objets.Fuite(this), secondesRestant * 1000); // tempsRestant en secondes
        setVitesse(5);
    }

    public Etat getEtat() {return etat;}
    public int getTempsRestant() {return secondesRestant;}

    public void fuit() {
        this.etat = Etat.FUITE;
        timer.cancel();
        //set position de destination le coin de l’écran le plus proche
        /* 9 directions possibles, il faudra utiliser les dimentions du terrain(consantes) */

        /*version initiale ce dirige vers (0,0)*/

        this.setDestination(new Position(-1, -1));



    }
    public void vadrouille() { this.etat = Etat.VADROUILLE;}
    public void decrementerTemps(int t){secondesRestant -= t;}

    public void action(){}



}

