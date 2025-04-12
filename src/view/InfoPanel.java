package view;

import java.util.ArrayList;
import java.util.HashMap;
import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import model.unite_non_controlables.Enemy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

public class InfoPanel extends JPanel {


    private volatile AtributInfo atributInfo;
    private volatile BackgroundPanel buttonPanel;
    private volatile BackgroundPanel emptyPanel;




    public InfoPanel() {

        setLayout(new GridLayout(3, 1));
        //setBackground(new Color(220, 220, 220));
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        // Panneau central pour empiler les boutons verticalement et les centrer
        buttonPanel = new BackgroundPanel();
        buttonPanel.setLayout(new GridBagLayout());
        //buttonPanel.setBackground(Color.CYAN);
        buttonPanel.setOpaque(false); // Make it transparent
        add(buttonPanel, BorderLayout.CENTER);

        emptyPanel = new BackgroundPanel();
        //emptyPanel.setBackground(Color.LIGHT_GRAY);
        emptyPanel.setOpaque(false); // Make it transparent
        add(emptyPanel);


    }

    public synchronized AtributInfo getAtributInfo() {
        return atributInfo;
    }


    public synchronized void updateInfo(UniteControlable unite) {
        removeAll();

        setLayout(new GridLayout(3, 1));
        //setBackground(new Color(220, 220, 220));
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));



        atributInfo = new AtributInfo(unite);
        add(atributInfo, BorderLayout.NORTH);
        atributInfo.updateInfo(unite.getAttributes());

        buttonPanel.setBackgroundImage("actionsBackground.png"); // Set the background image
        buttonPanel.removeAll();
        List<ButtonAction> actions = unite.getButtonActions();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 10, 10);

        for (ButtonAction action : actions) {
            JButton button = new JButton(action.getLabel());
            button.setFont(GamePanel.CUSTOM_FONT.deriveFont(16f));
            button.addActionListener(action.getAction());
            buttonPanel.add(button, gbc);
            gbc.gridy++;
        }

        add(buttonPanel, BorderLayout.CENTER);


        //emptyPanel.setBackground(Color.LIGHT_GRAY);
        emptyPanel.setBackgroundImage("unitIconBackground.png");
        emptyPanel.removeAll();
        if (unite.getUnitIcon() != null) {
            ImageIcon originalIcon = unite.getUnitIcon();
            Image scaledImage = originalIcon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setVerticalAlignment(SwingConstants.CENTER);
            emptyPanel.add(iconLabel);
        }
        add(emptyPanel);


        revalidate();
        repaint();
    }


    public void updateEnemyInfo(Enemy enemy) {
        removeAll();
        atributInfo = new AtributInfo(enemy);
        add(atributInfo);
        atributInfo.updateInfo(enemy.getAttributes());

        buttonPanel.removeAll(); // No action buttons for enemies
        buttonPanel.setBackgroundImage("unitIconBackground.png");

        add(buttonPanel, BorderLayout.CENTER);
        emptyPanel.setBackgroundImage("unitIconBackground.png");
        emptyPanel.removeAll();
        if (enemy.getUnitIcon() != null) {
            ImageIcon originalIcon = enemy.getUnitIcon();
            Image scaledImage = originalIcon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setVerticalAlignment(SwingConstants.CENTER);
            emptyPanel.add(iconLabel);
        }
        add(emptyPanel);




        buttonPanel.revalidate();
        buttonPanel.repaint();
        revalidate();
        repaint();
    }

    public void updateMultipleInfo(List<UniteControlable> units) {
        removeAll(); // Clear the panel
        setLayout(new BorderLayout()); // Set layout for two sections
        setBackground(new Color(220, 220, 220));

        // Top Section: Grid of 10 cubes for each unit
        JPanel topPanel = new JPanel(new GridLayout(5, 2, 10, 10)); // 5 rows, 2 columns
        topPanel.setBackground(Color.BLACK);



        Map<Class<? extends UniteControlable>, ImageIcon> scaledIcons = new HashMap<>();
        for (UniteControlable unit : units) {
            if (!scaledIcons.containsKey(unit.getClass())) {
                ImageIcon unitIcon = unit.getUnitIcon();
                if (unitIcon != null) {
                    Image scaledImage = unitIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    scaledIcons.put(unit.getClass(), new ImageIcon(scaledImage));
                }
            }
        }

        ImageIcon emptyIcon = new ImageIcon(GamePanel.getCachedImage("emptyUnitSlotIcon.png"));
        ImageIcon scaledEmptyIcon = null;
        if (emptyIcon != null) {
            Image scaledImage = emptyIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            scaledEmptyIcon = new ImageIcon(scaledImage);
        }

        List<JLabel> cubes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            JLabel cube = new JLabel();
            cube.setPreferredSize(new Dimension(50, 50)); // Set cube size
            cube.setHorizontalAlignment(SwingConstants.CENTER);
            cube.setVerticalAlignment(SwingConstants.CENTER);

            if (i < units.size() && units.get(i).isSelected()) {
                Class<? extends UniteControlable> unitClass = units.get(i).getClass();
                ImageIcon scaledIcon = scaledIcons.get(unitClass);
                if (scaledIcon != null) {
                    cube.setIcon(scaledIcon);
                } else {
                    cube.setBackground(Color.GREEN); // Fallback if no icon is available
                    cube.setOpaque(true);
                }
            } else {
                cube.setIcon(scaledEmptyIcon);
            }
            cubes.add(cube);
        }
        topPanel.removeAll(); // Clear the panel before adding new components
        for (JLabel cube : cubes) {
            topPanel.add(cube);
        }

        // Repaint the panel after all components are added
        topPanel.revalidate();
        topPanel.repaint();

        add(topPanel, BorderLayout.CENTER); // Add top section

        BackgroundPanel bottomPanel = new BackgroundPanel();
        bottomPanel.setBackground(Color.BLACK);

        List<ButtonAction> commonActions = UniteControlable.getCommonActions(units);
        int rows = (int) Math.ceil(commonActions.size() / 2.0);
        bottomPanel.setLayout(new GridLayout(rows, 2, 10, 10));



        for (ButtonAction action : commonActions) {
            JButton button = new JButton(action.getLabel());
            button.setFont(GamePanel.CUSTOM_FONT.deriveFont(16f));
            button.addActionListener(action.getAction());
            bottomPanel.add(button);
        }

        add(bottomPanel, BorderLayout.SOUTH); // Add bottom section

        revalidate(); // Refresh the panel
        repaint();

    }



}
