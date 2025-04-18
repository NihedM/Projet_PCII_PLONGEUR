package controler;

import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import view.GamePanel;

public class StaminaRegenHandler extends Thread {
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
    public void run() {
        while (true) {
            while (GamePanel.getInstance().isPaused()) {
                try {
                    Thread.sleep(50);  // Petite pause / ralentissement
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            try {
                for (UniteControlable unite : GamePanel.getInstance().getUnitesEnJeu()) {
                    if (!(unite instanceof Plongeur)) continue;

                    Plongeur plongeur = (Plongeur) unite;
                    updateStamina(plongeur);

                    if (GamePanel.getInstance().getInfoPanel().getAtributInfo()!= null &&
                            GamePanel.getInstance().getUnitesSelected().contains(plongeur)
                            && GamePanel.getInstance().getUnitesSelected().size() == 1) {
                        GamePanel.getInstance().getInfoPanel().getAtributInfo().updateInfo(plongeur.getAttributes());
                    }
                }

                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
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

        if (GamePanel.getInstance().getUnitesSelected().contains(plongeur)
                && GamePanel.getInstance().getUnitesSelected().size() == 1
                && GamePanel.getInstance().getInfoPanel().getAtributInfo() != null) {
            GamePanel.getInstance().getInfoPanel().getAtributInfo().updateInfo(plongeur.getAttributes());
            //GamePanel.getInstance().getInfoPanel().getAtributInfo().repaint();
        }
    }

}