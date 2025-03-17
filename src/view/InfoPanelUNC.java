package view;

import javax.swing.*;
import java.awt.*;

public class InfoPanelUNC extends JPanel {

    private JLabel infoLabel;

    public InfoPanelUNC(){
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 100));
        setBackground(new Color(220, 220, 220));

        infoLabel = new JLabel("Informations :", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(infoLabel, BorderLayout.CENTER);

        setVisible(false);
    }

    // TODO : AJOUTER BARRE DE PROGRESSION POUR LES RESSOURCES PAR MALO
}

