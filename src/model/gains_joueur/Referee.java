package model.gains_joueur;

import controler.VictoryManager;

public class Referee {
    private int argentJoueur;
    private int pointsVictoire;

    // Instance unique de la classe
    private static model.gains_joueur.Referee instance;
    public Referee(){
        this.argentJoueur = 0;
        this.pointsVictoire = 0;
    }

    public static model.gains_joueur.Referee getInstance() {
        if (instance == null) {
            instance = new model.gains_joueur.Referee();
        }
        return instance;
    }

    public int getArgentJoueur() {
        return argentJoueur;
    }

    public int getPointsVictoire() {
        return pointsVictoire;
    }


    public void ajouterArgent(int argent){
        this.argentJoueur += argent;
    }

    public void retirerArgent(int argent){
        this.argentJoueur -= argent;
    }

    public void ajouterPointsVictoire(int points){
        this.pointsVictoire += points;
    }

    public void reset() {
        this.argentJoueur = 0;
        this.pointsVictoire = 0;
    }
}

