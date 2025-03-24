package model.unite_controlables;

import controler.FuiteHandler;
import model.gains_joueur.Referee;
import model.objets.Position;
import model.objets.UniteControlable;
import model.unite_non_controlables.Calamar;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class Plongeur extends UniteControlable {
    private HashMap<model.objets.Ressource, Integer> sac; // Sac pour stocker les ressources et leurs quantités
    private static final int CAPACITE_SAC = 10, MAX_HP  = 100, COOLDOWN_FUITE = 30, MAX_OXYGEN = 100, MAX_STAMINA = 100; // Capacité maximale du sac, vie maximale du plongeur, 30 secondes de cooldown pour la fuite
    private int rayonFuite, oxygen, stamina; // Rayon de fuite du plongeur, niveau d'oxygène
    private boolean faitFuire;

    public Plongeur(int id, Position position, int rayon) {
        super(id, position, rayon, 10, MAX_HP);
        sac = new HashMap<>();
        this.rayonFuite = 50;
        this.faitFuire = false;
        this.oxygen = MAX_OXYGEN;
        this.stamina = MAX_STAMINA;
    }

    public HashMap<model.objets.Ressource, Integer> getSac() {
        return sac;
    }

    public int getRayonFuite() {
        return rayonFuite;
    }

    public boolean isFaitFuire() {
        return faitFuire;
    }
    public int getCurrentOxygen() {
        return oxygen;
    }

    public int getCurrentStamina() {
        return stamina;
    }

    @Override
    public String getInfo() {
        return super.getInfo() + ", Oxygen: " + getCurrentOxygen() + getCurrentStamina();
    }

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attributes = super.getAttributes();
        attributes.put("Oxygen", String.valueOf(getCurrentOxygen()));
        attributes.put("Stamina ", String.valueOf(getCurrentStamina()));
        return attributes;
    }


    //-------------------Méthodes-------------------


    public void setCurrentOxygen(int oxygen) {
        this.oxygen = oxygen;
    }

    public void setCurrentStamina(int stamina) {
        this.stamina = stamina;
    }

    public void setFaitFuire(boolean faitFuire) {
        this.faitFuire = faitFuire;
        if (faitFuire) {
            FuiteHandler.getInstance().addPlongeur(this);
        } else {
            FuiteHandler.getInstance().removePlongeur(this);
        }
    }


    // Méthode pour ajouter un collier au sac
    public boolean ajouterAuxSac(model.objets.Ressource ressource) {

        /*faire la somme des values du sac*/
        int quantite = 0;
        for (int value : sac.values()) {
            quantite += value;
        }
        /*ajouter une ressource au sac si le sac n'est pas plein, */


        if (quantite< CAPACITE_SAC) {
            sac.put(ressource, sac.get(ressource)+ 1);
            System.out.println("Ressource ramassé ! Points de victoire: " + Referee.getInstance().getPointsVictoire());
            return true; // Le collier a été ajouté avec succès
        } else {
            System.out.println("Sac plein ! Impossible de ramasser un autre collier.");
            JOptionPane.showMessageDialog(null, "Sac plein ! Impossible de ramasser un autre collier.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return false; // Le sac est plein, le collier n'a pas été ajouté
        }

    }




    // Méthode recolter, renvoie vrai si la ressource est récoltée, faux sinons
    public boolean recolter(model.objets.Ressource ressource) {
        /*if (Collision.collisionCC(this, ressource) > -1 && ressource.estRecoltable()) { // Vérifier si la ressource est prête
                return ajouterAuxSac(ressource); // Ajouter le collier au sac du plongeur
        }*/
        return false; // La ressource n'a pas été récoltée
    }



    // Méthode pour vendre les colliers
    public void vendre() {
        if (!sac.isEmpty()) {
            int gain = 0;
            for (model.objets.Ressource key : sac.keySet()) {
                gain += key.getValeur() * sac.get(key);
            }

            Referee.getInstance().ajouterArgent(gain);

            sac.clear(); // Vider le sac après la vente
            System.out.println("Colliers vendus ! Argent gagné: " + gain);
        } else {
            System.out.println("Aucun collier à vendre.");
        }
    }

    public void faireFuirCalamar(Calamar calamar) {
        calamar.fuit();
    }



}
