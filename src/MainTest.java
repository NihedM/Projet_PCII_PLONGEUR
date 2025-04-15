import controler.*;
import model.constructions.Base;
import model.objets.Objet;
import model.objets.Position;
import model.objets.spawns.ResourceSpawner;
import model.objets.Terrain;
import model.unite_controlables.Plongeur;
import model.unite_controlables.PlongeurArme;
import model.unite_controlables.SousMarin;
import model.unite_non_controlables.Kraken;
import model.unite_non_controlables.SeaSerpent;
import view.GameLaunchDialog;
import view.GamePanel;
import view.Redessine;

import javax.swing.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainTest {


    public static void main(String[] args) {

        JFrame maFenetre = new JFrame("Jeu");
        maFenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Activer le plein écran

        //JWindow fullScreenWindow = new JWindow();


        GameMaster gameMaster = new GameMaster();
        GamePanel gamePanel = new GamePanel();



        // Configuration des limites de ressources par profondeur
        Terrain terrain = gamePanel.getTerrain();
        terrain.configureDepthZone(1, 20); // Profondeur 1: max 30 ressources
        terrain.configureDepthZone(2, 50); // Profondeur 2: max 50 ressources
        terrain.configureDepthZone(3, 20); // Profondeur 3: max 20 ressources
        terrain.configureDepthZone(4, 40); // Profondeur 4: max 40 ressources

        Base base = gamePanel.getMainBase();
        //plongeur initial

        gamePanel.addUniteControlable(new Plongeur(0, new Position(base.getPosition().getX() + base.getLongueur() + 100, base.getPosition().getY())));




        AmmoManager ammo = new AmmoManager();

        // Démarrer le ResourceSpawner
        int maxResources = 500; // Nombre total de ressources à générer
        int spawnIntervalMin = 2000; // Délai minimum entre chaque apparition (1 seconde)
        int spawnIntervalMax = 3000; // Délai maximum entre chaque apparition (3 secondes)
        int spawnCountMin = 1; // Nombre minimum de ressources à générer à chaque intervalle
        int spawnCountMax = 5; // Nombre maximum de ressources à générer à chaque intervalle
        ResourceSpawner resourceSpawner = new ResourceSpawner(gamePanel, maxResources, spawnIntervalMin, spawnIntervalMax, spawnCountMin, spawnCountMax);
        SpawnManager spawnManager = new SpawnManager();

        ZoneMover z = new ZoneMover();

        StaminaRegenHandler.getInstance();
        OxygenHandler.getInstance();



        maFenetre.add(gamePanel);
        maFenetre.pack();

        GameLaunchDialog launchDialog = new GameLaunchDialog(maFenetre);
        launchDialog.setVisible(true);

        maFenetre.setVisible(true);



        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                gameMaster.start();

                ammo.start();

                resourceSpawner.start();
                spawnManager.start();

//        gameMaster.setRessourcesVisibilesJoueur(gamePanel.getRessources());

                z.start();
                gamePanel.startGame();
                Redessine r = new Redessine();
                r.start();
                return null;
            }
        }.execute();


        //-----------------------------------------------------------------------------------------------------------




       /* fullScreenWindow.getContentPane().add(gamePanel);
        fullScreenWindow.pack();

        // Get the default screen device and enable full-screen mode
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            // Set the window as full-screen
            gd.setFullScreenWindow(fullScreenWindow);
        } else {
            // Fall back to maximized window mode (may reveal taskbar)
            fullScreenWindow.setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
            fullScreenWindow.setVisible(true);
        }

        // Force focus and layout validation to avoid layout issues
        fullScreenWindow.requestFocus();
        fullScreenWindow.validate();*/




        // Create and show the thread manager window
  /*      JFrame threadManagerFrame = new JFrame("Thread Manager");
        threadManagerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        threadManagerFrame.setSize(400, 300);
        ThreadManagerPanel threadManagerPanel = new ThreadManagerPanel();
        controler.ThreadManager.setThreadManagerPanel(threadManagerPanel);
        threadManagerFrame.add(threadManagerPanel);
        threadManagerFrame.setVisible(true);

        ThreadManager.startDisplayThread();
*/



    }
}
