package controler;

import model.gains_joueur.Referee;
import model.objets.Position;
import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import view.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.util.concurrent.CopyOnWriteArrayList;

public class SelectionClic extends MouseAdapter implements MouseListener {
    private GamePanel panel;
    private KeyboardController keyboardController;
    //private ButtonPanel buttonPanel; // Référence à ButtonPanel

    private enum SelectionType { NONE, UNIT, RESOURCE }
    private SelectionType currentSelectionType = SelectionType.NONE;

    private int startX, startY, endX, endY;
    private boolean isSelecting = false;

    public SelectionClic(GamePanel panel) {
        this.panel = panel;
        this.keyboardController = new KeyboardController(panel, panel.getUnitesSelected());

        // Configuration du focus
        panel.setFocusable(true);
        panel.requestFocusInWindow();


    }

    private Point screenToWorld(Point screenPoint) {
        return new Point(
                screenPoint.x + GamePanel.getInstance().getCameraX(),
                screenPoint.y + GamePanel.getInstance().getCameraY()
        );
    }



    public void dropUnitesSelectionnees() {
        for (model.objets.UniteControlable unite : panel.getUnitesSelected()) {
            unite.setSelected(false);
        }
        panel.getUnitesSelected().clear();
    }


    @Override
    public void mousePressed(MouseEvent e) {
        // Vérifier si le clic est sur la minimap
        if (e.getX() >= panel.getWidth() - GamePanel.getMinimapWidth() - GamePanel.getMinimapMargin() &&
                e.getX() <= panel.getWidth() - GamePanel.getMinimapMargin() &&
                e.getY() >= GamePanel.getMinimapMargin() &&
                e.getY() <= GamePanel.getMinimapMargin() + GamePanel.getMinimapHeight()) {

            // Calculer les coordonnées dans le monde
            int worldX = (int)((e.getX() - (panel.getWidth() - GamePanel.getMinimapWidth() - GamePanel.getMinimapMargin())) / GamePanel.getMinimapScale());
            int worldY = (int)((e.getY() - GamePanel.getMinimapMargin()) / GamePanel.getMinimapScale());

            // Centrer la caméra sur ce point
            panel.moveCamera(worldX - GamePanel.VIEWPORT_WIDTH/2, worldY - GamePanel.VIEWPORT_HEIGHT/2);
            return;
        }
        Point worldPos = screenToWorld(e.getPoint());
        int x = worldPos.x;
        int y = worldPos.y;
        // Si le mode déplacement est actif, on ne fait rien ici
        if (panel.isDeplacementMode()) {
            return;
        }

        // Convertir les coordonnées du clic
        java.awt.Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), panel);
        startX = point.x;
        startY = point.y;
        endX = startX;
        endY = startY;

        if (e.getButton() == MouseEvent.BUTTON1) {
            // Si le mode récupération est activé, on recherche une ressource
            if (panel.isRecuperationMode()) {
                boolean resourceFound = false;
                for (model.objets.Ressource ressource : panel.getRessourcesMap()) {
                    int rayon = ressource.getRayon();
                    int resX = ressource.getPosition().getX();
                    int resY = ressource.getPosition().getY();
                    Ellipse2D.Double cercleRessource = new Ellipse2D.Double(
                            resX - rayon,
                            resY - rayon,
                            rayon * 2,
                            rayon * 2
                    );
                    if (cercleRessource.contains(x, y)) {
                        resourceFound = true;
                        if (!panel.getUnitesSelected().isEmpty()) {
                            model.objets.UniteControlable selectedUnit = panel.getUnitesSelected().get(0);
                            if (selectedUnit instanceof model.unite_controlables.Plongeur) {
                                model.unite_controlables.Plongeur plongeur = (model.unite_controlables.Plongeur) selectedUnit;
                                // Affecter la ressource et activer son flag fixed (via setTargeted)
                                plongeur.setTargetResource(ressource);
                                ressource.setTargeted(true);  // ou setFixed(true)
                                plongeur.setDestination(new Position(resX, resY));
                            }
                        }
                        panel.repaint();
                        break;
                    }
                }
                panel.setRecuperationMode(false);
                if (!resourceFound) {
                    JOptionPane.showMessageDialog(panel, "Aucune ressource détectée.");
                }
                return;
            }

            // Si on n'est pas en mode récupération, on effectue la sélection d'une unité ou d'une ressource classique
            dropUnitesSelectionnees();
            boolean unitSelected = false;
            isSelecting = true;
            for (model.objets.UniteControlable unite : panel.getUnitesEnJeu()) {
                Ellipse2D.Double cercle = new Ellipse2D.Double(
                        unite.getPosition().getX() - unite.getRayon(),
                        unite.getPosition().getY() - unite.getRayon(),
                        unite.getRayon() * 2,
                        unite.getRayon() * 2
                );
                if (cercle.contains(x, y)) {
                    panel.getUnitesSelected().add(unite);
                    unite.setSelected(true);
                    unitSelected = true;
                    GamePanel.getInstance().getInfoPanel().updateInfo(unite);
                    break;
                }
            }
            if (unitSelected) {
                currentSelectionType = SelectionType.UNIT;
                panel.showFixedInfoPanel("unit");
                panel.hideResourceInfoPanel();
            } else {
                boolean resourceSelected = false;
                for (model.objets.Ressource ressource : panel.getRessourcesMap()) {
                    int rayon = ressource.getRayon();
                    int resX = ressource.getPosition().getX();
                    int resY = ressource.getPosition().getY();
                    Ellipse2D.Double cercleRessource = new Ellipse2D.Double(
                            resX - rayon,
                            resY - rayon,
                            rayon * 2,
                            rayon * 2
                    );
                    if (cercleRessource.contains(x, y)) {
                        currentSelectionType = SelectionType.RESOURCE;
                        panel.showResourceInfoPanel(ressource); // Appeler showResourceInfoPanel pour afficher les informations
                        resourceSelected = true;
                        break;
                    }
                }
                if (!resourceSelected) {
                    currentSelectionType = SelectionType.NONE;
                    panel.setRessourceSelectionnee(null); // Aucune ressource sélectionnée
                    panel.showEmptyInfoPanel();
                    panel.getUnitesSelected().clear();
                }
            }
        }

        if (e.getButton() == MouseEvent.BUTTON3) {          // on a le droit d'utliser l'action deplacer ou clic droit pour deplacer
            if (!panel.getUnitesSelected().isEmpty() && !panel.isDeplacementMode() && !panel.isRecuperationMode()) {
                for (UniteControlable unite : panel.getUnitesSelected()) {
                    unite.setDestination(new Position(e.getX(), e.getY()));
                }
                panel.showFixedInfoPanel("unit");

            }
        }
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        Point worldPos = screenToWorld(e.getPoint());
        int x = worldPos.x;
        int y = worldPos.y;
        //System.out.println("mouseClicked reçu, deplacementMode = " + panel.isDeplacementMode());
        if (e.getButton() == MouseEvent.BUTTON1) {

            if (panel.isDeplacementMode()) {
                int destX = e.getX();
                int destY = e.getY();

                //Check les coordonnées pour qu'elles restent dans l'aire de jeu.
                if (destX > GamePanel.GAME_AREA_WIDTH) {
                    destX = GamePanel.GAME_AREA_WIDTH;
                }
                if (destX < GamePanel.TERRAIN_MIN_X) {
                    destX = GamePanel.TERRAIN_MIN_X;
                }
                if (destY > GamePanel.TERRAIN_MAX_Y) {
                    destY = GamePanel.TERRAIN_MAX_Y;
                }
                if (destY < GamePanel.TERRAIN_MIN_Y) {
                    destY = GamePanel.TERRAIN_MIN_Y;
                }

                for (UniteControlable unite : panel.getUnitesSelected()) {
                    if (unite.getMovementThread() != null) {
                        unite.getMovementThread().stopThread();
                    }
                    unite.setDestination(new Position(destX, destY));
                    //System.out.println("Destination définie à : " + destX + ", " + destY);
                }
                panel.setDeplacementMode(false);
                panel.showFixedInfoPanel("unit");

                return;
            }

        } else {
                //TOUCHER PAS ICI SANS CONSULTER
            }
        }


    @Override
    public void mouseDragged(MouseEvent e) {
        if (isSelecting) {
            java.awt.Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), panel);
            endX = point.x;
            endY = point.y;
            panel.repaint();
        }
    }


    @Override
    public void mouseReleased (MouseEvent e){
        if (isSelecting) {
            isSelecting = false;
            selectUnitsInRectangle();
            panel.repaint();
        }
    }


    public void selectUnitsInRectangle() {
        int x = Math.min(startX, endX);
        int y = Math.min(startY, endY);
        int width = Math.abs(startX - endX);
        int height = Math.abs(startY - endY);
        Rectangle selectionRect = new Rectangle(x, y, width, height);


        for (UniteControlable unite : panel.getUnitesEnJeu()) {
            if (selectionRect.contains(unite.getPosition().getX(), unite.getPosition().getY())) {
                panel.getUnitesSelected().add(unite);
                unite.setSelected(true);
            }
            //else unite.setSelected(false);
        }
        if (!panel.getUnitesSelected().isEmpty()) {
            panel.showFixedInfoPanel("unit");
            GamePanel.getInstance().getInfoPanel().updateInfo(panel.getUnitesSelected().get(0));
        }
    }

    public void paintSelection(Graphics g) {
        if (isSelecting) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(0, 0, 255, 100));
            g2d.fillRect(Math.min(startX, endX), Math.min(startY, endY), Math.abs(startX - endX), Math.abs(startY - endY));
            g2d.setColor(Color.BLUE);
            g2d.drawRect(Math.min(startX, endX), Math.min(startY, endY), Math.abs(startX - endX), Math.abs(startY - endY));
        }
    }


    @Override
    public void mouseEntered (MouseEvent e){
    }
    @Override
    public void mouseExited (MouseEvent e){
    }
}
