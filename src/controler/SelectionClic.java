package controler;

import model.gains_joueur.Referee;
import model.objets.CoordGrid;
import model.objets.Objet;
import model.objets.Position;
import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import model.unite_controlables.PlongeurArme;
import model.unite_non_controlables.Enemy;
import view.GamePanel;
import view.MinimapPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.util.concurrent.CopyOnWriteArrayList;

import static view.GamePanel.MAX_SELECTED_UNITS;

public class SelectionClic extends MouseAdapter implements MouseListener {
    private GamePanel panel;
    private KeyboardController keyboardController;
    //private ButtonPanel buttonPanel; // Référence à ButtonPanel

    private enum SelectionType { NONE, UNIT, RESOURCE }
    private SelectionType currentSelectionType = SelectionType.NONE;

    private int startXView, startYView, endXView, endYView;
    private int startXWorld, startYWorld, endXWorld, endYWorld;

    private boolean isSelecting = false;


    public SelectionClic(GamePanel panel) {
        this.panel = panel;
        this.keyboardController = new KeyboardController(panel, panel.getUnitesSelected());

        // Configuration du focus
        panel.setFocusable(true);
        panel.requestFocusInWindow();


    }



    public void dropUnitesSelectionnees() {
        for (model.objets.UniteControlable unite : panel.getUnitesSelected()) {
            unite.setSelected(false);
        }
        panel.getUnitesSelected().clear();
    }


