package controler;

import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import view.GamePanel;

public class OxygenHandler extends Thread {
    private static final int DECREMENT_INTERVAL = 3000; // 1 second
    public static final int OXYGEN_DECREMENT = 1; // Amount of oxygen to decrease per interval

    private static OxygenHandler instance;

    public static synchronized OxygenHandler getInstance() {
        if (instance == null) {
            instance = new OxygenHandler();
            instance.start();
        }
        return instance;
    }

    @Override
    public void run() {
        ThreadManager.incrementThreadCount("OxygenHandler");

        while (true) {
            for (UniteControlable unite : GamePanel.getInstance().getUnitesEnJeu()) {
                if (!(unite instanceof Plongeur)) {
                    continue;
                }
                Plongeur plongeur = (Plongeur) unite;

                plongeur.setCurrentOxygen(plongeur.getCurrentOxygen() - OXYGEN_DECREMENT);

                if (plongeur.getCurrentOxygen() <= 0) {
                    GamePanel.getInstance().removeObjet(plongeur, plongeur.getCoordGrid());
                    GamePanel.getInstance().showEmptyInfoPanel();
                }
            }
            try {
                Thread.sleep(DECREMENT_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        ThreadManager.decrementThreadCount("OxygenHandler");
    }
}