package view;

import model.unite_controlables.Plongeur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MarchePanel extends JFrame {
    private Plongeur plongeur;
    private view.GamePanel gamePanel;

    public MarchePanel(Plongeur plongeur, GamePanel gamePanel) {
        this.plongeur = plongeur;
        this.gamePanel = gamePanel;

        // Configuration de la fenêtre
        setTitle("Marché");
        setSize(300, 200); // Taille ajustée pour deux boutons
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 240, 240)); // Fond gris clair

        // Titre du marché
        JLabel titleLabel = new JLabel("Marché", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Panneau pour les boutons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 10, 10)); // 2 lignes, 1 colonne, espacement de 10
        buttonPanel.setBackground(new Color(240, 240, 240));

        // Bouton "Vendre"
        JButton vendreButton = createStyledButton("Vendre les colliers");
        vendreButton.addActionListener(new VendreButtonListener());

        // Bouton "Embaucher"
        JButton embaucherButton = createStyledButton("Embaucher (100 pièces)");
        embaucherButton.addActionListener(new EmbaucherButtonListener());

        // Ajout des boutons au panneau
        buttonPanel.add(vendreButton);
        buttonPanel.add(embaucherButton);
        add(buttonPanel, BorderLayout.CENTER);
    }

    // Méthode pour créer un bouton stylisé
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(new Color(0, 102, 204)); // Fond bleu
        button.setForeground(Color.WHITE); // Texte blanc
        button.setFocusPainted(false); // Désactiver la bordure de focus
        button.setPreferredSize(new Dimension(200, 40)); // Taille du bouton
        return button;
    }

    // Écouteur pour le bouton "Vendre"
    private class VendreButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new VendrePanel(plongeur, gamePanel).setVisible(true);
        }
    }

    // Écouteur pour le bouton "Embaucher"
    private class EmbaucherButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
           /* if (Referee.getInstance().getArgentJoueur() >= 100) {           //TODO il faut gérer le marché correctement : établir des prix aux unités controlables
                // Créer un nouveau plongeur
                Plongeur nouveauPlongeur = new Plongeur(gamePanel.getUnites().size() + 1, new Position(300, 300), 10);
                gamePanel.getUnites().add(nouveauPlongeur); // Ajouter le plongeur à la liste des unités

                // Démarrer un nouveau DeplacementThread pour le plongeur
                //new DeplacementThread(nouveauPlongeur, gamePanel, plongeur).start();          //TODO je ne comprend pas on arrete les threads de tout le monde mais on reprend un seul?

                Referee.getInstance().retirerArgent(100);// Déduire le coût
                gamePanel.repaint(); // Rafraîchir l'affichage
                JOptionPane.showMessageDialog(null, "Nouveau plongeur embauché !", "Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Pas assez d'argent pour embaucher un plongeur !", "Erreur", JOptionPane.ERROR_MESSAGE);
            }*/
        }
    }
}
