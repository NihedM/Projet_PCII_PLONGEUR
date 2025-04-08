package controler;

import model.unite_controlables.Plongeur;
import view.GamePanel;
import java.util.concurrent.CopyOnWriteArrayList;

public class FuiteHandler extends GameHandler {
    private final CopyOnWriteArrayList<Plongeur> plongeurs = new CopyOnWriteArrayList<>();
    private static final int DELAY = 1000;
    private static FuiteHandler instance;

    public static synchronized FuiteHandler getInstance() {
        if (instance == null) {
            instance = new FuiteHandler();
            instance.start();
        }
        return instance;
    }

    public void addPlongeur(Plongeur plongeur) {
        if (!plongeurs.contains(plongeur)) {
            plongeurs.add(plongeur);
        }
    }

    public void removePlongeur(Plongeur plongeur) {
        plongeurs.remove(plongeur);
    }

    @Override
    protected void executeHandlerLogic() {
        for (Plongeur plongeur : plongeurs) {
            if (plongeur.getCurrentStamina() > 0) {
                plongeur.setCurrentStamina(plongeur.getCurrentStamina() - 1);
            } else {
                plongeur.setFaitFuire(false);
                removePlongeur(plongeur);
            }

            if (GamePanel.getInstance().getUnitesSelected().contains(plongeur)
            && GamePanel.getInstance().getUnitesSelected().size() == 1) {
                GamePanel.getInstance().getInfoPanel().updateInfo(plongeur);
            }
        }
    }

    @Override
    protected int getDelay() {
        return DELAY;
    }
}