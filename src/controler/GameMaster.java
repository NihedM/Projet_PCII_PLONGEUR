package controler;

import model.constructions.Base;
import model.objets.*;
import model.unite_non_controlables.Calamar;
import model.unite_non_controlables.Enemy;
import model.unite_non_controlables.Pieuvre;
import model.unite_non_controlables.PieuvreBebe;
import view.GamePanel;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.List;
public class GameMaster extends Thread{
    private volatile static GameMaster instance;
    private static final int DELAY = 30;
    private final ExecutorService enemyExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static final int CELL_SIZE = 2000;
    public static final int GRID_WIDTH = GamePanel.TERRAIN_WIDTH / CELL_SIZE;
    public static final int GRID_HEIGHT = GamePanel.TERRAIN_HEIGHT / CELL_SIZE;

    private ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Enemy>> enemiesGrid;
    private CopyOnWriteArrayList<Enemy> enemies;
    private CopyOnWriteArrayList<Ressource> ressources;

    private static final CoordGrid[][] Grid = new CoordGrid[GRID_WIDTH][GRID_HEIGHT];
    static {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                Grid[x][y] = new CoordGrid(x, y);
            }
        }
    }



    public GameMaster() {
        this.enemiesGrid = new ConcurrentHashMap<>();
        ressources = new CopyOnWriteArrayList<Ressource>();
        enemies = new CopyOnWriteArrayList<Enemy>();
        instance = this;

        updateGrid();

    }


    //----------------------------------------------manip du grid--------------------------------------------------//
    public static CoordGrid getSpatialCell(int x, int y) {
        if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
            return Grid[x][y];
        }
        return null; // Return null or handle out-of-bounds case as needed
    }
    private void updateGrid() {
        enemiesGrid.clear();
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                continue; // sécurité
            }
            CoordGrid cell = getCellForPosition(enemy.getPosition());
            if (cell == null) {
                continue; // sécurité
            }
            if (enemiesGrid.containsKey(cell) ){
                enemiesGrid.get(cell).add(enemy);

            } else {
                CopyOnWriteArrayList<Enemy> objetsAtCoord = new CopyOnWriteArrayList<>();
                objetsAtCoord.add(enemy);
                enemiesGrid.put(cell, objetsAtCoord);
            }

        }
    }
    private CoordGrid getCellForPosition(Position position) {
        int cellX = position.getX() / CELL_SIZE;
        int cellY = position.getY() / CELL_SIZE;

        return getSpatialCell(cellX, cellY);
    }
    private CopyOnWriteArrayList<Enemy> getEnemiesInZone(ZoneEnFonctionnement zone) {
        CopyOnWriteArrayList<Enemy> enemiesInZone = new CopyOnWriteArrayList<>();
        int minCellX = Math.max(0, zone.getMinX() / CELL_SIZE);
        int maxCellX = Math.min(GRID_WIDTH - 1, zone.getMaxX() / CELL_SIZE);
        int minCellY = Math.max(0, zone.getMinY() / CELL_SIZE);
        int maxCellY = Math.min(GRID_HEIGHT - 1, zone.getMaxY() / CELL_SIZE);

        for (int x = minCellX; x <= maxCellX; x++) {
            for (int y = minCellY; y <= maxCellY; y++) {

                CoordGrid cell = getSpatialCell(x, y);
                if (enemiesGrid.containsKey(cell)) {
                    enemiesInZone.addAll(enemiesGrid.get(cell));
                }
            }
        }

        return enemiesInZone;
    }
    public ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Enemy>> getEnemiesGrid() {
        return enemiesGrid;
    }



    //-------------------GETTERS-------------------//

    public static GameMaster getInstance() {
        return instance;
    }

    public CopyOnWriteArrayList<Ressource> getRessources() {
        return ressources;
    }
    public void addResource(Ressource ressource) {ressources.add(ressource);}
    public void setRessourcesVisibilesJoueur(CopyOnWriteArrayList<Ressource> ressources) {
        this.ressources = ressources;
    }

    public CopyOnWriteArrayList<Enemy> getEnemies() {
        return enemies;
    }
    public void addEnemy(Enemy enemy, CopyOnWriteArrayList<Objet> targets) {
        synchronized (enemies) {
            if (enemies.size() >= 500) {
                System.out.println("Limite maximale d'ennemis atteinte. Aucun nouvel ennemi ne peut être ajouté.");
                return;
            }

            this.enemies.add(enemy);
            updateTargets();

        }

        enemy.setup(targets);
        GamePanel.getInstance().addObjet(enemy);
    }
    public void removeEnemy(Enemy enemy) {
        try {
            if(enemies.contains(enemy))
                enemies.remove(enemy);

        } catch (Exception e) {}

        CoordGrid cell = getCellForPosition(enemy.getPosition());
        if (enemiesGrid.containsKey(cell)) {
            enemiesGrid.get(cell).remove(enemy);

            // If the cell is now empty, you can optionally remove it from the grid
            if (enemiesGrid.get(cell).isEmpty()) {
                enemiesGrid.remove(cell);
            }
        }
    }


    public void updateTargets(){
        ZoneEnFonctionnement mainZone = GamePanel.getInstance().getMainZone();
        List<ZoneEnFonctionnement> dynamicZones = GamePanel.getInstance().getDynamicZones();

        // Collect all enemies in active zones
        Set<Enemy> activeEnemies = new HashSet<>();
        activeEnemies.addAll(getEnemiesInZone(mainZone));
        for (ZoneEnFonctionnement zone : dynamicZones) {
            activeEnemies.addAll(getEnemiesInZone(zone));
        }

        // Update targets for active enemies
        for (Enemy enemy : activeEnemies) {
            if (enemy instanceof Calamar) {
                // Update available resources for Calamar
                //((Calamar) enemy).setRessourcesDisponibles(ressources);
            } else if (enemy instanceof Pieuvre || enemy instanceof PieuvreBebe) {
                // Update available targets for Pieuvre and PieuvreBebe
                CopyOnWriteArrayList<UniteControlable> availableTargets = new CopyOnWriteArrayList<>(GamePanel.getInstance().getUnitesEnJeu());
                if (enemy instanceof Pieuvre) {
                    ((Pieuvre) enemy).setTargetsDisponibles(availableTargets);
                } else if (enemy instanceof PieuvreBebe) {
                    Pieuvre parent = ((PieuvreBebe) enemy).getParent();
                    if (parent != null) {
                        ((PieuvreBebe) enemy).setTarget(parent.getTarget());
                    }
                }
            }
        }
    }
    public void reset() {
        ressources.clear();

    }


    @Override
    public void run() {
        ThreadManager.incrementThreadCount("GameMaster");

        Position[] coins = GamePanel.getInstance().getMainBase().getCoints();

        while(true) {
            while (GamePanel.getInstance().isPaused()) {
                try {
                    Thread.sleep(50);  // Petite pause / ralentissement
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            updateGrid();

            // Track processed enemies to avoid redundant checks
            Set<Enemy> processedEnemies = new HashSet<>();

            for (Enemy enemy : getEnemiesInZone(GamePanel.getInstance().getMainZone())) {
                if (!processedEnemies.contains(enemy)) {
                    processedEnemies.add(enemy);
                    enemyExecutor.submit(() -> {

                        if(!GamePanel.getInstance().isWithinTerrainBounds(enemy.getPosition()) || enemy.get_Hp() <= 0||
                                GestionCollisions.estDans(coins[0].getX(), coins[0].getY(), coins[3].getX(), coins[3].getY(), enemy.getPosition().getX(), enemy.getPosition().getY())) {
                            GamePanel.getInstance().killUnite(enemy);
                            return;
                        }

                        if(enemy instanceof Calamar calamar) {
                            calamar.setRessourcesDisponibles(ressources);


                        }


                        enemy.action();
                    });
                }
            }

            for (ZoneEnFonctionnement zone : GamePanel.getInstance().getDynamicZones()) {
                for (Enemy enemy : getEnemiesInZone(zone)) {

                    //if (!processedEnemies.contains(enemy)) {
                        processedEnemies.add(enemy);
                        enemyExecutor.submit(() -> {
                            if(enemy instanceof Calamar)
                             ((Calamar) enemy).setRessourcesDisponibles(ressources);

                            enemy.vadrouille();
                            enemy.action();

                            if (!GamePanel.getInstance().isWithinTerrainBounds(enemy.getPosition())|| enemy.get_Hp() <= 0 ||
                                    GestionCollisions.estDans(coins[0].getX(), coins[0].getY(), coins[3].getX(), coins[3].getY(), enemy.getPosition().getX(), enemy.getPosition().getY())) {
                                GamePanel.getInstance().killUnite(enemy);

                            }

                        });

                }
            }


            for (Enemy enemy : enemies) {
                if (!processedEnemies.contains(enemy)) {
                    //System.out.println("Enemy outside active zones: " + enemy + ", InsideZone: " + enemy.isInsideZone());
                    enemy.stopAction();

                    if (enemy.isInsideZone()) {
                        enemy.stopAllThreads();
                        enemy.setInsideZone(false);
                    }
                }
            }


            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        ThreadManager.decrementThreadCount("GameMaster");
    }
}
