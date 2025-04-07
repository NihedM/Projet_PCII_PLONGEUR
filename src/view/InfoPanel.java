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



    public InfoPanel() {

        setLayout(new GridLayout(3, 1));
        setBackground(new Color(220, 220, 220));
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        // Panneau central pour empiler les boutons verticalement et les centrer
        buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(Color.CYAN);
        add(buttonPanel, BorderLayout.CENTER);


        JPanel emptyPanel = new JPanel();
        emptyPanel.setBackground(Color.LIGHT_GRAY);
        add(emptyPanel);
    }


    public void updateInfo(UniteControlable unite) {
        removeAll();
        setLayout(new BorderLayout());

        atributInfo = new AtributInfo(unite);
        add(atributInfo, BorderLayout.NORTH);
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

    public void updateMultipleInfo(List<UniteControlable> units) {
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel imagesPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        imagesPanel.setBackground(new Color(220, 220, 220));
        imagesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        int imageWidth = 50;
        int imageHeight = 50;
        for (UniteControlable unit : units) {
            JLabel imageLabel;
            ImageIcon icon = null;
            if (unit instanceof model.unite_controlables.PlongeurArme) {
                icon = new ImageIcon(getClass().getResource("/view/images/plongeurArme.png"));
            } else if (unit instanceof model.unite_controlables.Plongeur) {
                icon = new ImageIcon(getClass().getResource("/view/images/plongeurNormal.png"));
            }
            if (icon != null) {
                Image scaledImage = icon.getImage().getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaledImage);
                imageLabel = new JLabel(icon);
            } else {
                imageLabel = new JLabel("Unité " + unit.getId());
            }
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            imagesPanel.add(imageLabel);
        }
        add(imagesPanel);

        // Panneau des actions
        JPanel actionsPanel = new JPanel(new GridBagLayout());
        actionsPanel.setBackground(Color.CYAN);
        actionsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (!units.isEmpty()) {
            UniteControlable firstUnit = units.get(0);
            if (firstUnit instanceof model.unite_controlables.Plongeur) {
                List<ButtonAction> actions = ((model.unite_controlables.Plongeur) firstUnit).getButtonActions();
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.CENTER;
                gbc.insets = new Insets(10, 10, 10, 10);
                for (ButtonAction action : actions) {
                    JButton button = new JButton(action.getLabel());
                    button.addActionListener(action.getAction());
                    actionsPanel.add(button, gbc);
                    gbc.gridy++;
                }
            }
        }
        add(actionsPanel);

        revalidate();
        repaint();
    }




}
