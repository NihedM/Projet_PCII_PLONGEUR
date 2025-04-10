package controler;

import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import view.GamePanel;

public class OxygenHandler extends Thread {
    private static final int DELAY = 3000;
    public static final int OXYGEN_DECREMENT = 1;
    public static final int OXYGEN_INCREMENT = 1;
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
        while (true) {
            try {
                for (UniteControlable unite : GamePanel.getInstance().getUnitesEnJeu()) {
                    if (!(unite instanceof Plongeur)) continue;

                    Plongeur plongeur = (Plongeur) unite;
                    plongeur.setCurrentOxygen(plongeur.getCurrentOxygen() - OXYGEN_DECREMENT);

                    if (plongeur.getCurrentOxygen() <= 0) {

                        GamePanel.getInstance().killUnite(plongeur);
                        GamePanel.getInstance().showEmptyInfoPanel();


                    }
                    if (GamePanel.getInstance().getUnitesSelected().contains(plongeur)
                    && GamePanel.getInstance().getUnitesSelected().size() == 1) {
                        GamePanel.getInstance().getInfoPanel().getAtributInfo().updateInfo(plongeur.getAttributes());
                        //GamePanel.getInstance().getInfoPanel().getAtributInfo().repaint();
                    }
                }
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}