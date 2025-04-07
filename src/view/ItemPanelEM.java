package view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public abstract class ItemPanelEM extends JPanel {
    private String itemName;
    private int cost;
    private Image icon;

    public ItemPanelEM(String itemName, int cost, String iconPath) {
        this.itemName = itemName;
        this.cost = cost;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        setOpaque(true);

        if (iconPath != null && !iconPath.isEmpty()) {
            try {
                icon = ImageIO.read(new File(iconPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Label icône
        JLabel iconLabel = new JLabel();
        iconLabel.setAlignmentX(CENTER_ALIGNMENT);
        if (icon != null) {
            Image scaled = icon.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            iconLabel.setIcon(new ImageIcon(scaled));
        } else {
            iconLabel.setText("[Pas d'icône]");
        }
        add(Box.createVerticalStrut(10));
        add(iconLabel);

        // Label nom
        JLabel nameLabel = new JLabel(itemName);
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(Box.createVerticalStrut(5));
        add(nameLabel);

        // Label coût
        JLabel costLabel = new JLabel("Coût : " + cost + " €");
        costLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(5));
        add(costLabel);

        // Bouton acheter
        JButton buyButton = new JButton("Acheter");
        buyButton.setAlignmentX(CENTER_ALIGNMENT);
        buyButton.addActionListener(e -> onBuy());
        add(Box.createVerticalStrut(5));
        add(buyButton);

        add(Box.createVerticalGlue());
    }




    protected abstract void onBuy();
}
