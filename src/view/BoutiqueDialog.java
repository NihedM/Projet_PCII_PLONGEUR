package view;

import model.gains_joueur.Referee;
import model.objets.Position;
import model.unite_controlables.SousMarin;

import javax.swing.*;
import java.awt.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


public class BoutiqueDialog extends JDialog {

    private List<ItemPanel> itemPanels = new ArrayList<>();
    public BoutiqueDialog(JFrame parent) {

        super(parent, "Boutique", true);
        setSize(900, 600);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        JLabel titleLabel = new JLabel("Boutique - Sélectionnez un item", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panneau contenant la grille d'items
        JPanel itemsGrid = new JPanel(new GridLayout(2, 3, 10, 10));
        itemsGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(itemsGrid, BorderLayout.CENTER);

        // Création des items

        // Sous-marin (verrouillé si la profondeur 3 n'est pas atteinte)
        ItemPanel sousMarinPanel = new ItemPanel(
                "Sous-marin",
                "400€",
                isDepth3Unlocked(),
                "src/view/images/sous-marin.png"
        );
        sousMarinPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!sousMarinPanel.isUnlocked()) {
                    JOptionPane.showMessageDialog(BoutiqueDialog.this, "Cet item est verrouillé !");
                    return;
                }
                if (Referee.getInstance().getArgentJoueur() < 400) {
                    JOptionPane.showMessageDialog(BoutiqueDialog.this, "Pas assez d'argent pour acheter un sous-marin");
                } else {
                    // Créer un sous-marin à une position x,y
                    Position pos = new Position(400, 150); // Exemple de position, à adapter
                    SousMarin sousMarin = new SousMarin(pos);
                    // Ajout du sous-marin à la base
                    GamePanel.getInstance().addUniteControlable(sousMarin);

                    Referee.getInstance().retirerArgent(400);
                    Referee.getInstance().ajouterPointsVictoire(30); // 50 points de victoire
                    JOptionPane.showMessageDialog(BoutiqueDialog.this, "Sous-marin acheté et ajouté à la base !");
                    dispose();
                }
            }
        });
        itemsGrid.add(sousMarinPanel);

        // Essence
        ItemPanel essencePanel = new ItemPanel(
                "Essence",
                "25€",
                isDepth3Unlocked(),
                "src/view/images/oxygen.png"
        );
        itemsGrid.add(essencePanel);
        itemPanels.add(essencePanel);

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
        return true;
    }
}
