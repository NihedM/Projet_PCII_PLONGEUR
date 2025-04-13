package controler;

import model.objets.spawns.EnemySpawnPoint;
import model.objets.Position;
import model.objets.spawns.EpicSpawnPoint;
import model.unite_non_controlables.Calamar;
import model.unite_non_controlables.Pieuvre;
import view.GamePanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class SpawnManager extends Thread{
    private CopyOnWriteArrayList<EnemySpawnPoint> spawnPoints;
    private CopyOnWriteArrayList<EpicSpawnPoint> epicSpawnPoints;
    private static SpawnManager instance;
    private static Random random = new Random();
    private static int randominterval = 10000;


    public SpawnManager() {
        this.spawnPoints = new CopyOnWriteArrayList<>();
        this.epicSpawnPoints = new CopyOnWriteArrayList<>();
        instance = this;
    }

    public void addSpawnPoint(Position pos, int maxEnemies) {
        EnemySpawnPoint spawnPoint = new EnemySpawnPoint(pos, maxEnemies, 20);
        spawnPoint.setEnemyType(random.nextBoolean() ? Calamar.class : Pieuvre.class);
        spawnPoints.add(spawnPoint);
        new Thread(spawnPoint).start();
    }

    public static SpawnManager getInstance() {
        return instance;
    }

    public static int getRandomInterval() {
        return randominterval;
    }

    public CopyOnWriteArrayList<EnemySpawnPoint> getSpawnPoints() {
        return spawnPoints;
    }
    public CopyOnWriteArrayList<EpicSpawnPoint> getEpicSpawnPoints() {
        return epicSpawnPoints;
    }




    private Position generateRandomPositionInZone(ZoneEnFonctionnement zone) {
        int attempts = 0;
        int maxAttempts = 100;

        while (attempts < maxAttempts) {
            int x = random.nextInt(zone.getMaxX() - zone.getMinX() + 1) + zone.getMinX();
            int y = random.nextInt(zone.getMaxY() - zone.getMinY() + 1) + zone.getMinY();
            Position position = new Position(x, y);

            int tX = TileManager.transformeP_to_grid(position.getX());
            int tY = TileManager.transformeP_to_grid(position.getY());

            // Validate the position
            if (GamePanel.getInstance().isWithinTerrainBounds(position) &&
                    tX >= 0 && tX < TileManager.nbTilesWidth &&
                    tY >= 0 && tY < TileManager.nbTilesHeight) {
                return position;
            }

            attempts++;
        }
        throw new IllegalStateException("Failed to generate a valid position inside the zone after " + maxAttempts + " attempts.");
    }

    public void generateRandomSpawnPoint(int maxEnemies) {
        // Get all zones
        List<ZoneEnFonctionnement> zones = new ArrayList<>(GamePanel.getInstance().getDynamicZones());
        zones.add(GamePanel.getInstance().getMainZone());

        ZoneEnFonctionnement selectedZone = zones.get(random.nextInt(zones.size()));
        Position randomPosition = generateRandomPositionInZone(selectedZone);
        int depth = GamePanel.getInstance().getTerrain().getDepthAt(randomPosition.getX(), randomPosition.getY());

        randominterval = Math.max(60000 - depth * 1000, 20000);

        addSpawnPoint(randomPosition, maxEnemies);

    }

    public void addEpicSpawnPoint(Position pos) {
        EpicSpawnPoint epicSpawnPoint = new EpicSpawnPoint(pos);


        spawnPoints.add(epicSpawnPoint);
        epicSpawnPoints.add(epicSpawnPoint);
        new Thread(epicSpawnPoint).start();
    }

    public void epicSpawnPoints() {
        List<Position> epicPositions = List.of(
                new Position(2000, 2000),
                new Position(1000, 8000),
                new Position(5000, 5000),
                new Position(8000, 1000),
                new Position(9000, 9000)
        );

        for (Position position : epicPositions) {
            addEpicSpawnPoint(position);
        }

    }



        @Override
    public void run() {
        ThreadManager.incrementThreadCount("SpawnManager");

        epicSpawnPoints();

        while (true) {
                generateRandomSpawnPoint(10);

            try {
                Thread.sleep(getRandomInterval());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        ThreadManager.decrementThreadCount("SpawnManager");

    }


}
