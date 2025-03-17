package model.unite_non_controlables;

import model.objets.Objet;
import model.objets.Position;
import model.objets.Unite;
import model.objets.UniteNonControlableInterface;

import java.util.ArrayList;
import java.util.Timer;

public class Enemy extends Unite implements UniteNonControlableInterface {
    protected enum Etat {
        VADROUILLE, FUITE
    }

    private Etat etat;
    private int secondesRestant;
    private Timer timer = new Timer();

    public Enemy(Position position, int rayon, int secondesRestant, double vitesse) {
        super(position, rayon, vitesse);
        this.etat = Etat.VADROUILLE;
        this.secondesRestant = secondesRestant;
        timer.schedule(new model.objets.Fuite(this), secondesRestant * 1000); // tempsRestant en secondes
        setVitesse(5);
    }

    public Etat getEtat() {return etat;}
    public int getTempsRestant() {return secondesRestant;}

    public void fuit() {        //version default:  pas oublier de faire override sinon!!!
        this.etat = Etat.FUITE;
        this.setDestination(new Position(-1, -1));
    }
    public void vadrouille() { this.etat = Etat.VADROUILLE;}
    public void fuire(){this.etat= Etat.FUITE;}


    public void decrementerTemps(int t){secondesRestant -= t;}
    public void stopTimer(){timer.cancel();}



    @Override
    public void setup(ArrayList<Objet> interactionTargets) {

    }
    public void action(){}



}

