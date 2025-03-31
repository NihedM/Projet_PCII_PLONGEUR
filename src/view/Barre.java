package view;

import javax.swing.*;
import java.awt.*;

public class Barre extends JProgressBar {

    public Barre(int currentValue, int maxValue, Color color, int width, int height) {
        super(0, maxValue);
        setForeground(color);
        setStringPainted(true);
        setValue(currentValue);
        setPreferredSize(new Dimension(width, height)); // Adjust the preferred size of the progress bar

    }


}
