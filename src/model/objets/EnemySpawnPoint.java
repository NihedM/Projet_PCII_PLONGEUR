package model.objets;

import controler.*;
import model.objets.CoordGrid;
import model.objets.Objet;
import model.objets.Position;
import model.unite_non_controlables.Calamar;
import model.unite_non_controlables.Enemy;
import model.unite_non_controlables.Pieuvre;
import model.unite_non_controlables.PieuvreBebe;
import view.GamePanel;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class EnemySpawnPoint extends Objet implements Runnable {

    private int maxEnemies;
    private int spawnedEnemies;
    private static final int RAYON =200;

    private Class<? extends Enemy> enemyType;


    public EnemySpawnPoint(CoordGrid tile, int maxEnemies) {
        super(generateRandomPositionInTile(tile), RAYON);
        this.maxEnemies = maxEnemies;
        this.spawnedEnemies = 0;
        this.enemyType = Calamar.class; // Default
    }
    public static Position generateRandomPositionInTile(CoordGrid tile) {
        int cellSize = GameMaster.CELL_SIZE;
        int x = tile.getX() * cellSize + (int) (Math.random() * cellSize);
        int y = tile.getY() * cellSize + (int) (Math.random() * cellSize);
        return new Position(x, y);
    }

    public Position generateRandomPositionInsideSpawnPoint(){
        //generate a random position inside the spawn point of rayon 200
        int x = (int) (Math.random() * (2 * RAYON)) - RAYON;
        int y = (int) (Math.random() * (2 * RAYON)) - RAYON;
        return new Position(getPosition().getX() + x, getPosition().getY() + y);
    }

    public boolean canSpawn() {
        return spawnedEnemies < maxEnemies;
    }

    public void incrementSpawnedEnemies() {
        spawnedEnemies++;
    }

    public void setEnemyType(Class<? extends Enemy> enemyType) {
        this.enemyType = enemyType;
    }

    private void spawnEnemy() {
        incrementSpawnedEnemies();
        //generate random position in tile
        //Position position = EnemySpawnPoint.generateRandomPositionInTile(getCoordGrid());

        Position position = generateRandomPositionInsideSpawnPoint();

        //if ( !ZoneMover.isInsideAnyZone(position)) return;

        try {
            Enemy enemy = enemyType.getConstructor(Position.class).newInstance(position);
            if (enemy instanceof Calamar) {
                GameMaster.getInstance().addEnemy(enemy, new CopyOnWriteArrayList<>(GamePanel.getInstance().getRessources()));

            } else if (enemy instanceof Pieuvre) {
                GameMaster.getInstance().addEnemy(enemy, new CopyOnWriteArrayList<>(GamePanel.getInstance().getUnitesEnJeu()));
                //generate random number of children
                int nbChildren = (int) (Math.random() * 5) + 1;
                //for (int i = 0; i < nbChildren; i++)
                 // ((Pieuvre)enemy).addChild();
            }
            else{
                //throw genereic error
                throw new UnsupportedOperationException("Enemy type not supported");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    @Override
    public void run() {
        ThreadManager.incrementThreadCount("EnemySpawnPoints");

        while (canSpawn()) {
            spawnEnemy();

            try {
                Thread.sleep(SpawnManager.getRandomInterval(1000,5000)); // Random interval between 1 and 5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        SpawnManager.getInstance().getSpawnPoints().remove(this);
        ThreadManager.decrementThreadCount("EnemySpawnPoints");
    }
}
