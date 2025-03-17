package view;

import model.objets.Ressource;
import model.objets.Unite;
import model.unite_controlables.Plongeur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ButtonPanel extends JPanel {
    private view.GamePanel gamePanel;
    private ArrayList<Unite> unitesSelectionnees;
    private Ressource ressourceSelectionnee; // Référence à la ressource sélectionnée
    private Runnable onPause; // Callback pour la pause
    private Runnable onResume; // Callback pour la reprise

    public ButtonPanel(GamePanel gamePanel, ArrayList<Unite> unitesSelectionnees) {
        this.gamePanel = gamePanel;
        this.unitesSelectionnees = unitesSelectionnees;

        // Configuration du layout
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(240, 240, 240)); // Fond gris clair
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Marge intérieure

        // Titre du panneau
        JLabel titleLabel = new JLabel("Actions");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(titleLabel);

        // Espacement
        add(Box.createRigidArea(new Dimension(0, 10)));

        // Création des boutons
        JButton moveButton = createStyledButton("Déplacer");
        JButton harvestButton = createStyledButton("Récolter");
        JButton marketButton = createStyledButton("Marché (M)");

        // Ajout des écouteurs d'événements
        moveButton.addActionListener(new MoveButtonListener());
        harvestButton.addActionListener(new HarvestButtonListener());
        marketButton.addActionListener(new MarketButtonListener());

        // Ajout des boutons au panneau
        add(moveButton);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(harvestButton);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(marketButton);
    }

    // Méthode pour définir les callbacks de pause/reprise
    public void setPauseListener(Runnable onPause, Runnable onResume) {
        this.onPause = onPause;
        this.onResume = onResume;
    }


    // Méthode pour créer un bouton stylisé
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(new Color(0, 102, 204)); // Fond bleu
        button.setForeground(Color.WHITE); // Texte blanc
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(150, 30)); // Taille fixe
        button.setFocusPainted(false); // Désactiver la bordure de focus
        return button;
    }

    // Méthode pour définir la ressource sélectionnée
    public void setRessourceSelectionnee(Ressource ressource) {
        this.ressourceSelectionnee = ressource;
        repaint(); // Rafraîchir l'affichage
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dessiner la barre de croissance de la ressource sélectionnée
        if (ressourceSelectionnee != null) {
            dessinerBarreCroissance(g, ressourceSelectionnee);
        }
    }

    // Méthode pour dessiner la barre de croissance
    private void dessinerBarreCroissance(Graphics g, Ressource ressource) {
        int largeurBarre;
        int tempsInitial = ressource.getTempsInitial(); // Temps initial de croissance

        if (ressource.getEtat() == Ressource.Etat.EN_CROISSANCE) {
            // Calcul de la largeur en fonction du temps écoulé
            largeurBarre = (int) (100 * ((tempsInitial - ressource.getTempsRestant()) / (double) tempsInitial));
        } else if (ressource.getEtat() == Ressource.Etat.PRET_A_RECOLTER) {
            largeurBarre = 100; // Barre pleine
        } else {
            largeurBarre = 100; // Barre pleine (état DETRUIRE)
        }

        // Position de la barre (en bas du panneau)
        int x = 10; // 10 pixels depuis la gauche
        int y = getHeight() - 50; // 50 pixels depuis le bas

        // Dessiner le contour de la barre
        g.setColor(Color.BLACK);
        g.drawRect(x, y, 100, 30);

        // Choisir la couleur en fonction de l'état
        if (ressource.getEtat() == Ressource.Etat.EN_CROISSANCE) {
            g.setColor(Color.YELLOW);
        } else if (ressource.getEtat() == Ressource.Etat.PRET_A_RECOLTER) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.RED);
        }

        // Remplir la barre
        g.fillRect(x, y, largeurBarre, 30);
        repaint();
    }

    // Écouteur pour le bouton "Déplacer"
    private class MoveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!unitesSelectionnees.isEmpty()) {
                System.out.println("Déplacer l'unité sélectionnée");
            }
        }
    }










    //TODO  faut refaire, trop inconvenant pour l'utilisateur


    // Écouteur pour le bouton "Récolter"
    private class HarvestButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            /*if (!unitesSelectionnees.isEmpty()) {
                ArrayList<Ressource> ressourcesASupprimer = new ArrayList<>();
                for (Unite unite : unitesSelectionnees) {
                    for (Ressource ressource : gamePanel.getRessources()) {
                        if (Collision.collisionCC(unite,ressource) > -1 && ressource.estRecoltable() && unite instanceof Plongeur) {
                            boolean recolteReussie = ((Plongeur)unite).recolter(ressource);
                            if (recolteReussie) {
                                ressourcesASupprimer.add(ressource);
                            }
                        }
                    }
                }
                gamePanel.getRessources().removeAll(ressourcesASupprimer);
                gamePanel.repaint();
            }*/
        }
    }




    // TODO faut gérre pour l'unité selectionnée (mettre des securites)


    // Écouteur pour le bouton "Marché"
    private class MarketButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (onPause != null) {
                onPause.run(); // Mettre le jeu en pause
            }
            view.MarchePanel marchePanel = new MarchePanel(  (Plongeur) unitesSelectionnees.get(0), gamePanel);
            marchePanel.setVisible(true);
            marchePanel.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    if (onResume != null) {
                        onResume.run(); // Reprendre le jeu
                    }
                }
            });
        }
    }


}
