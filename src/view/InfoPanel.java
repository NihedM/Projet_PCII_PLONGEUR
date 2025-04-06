package view;

import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import model.unite_non_controlables.Enemy;

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
  /*      atributInfo = new AtributInfo();
        add(atributInfo);

*/
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
        removeAll();
        atributInfo = new AtributInfo(unite);
        add(atributInfo);
        atributInfo.updateInfo(unite.getAttributes());

        buttonPanel.removeAll();
        List<ButtonAction> actions = unite.getButtonActions();
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

        add(buttonPanel, BorderLayout.CENTER);

        buttonPanel.revalidate();
        buttonPanel.repaint();
        revalidate();
        repaint();
    }
    public void updateEnemyInfo(Enemy enemy) {
        removeAll();
        atributInfo = new AtributInfo(enemy);
        add(atributInfo);
        atributInfo.updateInfo(enemy.getAttributes());

        buttonPanel.removeAll(); // No action buttons for enemies

        add(buttonPanel, BorderLayout.CENTER);

        buttonPanel.revalidate();
        buttonPanel.repaint();
        revalidate();
        repaint();
    }



}
