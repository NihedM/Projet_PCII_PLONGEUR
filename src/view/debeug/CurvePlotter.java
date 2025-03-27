package view.debeug;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

public class CurvePlotter extends JPanel {
    private static final int MAX_STAMINA = 100;
private static final int WIDTH = 500;
private static final int HEIGHT = 500;

@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Draw axes
    g2d.drawLine(50, HEIGHT - 50, WIDTH - 50, HEIGHT - 50); // X-axis
    g2d.drawLine(50, HEIGHT - 50, 50, 50); // Y-axis

    // Draw labels for X-axis
    for (int i = 0; i <= MAX_STAMINA; i += 10) {
        int x = 50 + i * (WIDTH - 100) / MAX_STAMINA;
        g2d.drawString(String.valueOf(i), x - 10, HEIGHT - 30);
    }

    // Draw labels for Y-axis
    for (int i = 0; i <= 10; i++) {
        int y = HEIGHT - 50 - i * (HEIGHT - 100) / 10;
        g2d.drawString(String.valueOf(i), 30, y + 5);
    }

    // Draw curve
    Path2D.Double path = new Path2D.Double();
    for (int stamina = 0; stamina <= MAX_STAMINA; stamina++) {



        double maxSpeed = 5.0;
        double base = 100.0;
        double curve = 10;
        double offset = 20.0;

        double exponent = -curve * (stamina - offset) / base;
        double speed = maxSpeed / (1 + Math.exp(exponent));


        int x = 50 + stamina * (WIDTH - 100) / MAX_STAMINA;
        int y = HEIGHT - 50 - (int) (speed * (HEIGHT - 100) / 10);
        if (stamina == 0) {
            path.moveTo(x, y);
        } else {
            path.lineTo(x, y);
        }
    }
    g2d.draw(path);


    g2d.drawString("Stamina", WIDTH / 2, HEIGHT - 10);
    g2d.drawString("Speed", 10, HEIGHT / 2);
}

public static void main(String[] args) {
    JFrame frame = new JFrame("Curve Plotter");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(WIDTH, HEIGHT);
    frame.add(new CurvePlotter());
    frame.setVisible(true);
}
}