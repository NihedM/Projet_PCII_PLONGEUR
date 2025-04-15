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
    private int spawnInterval;


    public SpawnManager() {
        this.spawnPoints = new CopyOnWriteArrayList<>();
        this.epicSpawnPoints = new CopyOnWriteArrayList<>();
        instance = this;
    }

    public void addSpawnPoint(Position pos, int maxEnemies, int interval) {
        if (GameMaster.getInstance().getEnemies().size() >= 500) {
            System.out.println("Limite maximale d'ennemis atteinte. Aucun nouveau spawn n'est autorisé.");
            return;
        }

        EnemySpawnPoint spawnPoint = new EnemySpawnPoint(pos, maxEnemies, 20, interval);
        int chance = random.nextInt(100);

        // 75% de chance pour Calamar, 25% pour Pieuvre
        if (chance < 75) {
            spawnPoint.setEnemyType(Calamar.class);
        } else {
            spawnPoint.setEnemyType(Pieuvre.class);
        }
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
    public CopyOnWriteArrayList<EpicSpawnPoint> getEpicSpawnPoints() {
        return epicSpawnPoints;
    }



    public boolean isInsideBase(Position pos){
        Position[] coins = GamePanel.getInstance().getMainBase().getCoints();
        return (pos.getX() >= coins[0].getX() && pos.getX() <= coins[1].getX() &&
                pos.getY() >= coins[0].getY() && pos.getY() <= coins[2].getY());
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
                    tY >= 0 && tY < TileManager.nbTilesHeight
                    && !isInsideBase(position)){
                return position;
            }

            attempts++;
        }
        throw new IllegalStateException("Failed to generate a valid position inside the zone after " + maxAttempts + " attempts.");
    }

    public void generateRandomSpawnPoint(int maxEnemies, ZoneEnFonctionnement zone) {
        if (GameMaster.getInstance().getEnemies().size() >= 500) {
            System.out.println("Limite maximale d'ennemis atteinte. Aucun nouveau spawn n'est autorisé.");
            return;
        }


        Position randomPosition = generateRandomPositionInZone(zone);
        int depth = GamePanel.getInstance().getTerrain().getDepthAt(randomPosition.getX(), randomPosition.getY());

        int enemyIntervalMin;
        int enemyIntervalMax;

        switch (depth) {
            case 1: // Profondeur 1
                spawnInterval = 120000; // 120 secondes
                enemyIntervalMin = 60000; // 60 secondes
                enemyIntervalMax = 80000; // 80 secondes
                break;
            case 2: // Profondeur 2
                spawnInterval = 80000; // 80 secondes
                enemyIntervalMin = 50000; // 50 secondes
                enemyIntervalMax = 70000; // 70 secondes
                break;
            case 3: // Profondeur 3
                spawnInterval = 40000; // 40 secondes
                enemyIntervalMin = 30000; // 30 secondes
                enemyIntervalMax = 60000; // 60 secondes
                break;
            case 4: // Profondeur 4
                spawnInterval = 30000; // 30 secondes
                enemyIntervalMin = 10000; // 10 secondes
                enemyIntervalMax = 25000; // 25 secondes
                break;
            default: // Profondeur inconnue
                spawnInterval = 60000; // Valeur par défaut
                enemyIntervalMin = 60000;
                enemyIntervalMax = 80000;
                break;
        }
        int enemyInterval = getRandomInterval(enemyIntervalMin, enemyIntervalMax);
        addSpawnPoint(randomPosition, maxEnemies, enemyInterval );


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

            // Get all zones
                List<ZoneEnFonctionnement> zones = new ArrayList<>(GamePanel.getInstance().getDynamicZones());
                zones.add(GamePanel.getInstance().getMainZone());

                ZoneEnFonctionnement selectedZone = zones.get(random.nextInt(zones.size()));
                generateRandomSpawnPoint(random.nextInt(50) + 1, selectedZone );

            try {
                Thread.sleep(spawnInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        ThreadManager.decrementThreadCount("SpawnManager");

    }


}
