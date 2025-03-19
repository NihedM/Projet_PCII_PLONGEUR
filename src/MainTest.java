import controler.GameMaster;
import controler.SpawnManager;
import controler.ThreadManager;
import controler.TileManager;
import model.objets.CoordGrid;
import model.objets.Position;
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
        GameMaster gameMaster = new GameMaster(gamePanel.getObjetsMap());

        SpawnManager spawnManager = new SpawnManager(gamePanel, gameMaster);
        spawnManager.start();




        for(int i = 0; i < 4; i++)
            gamePanel.addUniteControlable(new Plongeur(3, new Position(), 10));

        ((Plongeur) (gamePanel.getUnitesEnJeu()).get(0)).setFaitFuire(true);
        for(int i = 0; i < 50; i++)
            gamePanel.addObjet(new Collier(new Position()));
        //System.out.println("Unités en jeu : " + gamePanel.getUnitesEnJeu().size());



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

        r.start();


    }
}
