package model.objets.spawns;

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
    private  final int RAYON;
    private int intervalNouveauEnemie;

    private Class<? extends Enemy> enemyType;


    public EnemySpawnPoint(Position pos, int maxEnemies, int rayon, int interval) {
        super(pos, rayon);
        this.RAYON = rayon;
        this.maxEnemies = maxEnemies;
        this.spawnedEnemies = 0;
        this.enemyType = Calamar.class; // Default
        this.intervalNouveauEnemie= interval;
    }


    public Position generateRandomPositionInsideSpawnPoint(){
        int attempts = 0;
        int maxAttempts = 100;

        while (attempts < maxAttempts) {
            int x = (int) (Math.random() * (2 * RAYON)) - RAYON;
            int y = (int) (Math.random() * (2 * RAYON)) - RAYON;
            Position position = new Position(getPosition().getX() + x, getPosition().getY() + y);

            int tX = TileManager.transformeP_to_grid(position.getX());
            int tY = TileManager.transformeP_to_grid(position.getY());

            // Validate the position
            if (GamePanel.getInstance().isWithinTerrainBounds(position) &&
                    tX >= 0 && tX < TileManager.nbTilesWidth &&
                    tY >= 0 && tY < TileManager.nbTilesHeight){
                return position;
            }

            attempts++;
        }    throw new IllegalStateException("Failed to generate a valid position inside the spawn point.");


    }

    public boolean canSpawn() {
        return spawnedEnemies < maxEnemies;
    }

    public void incrementSpawnedEnemies() {
        spawnedEnemies++;
    }

    public void setEnemyType(Class<? extends Enemy> enemyType) {
        if(enemyType == Calamar.class)
            this.intervalNouveauEnemie = intervalNouveauEnemie*3/4;
        this.enemyType = enemyType;
    }

    private void spawnEnemy() {
        incrementSpawnedEnemies();

        Position position = generateRandomPositionInsideSpawnPoint();


        try {
            Enemy enemy = enemyType.getConstructor(Position.class).newInstance(position);
            if (enemy instanceof Calamar) {
                GameMaster.getInstance().addEnemy(enemy, GamePanel.getInstance().getRessources());

            } else if (enemy instanceof Pieuvre) {
                GameMaster.getInstance().addEnemy(enemy, new CopyOnWriteArrayList<>(GamePanel.getInstance().getUnitesEnJeu()));
                //generate random number of children
                int nbChildren = (int) (Math.random() * 5) + 1;
                for (int i = 0; i < nbChildren; i++)
                    ((Pieuvre)enemy).addChild();
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
                Thread.sleep(intervalNouveauEnemie); // Random interval between 1 and 5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        SpawnManager.getInstance().getSpawnPoints().remove(this);
        ThreadManager.decrementThreadCount("EnemySpawnPoints");
    }
}
