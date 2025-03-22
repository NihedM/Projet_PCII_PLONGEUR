package controler;

import model.gains_joueur.Referee;
import model.objets.Position;
import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import view.GamePanel;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.util.concurrent.CopyOnWriteArrayList;

public class SelectionClic implements MouseListener {
    private CopyOnWriteArrayList<model.objets.UniteControlable> unitesVisibles;
    private CopyOnWriteArrayList<model.objets.UniteControlable> unitesSelectionnees;
    private GamePanel panel;
    //private ButtonPanel buttonPanel; // Référence à ButtonPanel

    private enum SelectionType { NONE, UNIT, RESOURCE }
    private SelectionType currentSelectionType = SelectionType.NONE;


    public SelectionClic(CopyOnWriteArrayList<model.objets.UniteControlable> unites, CopyOnWriteArrayList<model.objets.UniteControlable> unitesSelectionnees, GamePanel panel){//ButtonPanel buttonPanel) {
        this.unitesVisibles = unites;
        this.unitesSelectionnees = unitesSelectionnees;
        this.panel = panel;
        //this.buttonPanel = buttonPanel; // Initialiser ButtonPanel
    }

    public CopyOnWriteArrayList<model.objets.UniteControlable> getUnitesSelectionnees() {return unitesSelectionnees;}
    public void dropUnitesSelectionnees() {
        for (model.objets.UniteControlable unite : unitesSelectionnees) {
            unite.setSelected(false);
        }
        unitesSelectionnees.clear();
    }


    @Override
    public void mousePressed(MouseEvent e) {
        // Si le mode déplacement est actif, on ne fait rien ici
        if (panel.isDeplacementMode()) {
            return;
        }

        // Convertir les coordonnées du clic
        java.awt.Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), panel);
        int x = point.x;
        int y = point.y;

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

                        // On suppose qu'une seule unité est sélectionnée
                        if (!unitesSelectionnees.isEmpty()) {
                            model.objets.UniteControlable selectedUnit = unitesSelectionnees.get(0);
                            // Définir la destination de l'unité vers la ressource
                            selectedUnit.setDestination(new Position(resX, resY));

                            // Retirer la ressource du jeu :
                            panel.removeObjet(ressource, ressource.getCoordGrid());
                            panel.getRessources().remove(ressource);

                            // Augmenter l'argent du joueur (via Referee)
                            Referee.getInstance().ajouterPointsVictoire(30);
                            GamePanel.getInstance().addCollectedResource(ressource);

                            panel.repaint();
                        }
                        break;
                    }
                }
                // Après traitement, désactiver le mode récupération
                panel.setRecuperationMode(false);
                if (!resourceFound) {
                    // Optionnel : afficher un message si aucune ressource n'a été cliquée
                    JOptionPane.showMessageDialog(panel, "Aucune ressource détectée.");
                }
                return;
            }

            // Si on n'est pas en mode récupération, on effectue la sélection d'une unité ou d'une ressource classique
            dropUnitesSelectionnees();
            boolean unitSelected = false;
            for (model.objets.UniteControlable unite : unitesVisibles) {
                Ellipse2D.Double cercle = new Ellipse2D.Double(
                        unite.getPosition().getX() - unite.getRayon(),
                        unite.getPosition().getY() - unite.getRayon(),
                        unite.getRayon() * 2,
                        unite.getRayon() * 2
                );
                if (cercle.contains(x, y)) {
                    unitesSelectionnees.add(unite);
                    unite.setSelected(true);
                    unitSelected = true;
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
                    unitesSelectionnees.clear();
                }
            }
        }

        if (e.getButton() == MouseEvent.BUTTON3) {          // on a le droit d'utliser l'action deplacer ou clic droit pour deplacer
            if (!unitesSelectionnees.isEmpty() && !panel.isDeplacementMode() && !panel.isRecuperationMode()) {
                for (UniteControlable unite : unitesSelectionnees) {
                    unite.setDestination(new Position(e.getX(), e.getY()));
                }
            }
        }
    }


    @Override
    public void mouseClicked(MouseEvent e) {
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

                for (UniteControlable unite : unitesSelectionnees) {
                    if (unite.getMovementThread() != null) {
                        unite.getMovementThread().stopThread();
                    }
                    unite.setDestination(new Position(destX, destY));
                    System.out.println("Destination définie à : " + destX + ", " + destY);
                }
                panel.setDeplacementMode(false);
                return;
            }

        } else {
                // On peut ajouter ici d'autres traitements si nécessaire
                if (currentSelectionType == SelectionType.NONE) {
                      panel.showEmptyInfoPanel();
                }
                panel.repaint();
            }
        }



    @Override
    public void mouseReleased (MouseEvent e){
    }
    @Override
    public void mouseEntered (MouseEvent e){
    }
    @Override
    public void mouseExited (MouseEvent e){
    }
}
