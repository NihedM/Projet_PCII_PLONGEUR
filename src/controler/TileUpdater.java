package controler;

import model.objets.*;
import view.GamePanel;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TileUpdater extends Thread{

    private final ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Objet>> objetsMap;
    private volatile boolean running = true;
    private final ExecutorService executor;



    public TileUpdater(ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Objet>> objetsMap) {
        this.objetsMap = objetsMap;

        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }


    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {

        ThreadManager.incrementThreadCount("TileUpdater");
        //view.GamePanel.printGridContents(objetsMap);

        while(running) {

            // on update les objets dans les zones de fonctionnement
            List<Objet> objets = objetsMap.values().stream()
                    .flatMap(List::stream)
                    //.filter(objet -> GamePanel.getInstance().getMainZone().isInsideMain(objet.getPosition()))
                    .toList();

            int batchSize = Math.max(1, objets.size() / Runtime.getRuntime().availableProcessors());

            for (int i = 0; i < objets.size(); i += batchSize) {
                int start = i;
                int end = Math.min(i + batchSize, objets.size());
                ThreadManager.incrementThreadCount("TileUpdaterWorker");
                executor.submit(() -> {
                    try {
                        updateBatch(objets.subList(start, end));
                    } finally {
                        ThreadManager.decrementThreadCount("TileUpdaterWorker");
                    }
                });
            }

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
            //printCoordonnees();
        ThreadManager.decrementThreadCount("TileUpdater");

    }

    private void updateBatch(List<Objet> objets) {
        for (Objet objet : objets) {

            if (!(objet instanceof Unite)) continue;

            CoordGrid oldCoord = objet.getCoordGrid();
            objet.updatePosition();
            CoordGrid newCoord = objet.getCoordGrid();

            if (!oldCoord.equals(newCoord)) {
                GamePanel.getInstance().removeObjet(objet, oldCoord);
                GamePanel.getInstance().addObjet(objet);
            }
        }
    }


    private void printCoordonnees() {


        // Print out the coordinates of all the objects in the map
        for (ConcurrentHashMap.Entry<CoordGrid, CopyOnWriteArrayList<Objet>> entry : objetsMap.entrySet()) {
            CoordGrid coord = entry.getKey();
            List<Objet> objetsDansTile = entry.getValue();
            //print coord.x, coord.y and the list of objects in the tile
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

