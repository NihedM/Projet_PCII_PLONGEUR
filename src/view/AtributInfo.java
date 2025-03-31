package view;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class AtributInfo extends JPanel {

    public AtributInfo() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.GREEN);
    }

    public void updateInfo(Map<String, String> attributes) {
        removeAll();

        int attributeCount = attributes.size();
        int barHeight = ((GamePanel.PANELHEIGTH/3) / attributeCount)/4; // Calculate height based on panel height and number of attributes



        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey();

            String value = entry.getValue();
            JPanel attributePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            attributePanel.setBackground(Color.GREEN); // Set the background color for each attribute panel

            try {
                int numericValue = Integer.parseInt(value);
                int maxValue = getMaxValueForKey(key);
                Color barColor = getColorForKey(key);
                Barre barre = new Barre(numericValue, maxValue, barColor, GamePanel.PANEL_INFO_WIDTH/2,barHeight);
                attributePanel.add(new JLabel(key + ": "));
                attributePanel.add(barre);
            } catch (NumberFormatException e) {
                attributePanel.add(new JLabel(key + ": " + value));
            }
            add(attributePanel);

        }
        revalidate();
        repaint();
    }

    private Color getColorForKey(String key) {
        return switch (key) {
            case "Oxygen" -> Color.BLUE;
            case "Stamina" -> Color.ORANGE;
            case "Backpack" -> Color.MAGENTA;
            default -> Color.RED; // Default color
        };

    }


    private int getMaxValueForKey(String key) {
        // Implement this method to return the max value for each attribute
        // For example:
        switch (key) {
            case "Oxygen":
                return 100;
            case "Stamina":
                return 100;
            case "Backpack":
                return 4;
            default:
                return 100; // Default max value
        }
    }

}
