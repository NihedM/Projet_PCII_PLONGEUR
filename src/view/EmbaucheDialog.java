package view;

import model.gains_joueur.Referee;
import model.objets.Position;
import model.unite_controlables.Plongeur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class EmbaucheDialog extends JDialog {
        public EmbaucheDialog(JDialog parent) {
            super(parent, "Embaucher une unité", true);
            setLayout(new BorderLayout());
            setSize(300, 300);
            setLocationRelativeTo(parent);

            // Bouton "Acheter" placé en haut
            JButton acheterButton = new JButton("Acheter");
            add(acheterButton, BorderLayout.SOUTH);

            // Panneau personnalisé qui dessine un mini rond (aperçu de l'unité)
            JPanel miniUnitPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    int diameter = 50;
                    int x = (getWidth() - diameter) / 2;
                    int y = (getHeight() - diameter) / 2;
                    g.setColor(Color.BLUE); // Couleur du mini rond (modifiable)
                    g.fillOval(x, y, diameter, diameter);
                }
            };
            miniUnitPanel.setPreferredSize(new Dimension(300, 200));
            add(miniUnitPanel, BorderLayout.CENTER);

            // Action sur le bouton "Acheter"
            acheterButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Générer une position aléatoire dans la zone de jeu
                    if (Referee.getInstance().getArgentJoueur() >= 100) {
                        Random rand = new Random();
                        int margin = 25; // pour éviter de placer trop près du bord
                        int x = rand.nextInt(GamePanel.PANELDIMENSION - 2 * margin) + margin;
                        int y = rand.nextInt(GamePanel.PANELDIMENSION - 2 * margin) + margin;
                        Position pos = new Position(x, y);

                        Plongeur newUnit = new Plongeur(4, pos, 10);
                        GamePanel.getInstance().addUniteControlable(newUnit);
                        GamePanel.getInstance().repaint();

                        Referee.getInstance().retirerArgent(100);

                        JOptionPane.showMessageDialog(view.EmbaucheDialog.this, "Unité embauchée !");
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(view.EmbaucheDialog.this, "Vous n'avez pas assez d'agent");
                    }
                }
            });
        }
}

