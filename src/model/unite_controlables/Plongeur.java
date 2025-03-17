package model.unite_controlables;

import model.gains_joueur.Referee;
import model.objets.Position;
import model.objets.UniteControlable;

import javax.swing.*;
import java.util.HashMap;

public class Plongeur extends UniteControlable {
    //private int ressourceStockees; // Points de victoire
    //private int argent; // Argent gagné en vendant des colliers

    private HashMap<model.objets.Ressource, Integer> sac; // Sac pour stocker les ressources et leurs quantités

    private static final int CAPACITE_SAC = 10; // Capacité maximale du sac

    public Plongeur(int id, Position position, int rayon) {
        super(id, position, rayon, 10);
        sac = new HashMap<>();
    }

    public HashMap<model.objets.Ressource, Integer> getSac() {
        return sac;
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



}
