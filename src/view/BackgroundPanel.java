package view;

import javax.swing.*;
import java.awt.*;

public class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel() {
        super();
    }

    public void setBackgroundImage(String imagePath) {
        this.backgroundImage = GamePanel.getCachedImage(imagePath);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);


        setOpaque(false);

        if (backgroundImage != null) {

            g.drawImage(backgroundImage, 0, -150,getWidth(), getHeight()*2, this);

        }
    }
}
