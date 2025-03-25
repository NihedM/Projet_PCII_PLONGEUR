package controler;

import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import view.GamePanel;


public class StaminaRegenHandler extends Thread {
    private static final int REGEN_INTERVAL = 1000; // 1 second
    private static final int STAMINA_INCREMENT = 1; // Amount of stamina to increase per interval
    private static final int STAMINA_DECREMENT = 2; // Amount of stamina to decrease per interval

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
        ThreadManager.incrementThreadCount("StaminaRegenHandler");

        while (true) {
            for (UniteControlable unite : GamePanel.getInstance().getUnitesEnJeu()) {
                if(!(unite instanceof Plongeur)) {
                    continue;
                }
                Plongeur plongeur = (Plongeur) unite;



                if(plongeur.isFaitFuire()){
                    plongeur.setCurrentOxygen(plongeur.getCurrentOxygen() - OxygenHandler.OXYGEN_DECREMENT*3);
                    plongeur.setCurrentStamina(plongeur.getCurrentStamina() - STAMINA_DECREMENT);
                }else  if (plongeur.getDestination() == null)
                    plongeur.setCurrentStamina(plongeur.getCurrentStamina() + STAMINA_INCREMENT);
                else if (plongeur.getDestination() != null)
                    plongeur.setCurrentStamina(plongeur.getCurrentStamina() - STAMINA_DECREMENT);
                else
                    plongeur.setCurrentStamina(plongeur.getCurrentStamina() + STAMINA_DECREMENT);




                if (GamePanel.getInstance().getUnitesSelected().contains(plongeur)) {
                    GamePanel.getInstance().getInfoPanel().updateInfo(plongeur);
                }

            }
            try {
                Thread.sleep(REGEN_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        ThreadManager.decrementThreadCount("StaminaRegenHandler");
    }
}