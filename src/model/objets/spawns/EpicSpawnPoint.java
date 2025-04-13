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
import model.unite_non_controlables.Pieuvre;
import view.GamePanel;

public class EpicSpawnPoint extends EnemySpawnPoint{

    private static int CONSTANT_SPAWN_INTERVAL = 12000;
    private static Random random = new Random();

    private static final List<Class<? extends Enemy>> ALL_ENEMY_TYPES = Arrays.asList(
            Calamar.class,
            Pieuvre.class
    );

    public EpicSpawnPoint(Position pos) {
        super(pos, 0, 200);

        setImage("spawnPoint.png");
    }
    @Override
    public boolean canSpawn() {
        return true; // Always allow spawning
    }


    private void spawnEnemy() {
        Position position = generateRandomPositionInsideSpawnPoint();

        try {
            // Randomly select an enemy type
            Class<? extends Enemy> enemyType = ALL_ENEMY_TYPES.get(random.   nextInt(ALL_ENEMY_TYPES.size()));

            // Create the enemy based on the selected type
            Enemy enemy = enemyType.getConstructor(Position.class).newInstance(position);

            // Add the enemy to the game
            GameMaster.getInstance().addEnemy(enemy, new CopyOnWriteArrayList<>(GamePanel.getInstance().getRessources()));
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