    @Override
    public synchronized void mousePressed(MouseEvent e) {

        // Convertir les coordonnées du clic
        java.awt.Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), panel);
        Point worldPos = panel.screenToWorld(point);
        int x = worldPos.x;
        int y = worldPos.y;

        startXWorld = x;
        startYWorld = y;

        endXView = point.x;
        endYView = point.y;

        endXWorld = x;
        endYWorld = y;

        startXView = endXView;
        startYView = endYView;


        // Vérifier si le clic est sur la minimap
        MinimapPanel minimap = GamePanel.getInstance().getMinimapPanel();
        if (minimap != null &&
                point.x >= minimap.getX() &&
                point.x <= minimap.getX() + minimap.getWidth() &&
                point.y >= minimap.getY() &&
                point.y <= minimap.getY() + minimap.getHeight()) {

            // Calculer les coordonnées dans le monde
            double minimapX = point.x - minimap.getX();
            double minimapY = point.y - minimap.getY();

            // Convertir en coordonnées monde
            double worldX = minimapX / GamePanel.MINIMAP_SCALE_X;
            double worldY = minimapY / GamePanel.MINIMAP_SCALE_Y;

            // Centrer la caméra sur ce point
            GamePanel.getInstance().moveCamera(
                    (int)(worldX - GamePanel.VIEWPORT_WIDTH / 2) - GamePanel.getInstance().getCameraX(),
                    (int)(worldY - GamePanel.VIEWPORT_HEIGHT / 2) - GamePanel.getInstance().getCameraY()
            );
            return;
        }
        // Si le mode déplacement est actif, on ne fait rien ici
        if (panel.isDeplacementMode()) {
            return;
        }

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
                            if(!selectedUnit.canPerformAction("Récupérer (R)")) {
                                panel.setRecuperationMode(false);
                                return;
                            }
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


            if (panel.isShootingMode()){
                worldPos = panel.screenToWorld(e.getPoint());
                x = worldPos.x;
                y = worldPos.y;

                if(!GamePanel.getInstance().isWithinTerrainBounds(new Position(x, y))){
                    panel.setShootingMode(false);
                    return;
                }

                CoordGrid gridCoord = TileManager.transformePos_to_Coord(new Position(x, y));
                CopyOnWriteArrayList<Objet> objetsAtCoord = panel.getObjetsMap().get(gridCoord);
                if (objetsAtCoord != null) {

                    for (Objet objet : objetsAtCoord) {
                        System.out.println("Objet trouvé : " + objet);



                        if (objet instanceof Enemy) {
                            Enemy enemy = (Enemy) objet;
                            Ellipse2D.Double enemyArea = new Ellipse2D.Double(
                                    enemy.getPosition().getX() - enemy.getRayon(),
                                    enemy.getPosition().getY() - enemy.getRayon(),
                                    enemy.getRayon() * 2,
                                    enemy.getRayon() * 2
                            );

                            if (enemyArea.contains(x, y)) {
                                System.out.println("Enemy selected: " + objet);
                                // Trouvé un ennemi à attaquer
                                for (UniteControlable unite : panel.getUnitesSelected()) {
                                    if (unite instanceof PlongeurArme ) {
                                        ((PlongeurArme)unite).attack(enemy);
                                    }
                                }
                                return;
                            }


                        }
                    }
                }
                panel.setShootingMode(false);
                panel.repaint();
                return;

            }



            // ----------------------------------------------------------------------------------------------------------------------------------




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
                    if(GamePanel.getInstance().getUnitesSelected().size() == 1)
                        GamePanel.getInstance().getInfoPanel().updateInfo(unite);
                    break;
                }
            }
            if (unitSelected && GamePanel.getInstance().getUnitesSelected().size() == 1){
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



            if(!GamePanel.getInstance().isWithinTerrainBounds(new Position(x, y))){
                System.out.println("Clic en dehors du terrain");
                return;
            }

            CoordGrid gridCoord = TileManager.transformePos_to_Coord(new Position(x, y));
            CopyOnWriteArrayList<Objet> objetsAtCoord = panel.getObjetsMap().get(gridCoord);
            if(objetsAtCoord == null) return;

            for(Objet objet: objetsAtCoord) {
                if (objet instanceof Enemy) {
                    Enemy enemy = (Enemy) objet;
                    Ellipse2D.Double enemyArea = new Ellipse2D.Double(
                            enemy.getPosition().getX() - enemy.getRayon(),
                            enemy.getPosition().getY() - enemy.getRayon(),
                            enemy.getRayon() * 2,
                            enemy.getRayon() * 2
                    );

                    if (enemyArea.contains(x, y)) {
                        panel.getInfoPanel().updateEnemyInfo(enemy);
                        panel.showFixedInfoPanel("unit");
                        return;
                    }
                }
            }
        }

        if (e.getButton() == MouseEvent.BUTTON3) {          // on a le droit d'utliser l'action deplacer ou clic droit pour deplacer
            if (!panel.getUnitesSelected().isEmpty() && !panel.isDeplacementMode() && !panel.isRecuperationMode()) {
                for (UniteControlable unite : panel.getUnitesSelected()) {
                    unite.setDestination(new Position(worldPos.x, worldPos.y));
                }
                panel.showFixedInfoPanel("unit");

            }
        }




    }


    @Override
    public synchronized void mouseClicked(MouseEvent e) {
        Point worldPos = panel.screenToWorld(e.getPoint());
        int x = worldPos.x;
        int y = worldPos.y;
        //System.out.println("mouseClicked reçu, deplacementMode = " + panel.isDeplacementMode());
        if (e.getButton() == MouseEvent.BUTTON1) {

            if (panel.isDeplacementMode()) {
                int destX = worldPos.x;
                int destY = worldPos.y;

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
    public synchronized void mouseDragged(MouseEvent e) {
        if (isSelecting) {
            java.awt.Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), panel);
            endXView = point.x;
            endYView = point.y;

            Point worldPos = panel.screenToWorld(e.getPoint());
            endXWorld = worldPos.x;
            endYWorld= worldPos.y;

            panel.repaint();
        }
    }


    @Override
    public synchronized void mouseReleased (MouseEvent e){
        if (isSelecting) {
            isSelecting = false;
            selectUnitsInRectangle();
            panel.repaint();
        }
    }


    public synchronized void selectUnitsInRectangle() {

        int x = Math.min(startXWorld, endXWorld);
        int y = Math.min(startYWorld, endYWorld);
        int width = Math.abs(startXWorld - endXWorld);
        int height = Math.abs(startYWorld - endYWorld);
        Rectangle selectionRect = new Rectangle(x, y, width, height);


        for (UniteControlable unite : panel.getUnitesEnJeu()) {
            if (selectionRect.contains(unite.getPosition().getX(), unite.getPosition().getY())) {
                if (panel.getUnitesSelected().size() >= MAX_SELECTED_UNITS) {
                    System.out.println("Cannot select more units. Maximum limit reached: " + MAX_SELECTED_UNITS);
                    break;
                }
                panel.getUnitesSelected().add(unite);
                unite.setSelected(true);
            }
            //else unite.setSelected(false);
        }

        if (!panel.getUnitesSelected().isEmpty()) {
            panel.showFixedInfoPanel("unit");
            if (panel.getUnitesSelected().size() > 1) {
                GamePanel.getInstance().getInfoPanel().updateMultipleInfo(panel.getUnitesSelected());
            } else {
                GamePanel.getInstance().getInfoPanel().updateInfo(panel.getUnitesSelected().get(0));
            }
        }
    }

    public void paintSelection(Graphics g) {
        if (isSelecting) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(0, 0, 255, 100));
            g2d.fillRect(Math.min(startXView, endXView), Math.min(startYView, endYView), Math.abs(startXView - endXView), Math.abs(startYView - endYView));
            g2d.setColor(Color.BLUE);
            g2d.drawRect(Math.min(startXView, endXView), Math.min(startYView, endYView), Math.abs(startXView - endXView), Math.abs(startYView - endYView));
        }
    }



    @Override
    public void mouseEntered (MouseEvent e){
    }
    @Override
    public void mouseExited (MouseEvent e){
    }
}
