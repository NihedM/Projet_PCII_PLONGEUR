package view.debeug;

import controler.GameMaster;
import model.objets.CoordGrid;
import model.objets.Objet;
import model.objets.UniteControlable;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameInfoWindow extends JFrame {
    private ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Objet>> objetsMap;
    private CopyOnWriteArrayList<UniteControlable> unitesEnJeu;
    private  CopyOnWriteArrayList<UniteControlable> unitesSelected;


    private final Timer timer;
    public GameInfoWindow(ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Objet>> objetsMap,
                          CopyOnWriteArrayList<UniteControlable> unitesEnJeu,
                          CopyOnWriteArrayList<UniteControlable> unitesSelected) {
        this.objetsMap = objetsMap;
        this.unitesEnJeu = unitesEnJeu;
        this.unitesSelected = unitesSelected;

        setTitle("Game Information");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add a panel to display the information
        GameInfoPanel infoPanel = new GameInfoPanel();
        add(infoPanel);

        timer = new Timer(100, e -> repaint());
        timer.start();

        setVisible(true);
    }

    private class GameInfoPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int y = 20; // Starting Y position for drawing text


            //display the number of objects in objetsMap
            int totalObjets = 0;
            for (CoordGrid coord : objetsMap.keySet()) {
                totalObjets += objetsMap.get(coord).size();
            }
            g.drawString("Total number of objects in objetsMap: " + totalObjets, 20, y);
            y+=20;

            // Display the size of unitesEnJeu
            g.drawString("Size of unitesEnJeu: " + unitesEnJeu.size(), 20, y);
            y += 20;

            // Display the size of unitesSelected
            g.drawString("Size of unitesSelected: " + unitesSelected.size(), 20, y);
            y += 20;

            g.drawString("Size of Enemies: " + GameMaster.getInstance().getEnemies().size(), 20, y);
            y += 20;
            g.drawString("Size of Ressources: " + GameMaster.getInstance().getRessources().size(), 20, y);
            y += 20;
        }
    }
}

