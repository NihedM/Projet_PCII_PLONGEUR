package view;

import javax.swing.*;
import java.awt.*;

public class Barre extends JProgressBar {
    private Color baseColor;
    private final int warningThreshold;
    private boolean showAsTime = false;


    public Barre(int currentValue, int maxValue, Color color, int width, int height, int warningThreshold) {
        super(0, maxValue);
        this.baseColor = color;
        this.warningThreshold = warningThreshold;
        setForeground(color);
        setBackground(new Color(200, 200, 200)); // Couleur de fond
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        setStringPainted(true);
        setValue(currentValue);
        setPreferredSize(new Dimension(width, height));
        updateColor(currentValue, maxValue);
    }

    public void setShowAsTime(boolean showAsTime) {
        this.showAsTime = showAsTime;
    }

    @Override
    public String getString() {
        if (showAsTime) {
            int totalSeconds = getValue();
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }
        return super.getString();
    }

    public void updateProgress(int currentValue, int maxValue) {
        setMaximum(maxValue);
        setValue(currentValue);
        updateColor(currentValue, maxValue);
    }

    private void updateColor(int currentValue, int maxValue) {
        int percent = (int)((double)currentValue / maxValue * 100);
        if (percent < warningThreshold) {
            // Mettre en rouge si le pourcentage est inférieur au seuil
            setForeground(Color.RED);
        } else {
            setForeground(baseColor);
        }
    }
}