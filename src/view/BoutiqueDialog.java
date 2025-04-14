package view;

import model.gains_joueur.Referee;
import model.objets.Position;
import model.objets.UniteControlable;
import model.unite_controlables.SousMarin;

import javax.swing.*;
import java.awt.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


public class BoutiqueDialog extends JDialog {

    public BoutiqueDialog(JFrame parent) {

        super(parent, "Boutique", true);
        setSize(500, 300);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        JLabel titleLabel = new JLabel("Boutique - Sélectionnez un item", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panneau contenant la grille d'items
        JPanel itemsGrid = new JPanel(new GridLayout(1, 3, 20, 10)); // 1 ligne, 3 colonnes, avec un peu d'espace
        itemsGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(itemsGrid, BorderLayout.CENTER);

        // Création des items

        // Sous-marin (verrouillé si la profondeur 3 n'est pas atteinte)
        ItemPanelEM sousMarinPanel = new ItemPanelEM("Sous-marin", 400, "src/view/images/sous-marin.png") {
            @Override
            protected void onBuy() {
                if (!isDepth3Unlocked()) {
                    JOptionPane.showMessageDialog(null, "Cet item est verrouillé !");
                    return;
                }

                int cost = 400;
                if (Referee.getInstance().getArgentJoueur() < cost) {
                    JOptionPane.showMessageDialog(null, "Pas assez d'argent pour acheter un sous-marin.");
                    return;
                }

                // Créer un sous-marin à une position x,y
                Position pos = new Position(400, 150);
                SousMarin sousMarin = new SousMarin(pos);

                // Ajout du sous-marin dans le jeu
                GamePanel.getInstance().addUniteControlable(sousMarin);

                Referee.getInstance().retirerArgent(cost);
                Referee.getInstance().ajouterPointsVictoire(30);

                JOptionPane.showMessageDialog(null, "Sous-marin acheté et ajouté à la base !");
            }
        };
        itemsGrid.add(sousMarinPanel);


// Essence
        ItemPanelEM essencePanel = new ItemPanelEM("Essence", 25, "src/view/images/essence.png") {
            @Override
            protected void onBuy() {
                if (!isDepth3Unlocked()) {
                    JOptionPane.showMessageDialog(null, "Cet item est verrouillé !");
                    return;
                }
                int cost = 25;

                if (Referee.getInstance().getArgentJoueur() >= cost) {
                    Referee.getInstance().retirerArgent(cost);

                    // Recharge le carburant pour tous les sous-marins
                    for (UniteControlable uc : GamePanel.getInstance().getUnitesEnJeu()) {
                        if (uc instanceof SousMarin) {
                            ((SousMarin) uc).rechargeFuel();
                        }
                    }

                    JOptionPane.showMessageDialog(null, "Essence rechargée pour tous les sous-marins !");
                } else {
                    JOptionPane.showMessageDialog(null, "Fonds insuffisants pour acheter de l'essence.");
                }
            }
        };
        itemsGrid.add(essencePanel);



        ItemPanelEM oxygenePanel = new ItemPanelEM("Oxygène", 50, "src/view/images/oxygen.png") {
            @Override
            protected void onBuy() {
                int cost = 50;
                // Vérifier si le joueur possède suffisamment d'argent
                if (Referee.getInstance().getArgentJoueur() >= cost) {
                    // Déduire le coût
                    model.gains_joueur.Referee.getInstance().retirerArgent(cost);
                    // Recharger l'oxygène à son maximum pour tous les joueurs
                    GamePanel.getInstance().refillOxygenForAll();
                    JOptionPane.showMessageDialog(null, "Oxygène rechargé pour tous les joueurs !");
                } else {
                    JOptionPane.showMessageDialog(null, "Fonds insuffisants pour acheter de l'oxygène.");
                }
            }
        };
        itemsGrid.add(oxygenePanel);



        //  ajoute d'autant d’items ???

        // Bouton d’annulation en bas
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Fermer");
        cancelButton.addActionListener(e -> dispose());
        bottomPanel.add(cancelButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private boolean isDepth3Unlocked() {
        GamePanel gamePanel = GamePanel.getInstance();
        if (gamePanel == null || gamePanel.getTerrain() == null) {
            return false;
        }
        for (model.objets.UniteControlable uc : gamePanel.getUnitesEnJeu()) {
            int depth = gamePanel.getTerrain().getDepthAt(uc.getPosition().getX(), uc.getPosition().getY());
            if (depth >= 3) {
                return true;
            }
        }
        return false;
    }
}
