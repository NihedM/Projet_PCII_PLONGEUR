package view;

import model.constructions.Base;
import model.objets.Objet;
import model.objets.Ressource;
import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MinimapPanel extends JPanel {
    private static final int WIDTH = 180;
    private static final int HEIGHT = 120;
    private static final float SCALE_X = WIDTH / (float) GamePanel.TERRAIN_WIDTH;
    private static final float SCALE_Y = HEIGHT / (float) GamePanel.TERRAIN_HEIGHT;


    public MinimapPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(30, 30, 30, 200));
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        setFocusable(false); // Pour éviter qu'elle capture les événements
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Fond semi-transparent plus visible
        g.setColor(new Color(30, 30, 30, 220)); // Augmenter l'opacité
        g.fillRect(0, 0, getWidth(), getHeight());

        // Dessiner le terrain (avec le bon ratio)
        g.setColor(new Color(70, 70, 70)); // Couleur plus claire
        g.fillRect(0, 0, getWidth(), getHeight());

        // Taille minimale pour les éléments
        final int MIN_SIZE = 3;

        // Dessiner les ressources
        for (Ressource ressource : GamePanel.getInstance().getRessourcesMap()) {
            int x = (int)(ressource.getPosition().getX() * SCALE_X);
            int y = (int)(ressource.getPosition().getY() * SCALE_Y);
            int size = Math.max(MIN_SIZE, (int)(ressource.getRayon() * 2 * SCALE_X));

            // Couleur selon l'état de la ressource
            if (ressource.getEtat() == Ressource.Etat.PRET_A_RECOLTER) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(Color.YELLOW);
            }
            g.fillOval(x, y, size, size);
        }

        // Dessiner les unités
        for (UniteControlable unite : GamePanel.getInstance().getUnitesEnJeu()) {
            int x = (int)(unite.getPosition().getX() * SCALE_X);
            int y = (int)(unite.getPosition().getY() * SCALE_Y);
            int size = Math.max(MIN_SIZE, (int)(unite.getRayon() * 3 * SCALE_X));

            g.setColor(unite.isSelected() ? Color.RED : Color.BLUE);
            g.fillOval(x, y, size, size);
        }

        // Dessiner le rectangle de la vue actuelle
        g.setColor(new Color(255, 255, 255, 150)); // Plus visible
        int viewX = (int)(GamePanel.getInstance().getCameraX() * SCALE_X);
        int viewY = (int)(GamePanel.getInstance().getCameraY() * SCALE_Y);
        int viewW = (int)(GamePanel.VIEWPORT_WIDTH * SCALE_X);
        int viewH = (int)(GamePanel.VIEWPORT_HEIGHT * SCALE_Y);
        g.drawRect(viewX, viewY, viewW, viewH);
    }
}