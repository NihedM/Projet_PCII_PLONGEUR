package controler;

import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import view.GamePanel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.CopyOnWriteArrayList;

public class KeyboardController extends KeyAdapter {
    private final GamePanel panel;
    private final CopyOnWriteArrayList<UniteControlable> unitesSelectionnees;

    public KeyboardController(GamePanel panel, CopyOnWriteArrayList<UniteControlable> unitesSelectionnees) {
        this.panel = panel;
        this.unitesSelectionnees = unitesSelectionnees;
        panel.setFocusable(true);
        panel.requestFocusInWindow();
        panel.addKeyListener(this);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Déplacement de la caméra avec les touches
        int cameraSpeed = 20;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                panel.moveCamera(-cameraSpeed, 0);
                break;
            case KeyEvent.VK_RIGHT:
                panel.moveCamera(cameraSpeed, 0);
                break;
            case KeyEvent.VK_UP:
                panel.moveCamera(0, -cameraSpeed);
                break;
            case KeyEvent.VK_DOWN:
                panel.moveCamera(0, cameraSpeed);
                break;
        }

        if (unitesSelectionnees.isEmpty()) return;

        /*switch (e.getKeyCode()) {
            case KeyEvent.VK_D:
                panel.setDeplacementMode(true);
                break;
            case KeyEvent.VK_R:
                panel.setRecuperationMode(true);
                break;
            case KeyEvent.VK_F:
                UniteControlable unite = unitesSelectionnees.get(0);
                if(unite instanceof Plongeur)
                    ((Plongeur) unite).setFaitFuire(true);
                break;
        }*/
        switch (e.getKeyCode()) {
            case KeyEvent.VK_D -> handleMoveAction();
            case KeyEvent.VK_R -> handleRecoverAction();
            case KeyEvent.VK_A -> handleAttackAction();
            case KeyEvent.VK_F -> handleFleeAction();
            case KeyEvent.VK_S -> handleStopAction();
            case KeyEvent.VK_T -> handleShootAction();
            case KeyEvent.VK_X -> handleDefenceAction();
        }
    }


    private void handleMoveAction() {
        panel.setDeplacementMode(true);
    }

    private void handleRecoverAction() {
        panel.setRecuperationMode(true);
    }

    private void handleAttackAction() {

        panel.setAttackinggMode(true);
    }

    private void handleFleeAction() {

        for (UniteControlable unit : unitesSelectionnees) {
            if (unit instanceof Plongeur) {
                ((Plongeur) unit).setFaitFuire(true);
            }
        }
    }
    private void handleDefenceAction() {
        for (UniteControlable unit : unitesSelectionnees) {
            if (unit instanceof model.unite_controlables.PlongeurArme) {
                ((model.unite_controlables.PlongeurArme) unit).startDefendCircle(unit.getPosition(), 1000);
            }
        }
    }



    private void handleShootAction() {
        handleStopAction();
        for (UniteControlable unit : unitesSelectionnees) {
            if (unit instanceof model.unite_controlables.PlongeurArme) {
                GamePanel.getInstance().setPendingShootAction(true);
                return;
            }
        }

    }


    private void handleStopAction() {
        for (UniteControlable unit : unitesSelectionnees) {
            unit.stopAction();
        }


        GamePanel.getInstance().setPendingShootAction(false);
        panel.setDeplacementMode(false);
        panel.setRecuperationMode(false);
        panel.setAttackinggMode(false);

        System.out.println("All actions interrupted for selected units.");
    }
}