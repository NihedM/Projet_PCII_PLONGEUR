package controler;

import model.objets.CoordGrid;
import model.objets.EnemySpawnPoint;
import model.objets.Position;
import model.unite_non_controlables.Calamar;
import model.unite_non_controlables.Pieuvre;
import view.GamePanel;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class SpawnManager extends Thread{
    private CopyOnWriteArrayList<EnemySpawnPoint> spawnPoints;
    private GamePanel gamePanel;
    private GameMaster gameMaster;
    private static SpawnManager instance;
    private static Random random = new Random();


    public SpawnManager(GamePanel gamePanel, GameMaster gameMaster) {
        this.spawnPoints = new CopyOnWriteArrayList<>();
        this.gamePanel = gamePanel;
        this.gameMaster = gameMaster;
        instance = this;
    }

    public void addSpawnPoint(CoordGrid tile, int maxEnemies) {
        EnemySpawnPoint spawnPoint = new EnemySpawnPoint(tile, maxEnemies, gamePanel);
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
        int randomX = random.nextInt(TileManager.nbTiles);
        int randomY = random.nextInt(TileManager.nbTiles);
        CoordGrid randomTile = new CoordGrid(randomX, randomY);
        addSpawnPoint(randomTile, maxEnemies);
    }

    @Override
    public void run() {
        ThreadManager.incrementThreadCount("SpawnManager");

        while (true) {
            generateRandomSpawnPoint(10);
            try {
                Thread.sleep(getRandomInterval(10000, 20000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        ThreadManager.decrementThreadCount("SpawnManager");

    }


}
