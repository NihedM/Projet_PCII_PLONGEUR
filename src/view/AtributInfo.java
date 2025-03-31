package view;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class AtributInfo extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(173, 216, 230); // Bleu clair comme GamePanel
    private static final Color TEXT_COLOR = Color.BLACK;

    public AtributInfo() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    public void updateInfo(Map<String, String> attributes) {
        removeAll();

        int attributeCount = attributes.size();
        int barHeight = Math.max(15, ((GamePanel.PANELHEIGTH/3) / attributeCount)/4);

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            JPanel attributePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            attributePanel.setBackground(BACKGROUND_COLOR);
            attributePanel.setAlignmentX(LEFT_ALIGNMENT);

            JLabel label = new JLabel(key + ": ");
            label.setForeground(TEXT_COLOR);
            attributePanel.add(label);

            try {
                int numericValue = Integer.parseInt(value);
                int maxValue = getMaxValueForKey(key);
                Color barColor = getColorForKey(key);

                Barre barre = new Barre(numericValue, maxValue, barColor,
                        GamePanel.PANEL_INFO_WIDTH/2, barHeight, 30);
                barre.setString(value); // Affiche la valeur numérique

                attributePanel.add(barre);
            } catch (NumberFormatException e) {
                JLabel valueLabel = new JLabel(value);
                valueLabel.setForeground(TEXT_COLOR);
                attributePanel.add(valueLabel);
            }

            add(attributePanel);
        }
        revalidate();
        repaint();
    }

    private Color getColorForKey(String key) {
        return switch (key.toLowerCase()) {
            case "oxygen" -> new Color(0, 100, 200); // Bleu profond
            case "stamina" -> new Color(255, 165, 0); // Orange vif
            case "backpack" -> new Color(128, 0, 128); // Violet
            default -> new Color(50, 150, 50); // Vert par défaut
        };
    }

    private int getMaxValueForKey(String key) {
        switch (key.toLowerCase()) {
            case "oxygen": return 100;
            case "stamina": return 100;
            case "backpack": return 4;
            default: return 100;
        }
    }
}