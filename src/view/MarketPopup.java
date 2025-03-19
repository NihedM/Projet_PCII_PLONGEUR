package view;

import model.objets.Position;
import model.unite_controlables.Plongeur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class MarketPopup extends JDialog {

    public MarketPopup(JFrame parent) {
        super(parent, "Marché", true);
        setLayout(new FlowLayout());
        setSize(300, 150);
        setLocationRelativeTo(parent);

        JButton vendreButton = new JButton("Vendre");
        JButton embaucherButton = new JButton("Embaucher");

        vendreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Logique de vente
                JOptionPane.showMessageDialog(MarketPopup.this, "Vente effectuée");
                dispose();
            }
        });

        embaucherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EmbaucheDialog embaucheDialog = new EmbaucheDialog(MarketPopup.this);
                embaucheDialog.setVisible(true);
                dispose(); // on ferme la popup du Market
            }
        });

        add(vendreButton);
        add(embaucherButton);
    }

    // Fenêtre interne pour l'embauche d'une unité
    class EmbaucheDialog extends JDialog {
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
                    g.setColor(Color.BLACK); // Couleur du mini rond (modifiable)
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
                    Random rand = new Random();
                    int margin = 25; // pour éviter de placer trop près du bord
                    int x = rand.nextInt(GamePanel.PANELDIMENSION - 2 * margin) + margin;
                    int y = rand.nextInt(GamePanel.PANELDIMENSION - 2 * margin) + margin;
                    Position pos = new Position(x, y);

                    // Créer et ajouter une nouvelle unité (ici un Plongeur)
                    Plongeur newUnit = new Plongeur(4, pos, 10);
                    GamePanel.getInstance().addUniteControlable(newUnit);
                    GamePanel.getInstance().repaint();

                    JOptionPane.showMessageDialog(EmbaucheDialog.this, "Unité embauchée !");
                    dispose();
                }
            });
        }
    }
}

