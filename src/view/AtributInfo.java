package view;

import model.objets.Unite;
import model.objets.UniteControlable;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class AtributInfo extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(173, 216, 230); // Bleu clair comme GamePanel
    private static final Color TEXT_COLOR = Color.BLACK;
    private Unite unite;


    public AtributInfo(Unite unite) {
        this.unite = unite;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    public void updateInfo(Map<String, String> attributes) {
        removeAll();

        int attributeCount = attributes.size();
        int barHeight = Math.max(15, ((GamePanel.getPanelHeight()/3) / attributeCount)/4);

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
        return unite.getColorForKey(key);
    }

    private int getMaxValueForKey(String key) {
        return unite.getMaxValueForKey(key);
    }
}