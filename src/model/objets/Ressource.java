package model.objets;

import controler.TileManager;

public class Ressource extends Objet {
    public enum Etat {
        EN_CROISSANCE, PRET_A_RECOLTER, DETRUIRE
    }

    private static int compteur = 0; // Compteur statique pour générer des IDs uniques
    private int id; // Identifiant unique de la ressource
    private String nom; // Nom de la ressource avec l'ID

    private Etat etat;
    private int valeur;
    private int tempsRestant;
    private int tempsPret;
    private int tempsInitial;
    private Position position;

    private boolean targeted = false;
    private boolean fixed = false;


    public Ressource(Position position, int rayon, int valeur, int tempsRestant, String nom) {
        super(position, rayon);
        this.valeur = valeur;
        this.tempsRestant = tempsRestant;
        this.tempsInitial = tempsRestant;
        this.tempsPret = 3;
        this.etat = Etat.EN_CROISSANCE;
        this.position = new Position((int) (Math.random() * 800), (int) (Math.random() * 600));
        this.id = ++compteur; // Incrémenter le compteur et attribuer l'ID
        this.nom = nom + id; // Générer le nom unique

        setScalingFactor(1.0);
    }

    public String getNom() {
        return nom;
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
                etat = Etat.PRET_A_RECOLTER; // La ressource reste dans cet état indéfiniment
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

    public boolean isTargeted() {
        return targeted;
    }

    public void setTargeted(boolean targeted) {
        this.targeted = targeted;
        setFixed(targeted);
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }
    @Override
    public synchronized void updatePosition() {
        if (!fixed) {
            this.coordGrid = TileManager.transformePos_to_Coord(this.getPosition());
        }
    }

}