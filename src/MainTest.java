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
import java.util.Random;

public class MainTest {

    private static final int FENETREWIDTH = 800, FENETREHEIGHT = 600;




    public static void main(String[] args) {

        JFrame maFenetre = new JFrame("Collisions");
        maFenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        maFenetre.setResizable(false);
        maFenetre.setSize(FENETREWIDTH, FENETREHEIGHT);
        maFenetre.setLocationRelativeTo(null);




        GamePanel gamePanel = new GamePanel();
        GameMaster gameMaster = new GameMaster();
        AmmoManager ammo = new AmmoManager();
        ammo.start();
        StaminaRegenHandler.getInstance();
        OxygenHandler.getInstance();



        // Configuration des limites de ressources par profondeur
        Terrain terrain = gamePanel.getTerrain();
        terrain.configureDepthZone(1, 20); // Profondeur 1: max 30 ressources
        terrain.configureDepthZone(2, 0); // Profondeur 2: max 50 ressources
        terrain.configureDepthZone(3, 0); // Profondeur 3: max 20 ressources
        terrain.configureDepthZone(4, 0); // Profondeur 4: max 40 ressources




        Base base = gamePanel.getMainBase();
        for(int i = 0; i < 1; i++)
            gamePanel.addUniteControlable(new PlongeurArme(3, new Position(base.getPosition().getX()+ base.getLongueur(), base.getPosition().getY())));

        //for(int i = 0; i < 1; i++)
           // gamePanel.addUniteControlable(new Plongeur(3, new Position(base.getPosition().getX()+ base.getLongueur(), base.getPosition().getY())));

        // Afficher la fenêtre de lancement pour paramétrer la partie
        GameLaunchDialog launchDialog = new GameLaunchDialog(maFenetre);
        launchDialog.setVisible(true);

        // Démarrer le ResourceSpawner
        int maxResources = 500; // Nombre total de ressources à générer
        int spawnIntervalMin = 2000; // Délai minimum entre chaque apparition (1 seconde)
        int spawnIntervalMax = 3000; // Délai maximum entre chaque apparition (3 secondes)
        int spawnCountMin = 1; // Nombre minimum de ressources à générer à chaque intervalle
        int spawnCountMax = 5; // Nombre maximum de ressources à générer à chaque intervalle
        ResourceSpawner resourceSpawner = new ResourceSpawner(gamePanel, maxResources, spawnIntervalMin, spawnIntervalMax, spawnCountMin, spawnCountMax);
        resourceSpawner.start();


        SpawnManager spawnManager = new SpawnManager();
        //spawnManager.start();






        Redessine r = new Redessine();
        maFenetre.add(gamePanel);
        maFenetre.pack();
        maFenetre.setVisible(true);

        // Create and show the thread manager window
        JFrame threadManagerFrame = new JFrame("Thread Manager");
        threadManagerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        threadManagerFrame.setSize(400, 300);
        ThreadManagerPanel threadManagerPanel = new ThreadManagerPanel();
        controler.ThreadManager.setThreadManagerPanel(threadManagerPanel);
        threadManagerFrame.add(threadManagerPanel);
        threadManagerFrame.setVisible(true);

        ThreadManager.startDisplayThread();

//        gameMaster.setRessourcesVisibilesJoueur(gamePanel.getRessources());
//



        gameMaster.start();
        ZoneMover z = new ZoneMover();
        z.start();

        // Puis démarrer le système de victoire
        gamePanel.startGame();

        r.start();


    }
}
