package controler;

import model.objets.CoordGrid;
import model.objets.EnemySpawnPoint;
import model.objets.Position;
import model.unite_non_controlables.Calamar;
import model.unite_non_controlables.Pieuvre;
import model.unite_non_controlables.PieuvreBebe;
import view.GamePanel;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class SpawnManager extends Thread{
    private CopyOnWriteArrayList<EnemySpawnPoint> spawnPoints;
    private static SpawnManager instance;
    private static Random random = new Random();


    public SpawnManager() {
        this.spawnPoints = new CopyOnWriteArrayList<>();
        instance = this;
    }

    public void addSpawnPoint(CoordGrid tile, int maxEnemies) {
        EnemySpawnPoint spawnPoint = new EnemySpawnPoint(tile, maxEnemies);
        spawnPoint.setEnemyType(Pieuvre.class);
        spawnPoints.add(spawnPoint);
        new Thread(spawnPoint).start();
    }

    public static SpawnManager getInstance() {
        return instance;
    }

    public static int getRandomInterval(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public CopyOnWriteArrayList<EnemySpawnPoint> getSpawnPoints() {
        return spawnPoints;
    }




    public void generateRandomSpawnPoint(int maxEnemies) {
        int randomX = random.nextInt(TileManager.nbTilesWidth);
        int randomY = random.nextInt(TileManager.nbTilesHeight);
        CoordGrid randomTile = new CoordGrid(randomX, randomY);
        Position randomPosition = EnemySpawnPoint.generateRandomPositionInTile(randomTile);
        if (ZoneMover.isInsideAnyZone(randomPosition)) {
            addSpawnPoint(randomTile, maxEnemies);
        }
    }





    private int cpt = 0;



    @Override
    public void run() {
        ThreadManager.incrementThreadCount("SpawnManager");

        while (true) {
            //if(cpt == 0){
                generateRandomSpawnPoint(20);
                cpt++;
            //}
            try {
                Thread.sleep(getRandomInterval(100, 200));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        ThreadManager.decrementThreadCount("SpawnManager");

    }


}
