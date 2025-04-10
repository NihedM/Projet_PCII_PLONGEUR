package model.objets;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import view.ButtonAction;
import view.GamePanel;

import javax.swing.*;

public class UniteControlable extends Unite {
    private final int id;
    private boolean selected = false;
    private boolean dynamicZoneCreated = false;
    private model.objets.DeplacementThread movementThread;




    public UniteControlable(int id, Position position, int rayon, int vitesse, int hp) {
        super(position, rayon, vitesse,hp);
        this.id = id;
    }
    public int getId() {return id;}

    public boolean isSelected() {return selected;}
    public boolean contains(Point point) {
        int x = getPosition().getX();
        int y = getPosition().getY();
        int rayon = getRayon();
        return point.distance(x, y) <= rayon;
    }

    public void setSelected(boolean selected) {this.selected = selected;}

    public boolean isDynamicZoneCreated() {return dynamicZoneCreated;}
    public void setDynamicZoneCreated(boolean dynamicZoneCreated) {this.dynamicZoneCreated = dynamicZoneCreated;}
    public void setMovementThread(model.objets.DeplacementThread dt) {
        this.movementThread = dt;
    }
    public DeplacementThread getMovementThread() {
        return this.movementThread;
    }

    public boolean canPerformAction(String actionLabel) {
        for (ButtonAction action : getButtonActions()) {
            if (action.getLabel().equalsIgnoreCase(actionLabel)) {
                return true;
            }
        }
        return false;
    }

    public java.util.List<ButtonAction> getButtonActions() {
        List<ButtonAction> actions = new ArrayList<>();

        actions.add(new ButtonAction("Se déplacer (D)", e -> {
            GamePanel gamePanel = GamePanel.getInstance();
            if (gamePanel != null) {
                gamePanel.setDeplacementMode(true);
            }
        }));

        actions.add(new ButtonAction(("Stop (S)"), e -> {
            stopAction();
        }));

        return actions;
    }


    public java.util.List<ButtonAction> getButtonActionsForMultipleSelection(){
        //return getButtonActions();        //marche génial mais faut raffiner
        List<ButtonAction> actions = new ArrayList<>();

        actions.add(new ButtonAction("Se déplacer (D)", e -> {
            GamePanel gamePanel = GamePanel.getInstance();
            if (gamePanel != null) {
                gamePanel.setDeplacementMode(true);
            }
        }));

        actions.add(new ButtonAction(("Stop (S)"), e -> {
            GamePanel gamePanel = GamePanel.getInstance();
            if (gamePanel != null) {
                for (UniteControlable unit : gamePanel.getUnitesSelected()) {
                    unit.stopAction();
                }
            }
        }));

        return actions;
    }







}

