package view;

import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class InfoPanel extends JPanel {


    private AtributInfo atributInfo;
    private JPanel buttonPanel;



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
        buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(Color.CYAN);
        add(buttonPanel, BorderLayout.CENTER);


        //Temporaire

        JPanel emptyPanel = new JPanel();
        //emptyPanel.setOpaque(false);
        emptyPanel.setBackground(Color.LIGHT_GRAY);
        add(emptyPanel);
    }


    public void updateInfo(UniteControlable unite) {
        atributInfo.removeAll();
        atributInfo.updateInfo(unite.getAttributes());

        if (unite instanceof Plongeur plongeur) {
            List<ButtonAction> actions = plongeur.getButtonActions();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.insets = new Insets(10, 10, 10, 10);

            for (ButtonAction action : actions) {
                JButton button = new JButton(action.getLabel());
                button.addActionListener(action.getAction());
                buttonPanel.add(button, gbc);
                gbc.gridy++;
            }
        }

        buttonPanel.revalidate();
        buttonPanel.repaint();
    }



}
