package view;

import model.objets.Ressource;
import model.unite_controlables.Plongeur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VendrePanel extends JFrame {
    private model.unite_controlables.Plongeur plongeur;              // ici c'est bon , QUE le plongeur peut vendre
    private view.GamePanel gamePanel;

    public VendrePanel(Plongeur plongeur, GamePanel gamePanel) {
        this.plongeur = plongeur;
        this.gamePanel = gamePanel;

        // Configuration de la fenêtre
        setTitle("Vendre");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(240, 240, 240)); // Fond gris clair

        // Titre du panneau
        JLabel titleLabel = new JLabel("Vendre des colliers", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Panneau pour afficher les colliers
        JPanel collierPanel = new JPanel();
        collierPanel.setLayout(new GridLayout(0, 1, 10, 10));
        collierPanel.setBackground(new Color(240, 240, 240));

        // Afficher chaque collier dans le sac
        for (Ressource loot: plongeur.getSac().keySet()) {
            JButton collierButton = createStyledButton("Ressource - Valeur : " + loot.getValeur() + " - Quantité : " + plongeur.getSac().get(loot));
            collierButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    plongeur.vendre();
                    gamePanel.repaint();
                }
            });
            collierPanel.add(collierButton);
        }

        // Ajouter un message si le sac est vide
        if (plongeur.getSac().isEmpty()) {
            JLabel emptyLabel = new JLabel("Votre sac est vide !", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            collierPanel.add(emptyLabel);
        }
        // Ajouter le panneau à la fenêtre
        add(new JScrollPane(collierPanel), BorderLayout.CENTER);
    }

    // Méthode pour créer un bouton stylisé
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(new Color(0, 102, 204)); // Fond bleu
        button.setForeground(Color.WHITE); // Texte blanc
        button.setFocusPainted(false); // Désactiver la bordure de focus
        return button;
    }
}
