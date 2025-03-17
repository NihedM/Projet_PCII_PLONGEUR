package controler;

import model.objets.CoordGrid;
import model.objets.Objet;
import view.GamePanel;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TileUpdater extends Thread{

    private final ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Objet>> objetsMap;

    public TileUpdater(ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Objet>> objetsMap) {
        this.objetsMap = objetsMap;

    }

    @Override
    public void run() {

        ThreadManager.incrementThreadCount("TileUpdater");
        view.GamePanel.printGridContents(objetsMap);

        while (true) {
            synchronized (objetsMap) {
                // Clear the terminal screen
                //clearScreen();


                //System.out.println("Nombre d'objets dans Map: " + objetsMap.size());
                for (Objet objet : objetsMap.values().stream().flatMap(List::stream).toList()) {


                    CoordGrid oldCoord = objet.getCoordGrid();

                    objet.updatePosition();

                    CoordGrid newCoord = objet.getCoordGrid();

                    if (!oldCoord.equals(newCoord)) {
                        view.GamePanel.getInstance().removeObjet(objet, oldCoord);
                        GamePanel.getInstance().addObjet(objet);


                    }


                    /*//System.out.println("Tile (" + coord.getX() + ", " + coord.getY() + "):");

                    GamePanel.getInstance().removeObjet(objet);
                    objet.updatePosition();


                    // System.out.println("Tile (" + coord.getX() + ", " + coord.getY() + "):");

                    GamePanel.getInstance().addObjet(objet);*/


                }
            }

            //printCoordonnees();


            // Pause avant la prochaine mise à jour
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        ThreadManager.decrementThreadCount("TileUpdater");

    }


    private void printCoordonnees() {


        // Print out the coordinates of all the objects in the map
        for (ConcurrentHashMap.Entry<CoordGrid, CopyOnWriteArrayList<Objet>> entry : objetsMap.entrySet()) {
            CoordGrid coord = entry.getKey();
            List<Objet> objetsDansTile = entry.getValue();
            /*print coord.x, coord.y and the list of objects in the tile*/
            System.out.println("Tile (" + coord.getX() + ", " + coord.getY() + "):");
            for (Objet objet : objetsDansTile) {
                System.out.println("  - " + objet.getClass().getSimpleName() + " at (" + objet.getPosition().getX() + ", " + objet.getPosition().getY() + ")");
            }
        }
    }


    private void clearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

}

