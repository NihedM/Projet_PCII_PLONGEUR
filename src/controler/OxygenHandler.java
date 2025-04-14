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

                    int depth = GamePanel.getInstance().getTerrain().getDepthAt(
                            plongeur.getPosition().getX(),
                            plongeur.getPosition().getY()
                    );

                    int oxygenDecrement = calculateOxygenDecrement(depth);
                    plongeur.setCurrentOxygen(plongeur.getCurrentOxygen() - oxygenDecrement);


                    if (plongeur.getCurrentOxygen() <= 0) {

                        GamePanel.getInstance().killUnite(plongeur);
                        GamePanel.getInstance().showEmptyInfoPanel();
                        continue;
                    }
                    if (GamePanel.getInstance().getUnitesSelected().contains(plongeur)
                    && GamePanel.getInstance().getUnitesSelected().size() == 1) {
                        if(GamePanel.getInstance().getInfoPanel().getAtributInfo() != null)
                            GamePanel.getInstance().getInfoPanel().getAtributInfo().updateInfo(plongeur.getAttributes());
                    }
                }
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private int calculateOxygenDecrement(int depth) {
        switch (depth) {
            case 1: return OXYGEN_DECREMENT; // Shallow depth
            case 2: return 2*OXYGEN_DECREMENT; // Moderate depth
            case 3: return 3*OXYGEN_DECREMENT; // Deep depth
            case 4: return 4*OXYGEN_DECREMENT; // Very deep
            default: return 1*OXYGEN_DECREMENT; // Default decrement if depth is unknown
        }
    }

}