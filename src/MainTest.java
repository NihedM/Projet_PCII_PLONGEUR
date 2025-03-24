import controler.GameMaster;
import controler.SpawnManager;
import controler.ThreadManager;
import controler.TileManager;
import model.objets.CoordGrid;
import model.objets.Position;
import model.objets.ResourceSpawner;
import model.ressources.Collier;
import model.unite_controlables.Plongeur;
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

        // Initialisation des unités
        for(int i = 0; i < 1; i++) {
            gamePanel.addUniteControlable(new Plongeur(3, new Position(), 10));
        }
        ((Plongeur)(gamePanel.getUnitesEnJeu()).get(0)).setFaitFuire(true);

        // Initialisation du ResourceSpawner
        ResourceSpawner resourceSpawner = new ResourceSpawner(
                gamePanel,
                50,         // maxResources
                2000,       // spawnIntervalMin
                3000,       // spawnIntervalMax
                1,          // spawnCountMin
                5           // spawnCountMax
        );
        resourceSpawner.start();

        // Initialisation du SpawnManager
        SpawnManager spawnManager = new SpawnManager(gamePanel, gameMaster);
        spawnManager.start();

        // Configuration de la fenêtre
        maFenetre.add(gamePanel);
        maFenetre.pack();
        maFenetre.setVisible(true);

        // Démarrer le GameMaster EN PREMIER
        gameMaster.start();

        // Puis démarrer le système de victoire
        gamePanel.startGame();

        // Initialisation de l'affichage
        Redessine r = new Redessine();
        r.start();

        // Fenêtre de debug (ThreadManager)
        JFrame threadManagerFrame = new JFrame("Thread Manager");
        threadManagerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        threadManagerFrame.setSize(400, 300);
        ThreadManagerPanel threadManagerPanel = new ThreadManagerPanel();
        ThreadManager.setThreadManagerPanel(threadManagerPanel);
        threadManagerFrame.add(threadManagerPanel);
        threadManagerFrame.setVisible(true);

        ThreadManager.startDisplayThread();
    }
}