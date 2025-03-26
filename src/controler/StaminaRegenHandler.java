package controler;

import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import view.GamePanel;

public class StaminaRegenHandler extends GameHandler {
    private static final int DELAY = 1000;
    private static final int STAMINA_INCREMENT = 1;
    private static final int STAMINA_DECREMENT = 2;
    private static StaminaRegenHandler instance;

    public static synchronized StaminaRegenHandler getInstance() {
        if (instance == null) {
            instance = new StaminaRegenHandler();
            instance.start();
        }
        return instance;
    }

    @Override
    protected void executeHandlerLogic() {
        for (UniteControlable unite : GamePanel.getInstance().getUnitesEnJeu()) {
            if (!(unite instanceof Plongeur)) continue;

            Plongeur plongeur = (Plongeur) unite;
            updateStamina(plongeur);

            if (GamePanel.getInstance().getUnitesSelected().contains(plongeur)) {
                GamePanel.getInstance().getInfoPanel().updateInfo(plongeur);
            }
        }
    }

    private void updateStamina(Plongeur plongeur) {
        if (plongeur.isFaitFuire()) {
            plongeur.setCurrentOxygen(plongeur.getCurrentOxygen() - OxygenHandler.OXYGEN_DECREMENT * 3);
            plongeur.setCurrentStamina(plongeur.getCurrentStamina() - STAMINA_DECREMENT);
        } else if (plongeur.getDestination() == null) {
            plongeur.setCurrentStamina(plongeur.getCurrentStamina() + STAMINA_INCREMENT);
        } else {
            plongeur.setCurrentStamina(plongeur.getCurrentStamina() - STAMINA_DECREMENT);
        }
    }

    @Override
    protected int getDelay() {
        return DELAY;
    }
}