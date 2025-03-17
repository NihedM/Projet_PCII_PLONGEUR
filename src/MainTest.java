import controler.GameMaster;
import controler.ThreadManager;
import model.objets.Objet;
import model.objets.Position;
import model.ressources.Collier;
import model.unite_controlables.Plongeur;
import model.unite_non_controlables.Calamar;
import view.GamePanel;
import view.Redessine;
import view.debeug.ThreadManagerPanel;

import javax.swing.*;
import java.util.ArrayList;

public class MainTest {

    private static final int FENETREWIDTH = 800, FENETREHEIGHT = 600;
    //ajouter classe terrain

    public static void main(String[] args) {

        JFrame maFenetre = new JFrame("Collisions");
        maFenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        maFenetre.setResizable(false);
        maFenetre.setSize(FENETREWIDTH, FENETREHEIGHT);
        maFenetre.setLocationRelativeTo(null);

// Créer un panel overlay pour le glass pane
        // JPanel overlayPanel = new JPanel(new BorderLayout());
        // overlayPanel.setOpaque(false); // Transparent
// Vous pouvez, si vous le souhaitez, ajouter un fond semi-transparent pour l'effet overlay
// overlayPanel.setBackground(new Color(0, 0, 0, 50)); // noir transparent, par exemple

        //   maFenetre.setGlassPane(overlayPanel);
        // overlayPanel.setVisible(false);






        //GamePanel gamePanel = new GamePanel(objets);        //Temporaire, tout nouvelle aparition sera  gérer en dehors du constructeur

        /*TileManager tileManager = new TileManager(20, 20, gamePanel);
        Referee referee = new Referee();*/


        GamePanel gamePanel = new GamePanel();


        //gamePanel.addUniteControlable(new model.unite_controlables.Plongeur(1, new Position(100,100), 10));
        //gamePanel.addUniteControlable(new Plongeur(2, new Position(50,500), 10));
        for(int i = 0; i < 4; i++)
            gamePanel.addUniteControlable(new Plongeur(3, new Position(), 10));
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

        GameMaster gameMaster = new GameMaster(gamePanel.getObjetsMap());
        gameMaster.setRessourcesVisibilesJoueur(gamePanel.getRessources());


        ArrayList<Objet> ressourcesAsObjets = new ArrayList<>(gamePanel.getRessources());


        for(int i = 0; i < 10; i++) {
            Calamar calamar = new Calamar(new Position());

            calamar.setupCalamar(gamePanel.getRessources());
            gamePanel.addObjet(calamar);

            gameMaster.addEnemy(calamar, ressourcesAsObjets);
        }
        gameMaster.start();

        r.start();


    }
}
