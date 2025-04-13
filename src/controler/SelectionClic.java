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
import model.objets.Ressource;
import java.util.ArrayList;

public class SelectionClic extends MouseAdapter implements MouseListener {
    private GamePanel panel;
    private KeyboardController keyboardController;
    //private ButtonPanel buttonPanel; // Référence à ButtonPanel

    private enum SelectionType { NONE, UNIT, RESOURCE }
    private SelectionType currentSelectionType = SelectionType.NONE;

    private int startXView, startYView, endXView, endYView;
    private int startXWorld, startYWorld, endXWorld, endYWorld;

    private Ressource ressourceRecuperationModetarget;
    private Enemy enemyAttackModetarget;
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
        panel.hideResourceInfoPanel();
        panel.showEmptyInfoPanel();
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



        if(!GamePanel.getInstance().isWithinTerrainBounds(new Position(x, y))){
            System.out.println("Clic en dehors du terrain");
            return;
        }



        if (handleMinimapClick(point)) return;


        if (e.getButton() == MouseEvent.BUTTON1) {

            if (GamePanel.getInstance().isPendingShootAction()) {
                handleShootingAction(x, y);
                GamePanel.getInstance().setPendingShootAction(false); // Reset the pending shoot action
                return;
            }

            handleSelection(e, x, y);
            if(panel.isRecuperationMode()){
                handleRecuperationMode(x, y);
            }else if(panel.isAttackingMode()){
                handleAttackingMode(x, y);
            }else if(panel.isDeplacementMode()){
                handleDeplacementMode(x, y);
            }

        }

