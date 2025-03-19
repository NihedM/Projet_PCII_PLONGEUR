package view;

import model.objets.Position;
import model.unite_controlables.Plongeur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class EmbaucheDialog extends JDialog {
    // Dans MarketPopup.java, classe EmbaucheDialog
    public EmbaucheDialog(JDialog parent) {
        super(parent, "Embaucher une unité", true);
        setLayout(new BorderLayout());
        setSize(300, 300);
        setLocationRelativeTo(parent);

        // Bouton "Acheter" placé en haut
        JButton acheterButton = new JButton("Acheter");
        add(acheterButton, BorderLayout.NORTH);

        // Panneau personnalisé qui dessine un mini rond (aperçu de l'unité) et le coût en dessous
        JPanel miniUnitPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int diameter = 50;
                int x = (getWidth() - diameter) / 2;
                int y = (getHeight() - diameter) / 2 - 10; // décalage pour laisser la place au texte en dessous
                g.setColor(Color.BLUE);
                g.fillOval(x, y, diameter, diameter);

                // Afficher le coût sous le cercle
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                String costStr = "Prix : 30";
                int strWidth = g.getFontMetrics().stringWidth(costStr);
                g.drawString(costStr, (getWidth() - strWidth) / 2, y + diameter + 20);
            }
        };
        miniUnitPanel.setPreferredSize(new Dimension(300, 200));
        add(miniUnitPanel, BorderLayout.CENTER);

        acheterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Vérifier si le joueur a assez d'argent (coût = 30)
                if (model.gains_joueur.Referee.getInstance().getArgentJoueur() < 30) {
                    JOptionPane.showMessageDialog(EmbaucheDialog.this, "Pas assez d'argent pour embaucher");
                    return;
                }

                // Générer une position aléatoire dans la zone de jeu
                Random rand = new Random();
                int margin = 25;
                int x = rand.nextInt(GamePanel.PANELDIMENSION - 2 * margin) + margin;
                int y = rand.nextInt(GamePanel.PANELDIMENSION - 2 * margin) + margin;
                model.objets.Position pos = new model.objets.Position(x, y);

                // Créer et ajouter une nouvelle unité (ici un Plongeur)
                model.unite_controlables.Plongeur newUnit = new model.unite_controlables.Plongeur(3,pos,10);
                GamePanel.getInstance().addUniteControlable(newUnit);
                GamePanel.getInstance().repaint();

                // Déduire le coût d'embauche
                model.gains_joueur.Referee.getInstance().retirerArgent(30);

                // Afficher un message de confirmation sans fermer la fenêtre
                JOptionPane.showMessageDialog(EmbaucheDialog.this, "Unité embauchée !");
            }
        });
    }

}