package view;

import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InfoPanel extends JPanel {


    private JLabel infoLabel;
    private AtributInfo atributInfo;

    private JButton deplacerButton;
    private JButton recupererButton;
    private JButton faireFuirButton;



    //Panel pour les plongeurs
    public InfoPanel() {
        setLayout(new GridLayout(3, 1));
        setBackground(new Color(220, 220, 220));
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        // Label pour afficher les informations de l'unité
        /*infoLabel = new JLabel("Informations :", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 16));*/
        //add(infoLabel, BorderLayout.NORTH);

        //
        atributInfo = new AtributInfo();
        add(atributInfo);


        // Panneau central pour empiler les boutons verticalement et les centrer
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(Color.CYAN);

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
        buttonPanel.add(deplacerButton, gbc);

        // Ajout du panneau centré dans l'InfoPanel
        add(buttonPanel, BorderLayout.CENTER);

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
        buttonPanel.add(recupererButton, gbc);

        // Ajout du panneau centré dans l'InfoPanel
        add(buttonPanel, BorderLayout.CENTER);


        gbc.gridy++;
        faireFuirButton = new JButton("Faire fuire (F)");
        faireFuirButton.addActionListener(e -> {
            GamePanel gamePanel = (GamePanel) SwingUtilities.getAncestorOfClass(GamePanel.class, InfoPanel.this);
            if (gamePanel != null) {
                for (UniteControlable unite : gamePanel.getUnitesSelected()) {
                    if (unite instanceof Plongeur plongeur) {
                        plongeur.setFaitFuire(true);
                    }
                }
            }
        });
        buttonPanel.add(faireFuirButton, gbc);


        add(buttonPanel);

        //Temporaire

        JPanel emptyPanel = new JPanel();
        //emptyPanel.setOpaque(false);
        emptyPanel.setBackground(Color.LIGHT_GRAY);
        add(emptyPanel);
    }


    public void updateInfo(UniteControlable unite) {
        atributInfo.removeAll();
        atributInfo.updateInfo(unite.getAttributes());
        atributInfo.revalidate();
        atributInfo.repaint();
    }


}
