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



    public void spawnPieuvreSwarm(CoordGrid tile, int nbChilds){

        Position parentPosition = EnemySpawnPoint.generateRandomPositionInTile(tile);
        Pieuvre parentPieuvre = new Pieuvre(parentPosition);
        gameMaster.addEnemy(parentPieuvre, new CopyOnWriteArrayList<>());
        // Create the swarm of PieuvreBebe
        for (int i = 0; i < nbChilds; i++) {
            // Generate a random position around the parent Pieuvre
            int offsetX = (random.nextInt(2 * TileManager.TILESIZE) - TileManager.TILESIZE);
            int offsetY = (random.nextInt(2 * TileManager.TILESIZE) - TileManager.TILESIZE);
            Position childPosition = new Position(parentPosition.getX() + offsetX, parentPosition.getY() + offsetY);

            // Ensure the new position is not too close to the parent
            while (Math.abs(offsetX) < TileManager.TILESIZE && Math.abs(offsetY) < TileManager.TILESIZE) {
                offsetX = (random.nextInt(2 * TileManager.TILESIZE) - TileManager.TILESIZE);
                offsetY = (random.nextInt(2 * TileManager.TILESIZE) - TileManager.TILESIZE);
                childPosition = new Position(parentPosition.getX() + offsetX, parentPosition.getY() + offsetY);
            }

            PieuvreBebe childPieuvre = new PieuvreBebe(childPosition, parentPieuvre);
            gameMaster.addEnemy(childPieuvre, new CopyOnWriteArrayList<>());
        }

    }


    public void generateRandomSpawnPoint(int maxEnemies) {
        int randomX = random.nextInt(TileManager.nbTilesWidth);
        int randomY = random.nextInt(TileManager.nbTilesHeight);
        CoordGrid randomTile = new CoordGrid(randomX, randomY);
        addSpawnPoint(randomTile, maxEnemies);
    }




    private int cpt = 0;


    @Override
    public void run() {
        ThreadManager.incrementThreadCount("SpawnManager");

        while (true) {
            if(cpt == 0){
                //generateRandomSpawnPoint(10);
                spawnPieuvreSwarm(new CoordGrid(5, 5), 5);
                cpt++;
            }
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
