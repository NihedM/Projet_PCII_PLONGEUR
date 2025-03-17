package model.objets;

public class Ressource extends Objet {
    public enum Etat {
        EN_CROISSANCE, PRET_A_RECOLTER, DETRUIRE
    }

    private Etat etat;
    private int valeur;
    private int tempsRestant;
    private int tempsPret;
    private int tempsInitial;
    private Position position;

    public Ressource(Position position, int rayon, int valeur, int tempsRestant) {
        super(position, rayon);
        this.valeur = valeur;
        this.tempsRestant = tempsRestant;
        this.tempsInitial = tempsRestant;
        this.tempsPret = 3;
        this.etat = Etat.EN_CROISSANCE;
        this.position = new Position((int) (Math.random() * 800), (int) (Math.random() * 600));
    }

    public Etat getEtat() {
        return etat;
    }

    public int getTempsRestant() {
        return tempsRestant;
    }

    public int getTempsInitial() {
        return tempsInitial;
    }

    public int getValeur() {
        return valeur;
    }

    public synchronized void evoluer() {
        if (etat == Etat.EN_CROISSANCE) {
            tempsRestant--;
            if (tempsRestant <= 0) {
                etat = Etat.PRET_A_RECOLTER;
            }
        } else if (etat == Etat.PRET_A_RECOLTER) {
            tempsPret--;
            if (tempsPret <= 0) {
                etat = Etat.DETRUIRE;
            }
        }
    }

    public synchronized boolean estRecoltable() {
        return etat == Etat.PRET_A_RECOLTER; // La ressource est récoltable uniquement si elle est prête
    }

    public synchronized int recolter() {
        if (estRecoltable()) {
            int gain = valeur;
            etat = Etat.DETRUIRE;
            return gain;
        }
        return 0;
    }



}