        if (e.getButton() == MouseEvent.BUTTON3) {          // on a le droit d'utliser l'action deplacer ou clic droit pour deplacer
            handleRightClickOrder(x, y);
        }




    }
    //------------------------------------------------------------------------------------------------------------------------

    public int getNeighbourPos(int rest, int x){
        if (rest < 20 && x > 0) {
            x -=  TileManager.TILESIZE;
        }else if (rest > TileManager.TILESIZE - 20 && x < GamePanel.TERRAIN_WIDTH) {
            x += TileManager.TILESIZE;
        }
        return x;
    }


    private void handleSelection(MouseEvent e, int x, int y) {
        if(!panel.isRecuperationMode() && !panel.isAttackingMode() && !panel.isDeplacementMode()) {
            dropUnitesSelectionnees();
        }

        isSelecting = true;

        int restX = x % TileManager.TILESIZE;
        int restY = y % TileManager.TILESIZE;

        CoordGrid clickedCell = TileManager.transformePos_to_Coord(new Position(x, y));
        CopyOnWriteArrayList<Objet> objectsInCell = new CopyOnWriteArrayList<>();

        CopyOnWriteArrayList<Objet> primaryObjects = panel.getObjetsMap().get(clickedCell);
        if (primaryObjects != null) {
            objectsInCell.addAll(primaryObjects);
        }

        //on se retrouve trop près d'un bord de la tile
        boolean nearLeftOrRightEdge = restX < 20 || restX > TileManager.TILESIZE - 20;
        boolean nearTopOrBottomEdge = restY < 20 || restY > TileManager.TILESIZE - 20;
        ArrayList<CoordGrid> neighbors = new ArrayList<>();
        int neighbourX = getNeighbourPos(restX, x);
        int neighbourY = getNeighbourPos(restY, y);

        // il sa'git d'un coin
        if (nearLeftOrRightEdge && nearTopOrBottomEdge) {
            neighbors.add(TileManager.transformePos_to_Coord(new Position(neighbourX, y))); // Horizontal neighbor
            neighbors.add(TileManager.transformePos_to_Coord(new Position(x, neighbourY))); // Vertical neighbor
            neighbors.add(TileManager.transformePos_to_Coord(new Position(neighbourX, neighbourY))); // Diagonal neighbor
            for (CoordGrid corner : neighbors) {
                CopyOnWriteArrayList<Objet> cornerObjects = panel.getObjetsMap().get(corner);
                if (cornerObjects != null) {
                    objectsInCell.addAll(cornerObjects);
                }
            }
        }else if(nearLeftOrRightEdge || nearTopOrBottomEdge) {
            CoordGrid neighbourCoord = TileManager.transformePos_to_Coord(new Position(neighbourX, neighbourY));
            if (neighbourCoord != null) {
                CopyOnWriteArrayList<Objet> neighborObjects = panel.getObjetsMap().get(neighbourCoord);
                if (neighborObjects != null) {
                    objectsInCell.addAll(neighborObjects);
                }
            }
        }





        if (objectsInCell != null) {
            for (Objet objet : objectsInCell) {
                Ellipse2D.Double objectBounds = new Ellipse2D.Double(
                        objet.getPosition().getX() - objet.getRayon(),
                        objet.getPosition().getY() - objet.getRayon(),
                        objet.getRayon() * 2,
                        objet.getRayon() * 2
                );
                if (objectBounds.contains(x, y)) {
                    if (objet instanceof UniteControlable && ! panel.isDeplacementMode()) {
                        if(panel.isAttackingMode() || panel.isRecuperationMode() )
                            dropUnitesSelectionnees();
                        UniteControlable unite = (UniteControlable) objet;
                        panel.getUnitesSelected().add(unite);
                        unite.setSelected(true);
                        currentSelectionType = SelectionType.UNIT;
                        panel.hideResourceInfoPanel();
                        panel.showFixedInfoPanel("unit");
                        if(GamePanel.getInstance().getUnitesSelected().size() == 1)
                            GamePanel.getInstance().getInfoPanel().updateInfo(unite);

                    } else if (objet instanceof Ressource) {
                        Ressource ressource = (Ressource) objet;
                        currentSelectionType = SelectionType.RESOURCE;

                        if(panel.isRecuperationMode()){
                            ressourceRecuperationModetarget = ressource;
                            panel.hideResourceInfoPanel();
                        }else
                            panel.showResourceInfoPanel(ressource);

                    } else if (objet instanceof Enemy) {
                        Enemy enemy = (Enemy) objet;
                        panel.hideResourceInfoPanel();
                        panel.showFixedInfoPanel("unit");
                        if(panel.isAttackingMode()) {
                            enemyAttackModetarget = enemy;
                            System.out.println("Enemy selected: " + enemy);
                        }else
                            panel.getInfoPanel().updateEnemyInfo(enemy);


                    }
                    return;
                }
            }
        }

        // si le clic est à l'intérieur du viewport
        if (x >= 0 && x <= GamePanel.VIEWPORT_WIDTH && y >= 0 && y <= GamePanel.VIEWPORT_HEIGHT) {
            currentSelectionType = SelectionType.NONE;
            panel.setRessourceSelectionnee(null); // Aucune ressource sélectionnée
            panel.hideResourceInfoPanel();
            panel.showEmptyInfoPanel();
            panel.setRecuperationMode(false);
            panel.setAttackinggMode(false);
        }

    }





    private void handleRecuperationMode(int x, int y) {
        if (ressourceRecuperationModetarget == null) {
            panel.setRecuperationMode(false);
            return;
        }

        for (UniteControlable selectedUnit : panel.getUnitesSelected()) {
            if (selectedUnit instanceof Plongeur && (!(selectedUnit instanceof PlongeurArme))) {
                if(!selectedUnit.canPerformAction("Récupérer (R)")) {
                    panel.setRecuperationMode(false);
                    return;
                }
                if (ressourceRecuperationModetarget != null) {
                    ressourceRecuperationModetarget.setTargeted(true);
                    ((Plongeur) selectedUnit).setTargetResource(ressourceRecuperationModetarget);
                    ((Plongeur) selectedUnit).setDestination(ressourceRecuperationModetarget.getPosition());
                    ressourceRecuperationModetarget = null;
                }
                panel.setRecuperationMode(false);

            }

        }

    }


    private void handleAttackingMode(int x, int y) {
        if (enemyAttackModetarget == null) {
            panel.setAttackinggMode(false);
            return;
        }
        for (UniteControlable selectedUnit : panel.getUnitesSelected()) {
            if (selectedUnit instanceof PlongeurArme) {
                PlongeurArme plongeurArme = (PlongeurArme) selectedUnit;
                plongeurArme.attack(enemyAttackModetarget);


            }
        }
        enemyAttackModetarget = null;
    }


    private void handleShootingAction(int x, int y) {
        Position targetPosition = new Position(x, y);

        for (UniteControlable selectedUnit : panel.getUnitesSelected()) {
            if (selectedUnit instanceof PlongeurArme) {
                PlongeurArme plongeurArme = (PlongeurArme) selectedUnit;

                // Fire the Ammo toward the target position
                boolean success = plongeurArme.shoot(targetPosition);
                if (success) {
                    System.out.println("PlongeurArme fired Ammo toward: " + targetPosition);
                } else {
                    System.out.println("PlongeurArme failed to shoot (out of ammo or out of range).");
                }
            }
        }
    }


    private void handleDeplacementMode(int x, int y) {
        Position destination = new Position(x, y);
        int index = 0;
        int baseRadius = 30;
        double angleIncrement = Math.PI / 4;
        panel.setDeplacementMode(true);

        if (!panel.getUnitesSelected().isEmpty() && !panel.isRecuperationMode()) {
            for (UniteControlable unite : panel.getUnitesSelected()) {
                if (unite.getMovementThread() != null) {
                    unite.getMovementThread().stopThread();
                }
                int radiusIncrement = unite.getRayon();

                int layer = index / 8;
                double radius = baseRadius + layer * radiusIncrement;
                double angle = index * angleIncrement;

                // Calculate the adjusted destination
                int offsetX = (int) (radius * Math.cos(angle));
                int offsetY = (int) (radius * Math.sin(angle));
                Position adjustedDestination = new Position(destination.getX() + offsetX, destination.getY() + offsetY);

                unite.setDestination(adjustedDestination );
                index++;

            }
            panel.showFixedInfoPanel("unit");

        }
        panel.setDeplacementMode(false);
    }


    private void handleRightClickOrder(int x, int y) {
        handleDeplacementMode(x,y);

    }

    //------------------------------------------------------------------------------------------------------------------------

    @Override
    public synchronized void mouseClicked(MouseEvent e) {
        /*Point worldPos = panel.screenToWorld(e.getPoint());
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
            }*/
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
                if(!panel.getUnitesSelected().contains(unite)) {

                    panel.getUnitesSelected().add(unite);
                    unite.setSelected(true);
                }
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




    private boolean handleMinimapClick(Point point) {
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
            return true ;
        }
        return false;
    }





        @Override
    public void mouseEntered (MouseEvent e){
    }
    @Override
    public void mouseExited (MouseEvent e){
    }
}
