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

        // Création d'un panneau pour les boutons d'action
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        deplacerButton = new JButton("Se déplacer");
        recupererButton = new JButton("Récupérer");

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
        recupererButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GamePanel.getInstance().setRecuperationMode(true);
                //JOptionPane.showMessageDialog(GamePanel.getInstance(), "Cliquez sur la ressource à récupérer");
            }
        });

        buttonPanel.add(deplacerButton);
        buttonPanel.add(recupererButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }


    public void updateInfo(UniteControlable unit) {
        String infoText = "<html>ID: " + unit.getId() + "<br>" +
                "Position: (" + unit.getPosition().getX() + ", " + unit.getPosition().getY() + ")<br>" +
                "Vitesse: " + unit.getVitesse() + "</html>";
        infoLabel.setText(infoText);
    }
}
