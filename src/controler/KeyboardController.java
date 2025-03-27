package controler;

import model.objets.UniteControlable;
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

        switch (e.getKeyCode()) {
            case KeyEvent.VK_D:
                panel.setDeplacementMode(true);
                break;
            case KeyEvent.VK_R:
                panel.setRecuperationMode(true);
                break;
        }
    }
}