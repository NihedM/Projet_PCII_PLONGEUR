package view;

import model.objets.UniteControlable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InfoPanel extends JPanel {


    private JLabel infoLabel;
    private JButton deplacerButton;
    private JButton recupererButton;


    public InfoPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(220, 220, 220));
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        // Label pour afficher les informations de l'unité
        infoLabel = new JLabel("Informations :", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(infoLabel, BorderLayout.NORTH);

        // Panneau central pour empiler les boutons verticalement et les centrer
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Bouton "Se déplacer"
        deplacerButton = new JButton("Se déplacer (D)");
        deplacerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Recherche du GamePanel parent pour activer le mode déplacement
                view.GamePanel gamePanel = (view.GamePanel) SwingUtilities.getAncestorOfClass(view.GamePanel.class, InfoPanel.this);
                if (gamePanel != null) {
                    gamePanel.setDeplacementMode(true);
                }
            }
        });
        centerPanel.add(deplacerButton, gbc);

        // Incrémenter la ligne pour placer le second bouton en-dessous
        gbc.gridy++;

        // Bouton "Récupérer"
        recupererButton = new JButton("Récupérer (R)");
        recupererButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GamePanel.getInstance().setRecuperationMode(true);
            }
        });
        centerPanel.add(recupererButton, gbc);

        // Ajout du panneau centré dans l'InfoPanel
        add(centerPanel, BorderLayout.CENTER);
    }


}
