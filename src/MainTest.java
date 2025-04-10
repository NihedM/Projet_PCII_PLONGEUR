import controler.*;
import model.constructions.Base;
import model.objets.CoordGrid;
import model.objets.Position;
import model.objets.ResourceSpawner;
import model.objets.Terrain;
import model.ressources.Collier;
import model.unite_controlables.Plongeur;
import model.unite_controlables.PlongeurArme;
import view.GameLaunchDialog;
import view.GamePanel;
import view.Redessine;
import view.debeug.ThreadManagerPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Random;

public class MainTest {


    public static void main(String[] args) {

        JFrame maFenetre = new JFrame("Jeu");
        maFenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Activer le plein écran

        //JWindow fullScreenWindow = new JWindow();



        GamePanel gamePanel = new GamePanel();
        GameMaster gameMaster = new GameMaster();


        // Configuration des limites de ressources par profondeur
        Terrain terrain = gamePanel.getTerrain();
        terrain.configureDepthZone(1, 20); // Profondeur 1: max 30 ressources
        terrain.configureDepthZone(2, 0); // Profondeur 2: max 50 ressources
        terrain.configureDepthZone(3, 0); // Profondeur 3: max 20 ressources
        terrain.configureDepthZone(4, 0); // Profondeur 4: max 40 ressources

        Base base = gamePanel.getMainBase();
        for(int i = 0; i < 1; i++)
            gamePanel.addUniteControlable(new PlongeurArme(3, new Position(base.getPosition().getX()+ base.getLongueur(), base.getPosition().getY()+100)));
        for(int i = 0; i < 2; i++)
         gamePanel.addUniteControlable(new Plongeur(3, new Position(base.getPosition().getX()+ base.getLongueur()+i*100, base.getPosition().getY())));

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




        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                ammo.start();


                resourceSpawner.start();
                //spawnManager.start();

//        gameMaster.setRessourcesVisibilesJoueur(gamePanel.getRessources());
//
                gameMaster.start();
                z.start();
                gamePanel.startGame();

                Redessine r = new Redessine();
                r.start();
                return null;
            }
        }.execute();


        //-----------------------------------------------------------------------------------------------------------


        maFenetre.add(gamePanel);
        maFenetre.pack();

        GameLaunchDialog launchDialog = new GameLaunchDialog(maFenetre);
        launchDialog.setVisible(true);


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

        maFenetre.setVisible(true);



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
