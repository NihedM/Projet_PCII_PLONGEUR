package view;

import model.gains_joueur.Referee;
import model.objets.Position;
import model.objets.Terrain;
import model.unite_controlables.Plongeur;
import model.unite_controlables.PlongeurArme;
import model.objets.UniteControlable;
import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class EmbaucheDialog extends JDialog {

    private Random rand = new Random();
    public EmbaucheDialog(JDialog parent) {
        super(parent, "Embaucher une unité", true);
        setSize(600, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Choisissez votre unité", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        JPanel itemsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        itemsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(itemsPanel, BorderLayout.CENTER);

        // Item 1 : Plongeur
        ItemPanelEM plongeurPanel = new ItemPanelEM(
                "Plongeur",
                50,  // coût
                "src/view/images/plongeur.png"
        ) {
            @Override
            protected void onBuy() {   //
                if (!checkMoney(50) )return;
                // Générer une position aléatoire
                Position pos = generateRandomPosition();
                // Créer et ajouter un Plongeur
                Plongeur newUnit = new Plongeur(3, pos);
                addUnitToGame(newUnit, 50, 10);
            }
        };
        itemsPanel.add(plongeurPanel);

        // Item 2 : Plongeur Armé
        ItemPanelEM plongeurArmePanel = new ItemPanelEM(
                "Plongeur Armé",
                100,  // coût
                "src/view/images/plongeurArme.png"
        ) {
            @Override
            protected void onBuy() {
                if (!checkMoney(100)) return;
                // Générer une position aléatoire
                Position pos = generateRandomPosition();
                // Créer et ajouter un PlongeurArme
                PlongeurArme newUnit = new PlongeurArme(3, pos);
                addUnitToGame(newUnit, 100, 10); // 30 points de victoire
            }
        };
        itemsPanel.add(plongeurArmePanel);

        // Bouton de fermeture en bas
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Fermer");
        closeBtn.addActionListener(e -> dispose());
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }


    private boolean checkMoney(int cost) {
        if (Referee.getInstance().getArgentJoueur() < cost) {
            JOptionPane.showMessageDialog(this, "Pas assez d'argent pour embaucher !");
            return false;
        }
        return true;
    }


    private void addUnitToGame(UniteControlable unit, int cost, int points) {
        GamePanel.getInstance().addUniteControlable(unit);
        Referee.getInstance().retirerArgent(cost);
        Referee.getInstance().ajouterPointsVictoire(points);
        GamePanel.getInstance().repaint();
        JOptionPane.showMessageDialog(this, unit.getClass().getSimpleName() + " embauché !");
    }


    private Position generateRandomPosition() {
        Terrain terrain = GamePanel.getInstance().getTerrain();
        int width = terrain.getWidth();
        int height = terrain.getHeight();
        int x, y;
        do {
            x = rand.nextInt(width);
            y = rand.nextInt(height);
        } while (terrain.getDepthAt(x, y) != 1);
        return new Position(x, y);
    }

}


