package model.objets.spawns;

import controler.GameMaster;
import controler.ThreadManager;
import model.objets.Position;
import model.unite_non_controlables.Enemy;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import java.util.Arrays;
import java.util.List;

import model.unite_non_controlables.Calamar;
import model.unite_non_controlables.Kraken;
import model.unite_non_controlables.Pieuvre;
import view.GamePanel;

public class EpicSpawnPoint extends EnemySpawnPoint{

    private static int CONSTANT_SPAWN_INTERVAL = 60000;
    private static Random random = new Random();

    private static final List<Class<? extends Enemy>> ALL_ENEMY_TYPES = Arrays.asList(
            Calamar.class,
            Pieuvre.class,
            Kraken.class
    );

    public EpicSpawnPoint(Position pos) {
        super(pos, 0, 200, CONSTANT_SPAWN_INTERVAL);

        setImage("spawnPoint.png");
    }
    @Override
    public boolean canSpawn() {
        return true; // Always allow spawning
    }


    private void spawnEnemy() {
        Position position = generateRandomPositionInsideSpawnPoint();

        try {


            int randomType = random.nextInt(100); // Random number between 0 and 99

            if (randomType < 70) {
                // 70% chance to not spawn anything

            } else if (randomType < 95) {
                // 25% chance to spawn Pieuvre

                Enemy enemy = ALL_ENEMY_TYPES.get(1).getConstructor(Position.class).newInstance(position);
                GameMaster.getInstance().addEnemy(enemy,  new CopyOnWriteArrayList<>(GamePanel.getInstance().getUnitesEnJeu()));
            } else {
                // 5% chance to spawn Kraken depeding on the number of player units
                int playerUnitCount = GamePanel.getInstance().getUnitesEnJeu().size();
                int numberOfKrakens = Math.max(1, random.nextInt(playerUnitCount / 2 + 1)); // Spawn 1 to playerUnitCount/2 Krakens
                for (int j = 0; j < numberOfKrakens; j++) {
                    Position krakenPosition = generateRandomPositionInsideSpawnPoint();
                    Enemy enemy = ALL_ENEMY_TYPES.get(2).getConstructor(Position.class).newInstance(krakenPosition);
                    GameMaster.getInstance().addEnemy(enemy,  new CopyOnWriteArrayList<>(GamePanel.getInstance().getUnitesEnJeu()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        ThreadManager.incrementThreadCount("EpicSpawnPoints");

        while (true) {
            spawnEnemy();

            try {
                Thread.sleep(CONSTANT_SPAWN_INTERVAL); // Constant high spawn frequency
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        ThreadManager.decrementThreadCount("EpicSpawnPoints");
    }
}
