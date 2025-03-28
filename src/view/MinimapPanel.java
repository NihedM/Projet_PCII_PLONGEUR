package view;

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

        // Dessiner les objets
        for (Objet objet : GamePanel.getInstance().getObjetsMap().values()
                .stream().flatMap(CopyOnWriteArrayList::stream).toList()) {

            int x = (int)(objet.getPosition().getX() * SCALE_X);
            int y = (int)(objet.getPosition().getY() * SCALE_Y);

            int size = (int) (objet.getRayon() * 2 * SCALE_X); // Proportional size


            if (objet instanceof UniteControlable) {
                g.setColor(objet instanceof Plongeur ? Color.BLUE : Color.GREEN);
            } else if (objet instanceof Ressource) {
                g.setColor(((Ressource) objet).getEtat() == Ressource.Etat.PRET_A_RECOLTER
                        ? Color.GREEN : Color.YELLOW);
            } else {
                g.setColor(Color.RED);
            }
            g.fillRect(x - size / 2, y - size / 2, size, size);

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