package view;

import controler.GameMaster;
import controler.ZoneEnFonctionnement;
import model.constructions.Base;
import model.objets.Objet;
import model.objets.Position;
import model.objets.Ressource;
import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import model.unite_non_controlables.Enemy;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MinimapPanel extends JPanel {
    private static float getScaleX() {
        return GamePanel.MINIMAP_WIDTH / (float)GamePanel.TERRAIN_WIDTH;
    }

    private static float getScaleY() {
        return GamePanel.MINIMAP_HEIGHT / (float)GamePanel.TERRAIN_HEIGHT;
    }


    public MinimapPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(GamePanel.MINIMAP_WIDTH, GamePanel.MINIMAP_HEIGHT));
        setBackground(new Color(30, 30, 30, 200));
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        setFocusable(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Utiliser les dimensions réelles du composant
        int width = getWidth();
        int height = getHeight();

        // Fond semi-transparent
        g.setColor(new Color(30, 30, 30, 220));
        g.fillRect(0, 0, width, height);

        // Dessiner le terrain
        g.setColor(new Color(70, 70, 70));
        g.fillRect(0, 0, width, height);

        // Taille minimale pour les éléments
        final int MIN_SIZE = 3;

        // Dessiner les ressources UNIQUEMENT si détectées par un plongeur
        for (Ressource ressource : GamePanel.getInstance().getRessourcesMap()) {
            if (isDetectedByDivers(ressource.getPosition())) {
                int x = (int)(ressource.getPosition().getX() * getScaleX());
                int y = (int)(ressource.getPosition().getY() * getScaleY());
                int size = Math.max(MIN_SIZE, (int)(ressource.getRayon() * 2 * getScaleX()));
                g.setColor(ressource.getEtat() == Ressource.Etat.PRET_A_RECOLTER ? Color.GREEN : Color.YELLOW);
                g.fillOval(x, y, size, size);
            }
        }

        // Dessiner les ennemis UNIQUEMENT si détectés par un plongeur
        for (Enemy enemy : GameMaster.getInstance().getEnemies()) {
            if (isDetectedByDivers(enemy.getPosition())) {
                int x = (int)(enemy.getPosition().getX() * getScaleX());
                int y = (int)(enemy.getPosition().getY() * getScaleY());
                int size = Math.max(MIN_SIZE, (int)(enemy.getRayon() * 3 * getScaleX()));
                g.setColor(Color.RED);
                g.fillOval(x, y, size, size);
            }
        }

        // Dessiner TOUTES les unités (plongeurs en carré, autres en cercle)
        for (UniteControlable unite : GamePanel.getInstance().getUnitesEnJeu()) {
            int x = (int)(unite.getPosition().getX() * getScaleX());
            int y = (int)(unite.getPosition().getY() * getScaleY());
            int size = Math.max(MIN_SIZE, (int)(unite.getRayon() * 3 * getScaleX()));

            if (unite instanceof Plongeur) {
                // Carré bleu pour les plongeurs
                g.setColor(unite.isSelected() ? Color.RED : Color.BLUE);
                g.fillRect(x - size/2, y - size/2, size, size);

                // Optionnel : Dessiner leur zone de détection
                g.setColor(new Color(0, 0, 255, 50));
                int detectionSize = (int)(((Plongeur) unite).getDetectionRange() * 2 * getScaleX());
                g.fillOval(x - detectionSize/2, y - detectionSize/2, detectionSize, detectionSize);
            } else {
                // Autres unités en cercle
                g.setColor(unite.isSelected() ? Color.RED : Color.BLUE);
                g.fillOval(x - size/2, y - size/2, size, size);

                g.setColor(new Color(0, 0, 255, 50));
                int detectionSize = (int)(unite.getDetectionRange() * 2 * getScaleX());
                g.fillOval(x - detectionSize/2, y - detectionSize/2, detectionSize, detectionSize);
            }
        }

        Base base = GamePanel.getInstance().getMainBase();
        int x = (int)(base.getPosition().getX() * getScaleX());
        int y = (int)(base.getPosition().getY() * getScaleY());
        int size = Math.max(MIN_SIZE, (int)(base.getRayon() * 3 * getScaleX()));
        g.setColor(Color.CYAN);
        g.fillOval(x, y, size, size);
        Position[] coints = base.getCoints();
        g.setColor(Color.CYAN);
        for(int i = 0; i < coints.length; i++){
            //topleft
            int x1 = (int)(coints[i].getX() * getScaleX());
            int y1 = (int)(coints[i].getY() * getScaleY());
            int x2 = (int)(coints[(i+1)%coints.length].getX() * getScaleX());
            int y2 = (int)(coints[(i+1)%coints.length].getY() * getScaleY());
            g.drawLine(x1, y1, x2, y2);
        }


        // Dessiner le rectangle de la vue actuelle
        g.setColor(new Color(255, 255, 255, 150));
        int viewX = (int)(GamePanel.getInstance().getCameraX() * getScaleX());
        int viewY = (int)(GamePanel.getInstance().getCameraY() * getScaleY());
        int viewW = (int)(GamePanel.VIEWPORT_WIDTH * getScaleX());
        int viewH = (int)(GamePanel.VIEWPORT_HEIGHT * getScaleY());
        g.drawRect(viewX, viewY, viewW, viewH);

        //drawZoneBorders(g);

    }

    private boolean isDetectedByDivers(Position position) {
        for (UniteControlable unite : GamePanel.getInstance().getUnitesEnJeu()) {
            if (unite instanceof Plongeur) {
                double distance = position.distanceTo(unite.getPosition());
                if (distance <= ((Plongeur) unite).getDetectionRange()) {
                    return true;
                }
            }
        }
        return false;
    }


    private void drawZoneBorders(Graphics g) {
        GamePanel gamePanel = GamePanel.getInstance();


        ZoneEnFonctionnement mainZone = gamePanel.getMainZone();
        drawZoneBorder(g, mainZone, Color.RED);

        for (ZoneEnFonctionnement zone : gamePanel.getDynamicZones()) {
            if (zone.equals(mainZone))
                //erreur le main devrais jamais etre ici
                throw new IllegalStateException("Main zone should not be in dynamic zones");
            drawZoneBorder(g, zone, Color.BLUE);
        }
    }
    private void drawZoneBorder(Graphics g, ZoneEnFonctionnement zone, Color color) {
        g.setColor(color);
        int x = (int) (zone.getMinX() * getScaleX());
        int y = (int) (zone.getMinY() * getScaleY());
        int width = (int) ((zone.getMaxX() - zone.getMinX()) * getScaleX());
        int height = (int) ((zone.getMaxY() - zone.getMinY()) * getScaleY());



        if(!zone.equals(GamePanel.getInstance().getMainZone())) {
           // System.out.println("Zone Position: MinX=" + zone.getMinX() + ", MinY=" + zone.getMinY() + ",\n MaxX=" + zone.getMaxX() + ", MaxY=" + zone.getMaxY());

        }
        g.drawRect(x, y, width, height);
    }
}