package model.objets;

import controler.GameMaster;
import controler.ThreadManager;
import controler.ZoneMover;
import model.objets.Position;
import model.objets.GestionRessource;
import model.ressources.Collier;
import view.GamePanel;

import java.util.Random;

public class ResourceSpawner extends Thread {
    private GamePanel gamePanel;
    private int maxResources;
    private int spawnIntervalMin;
    private int spawnIntervalMax;
    private int spawnCountMin;
    private int spawnCountMax;
    private volatile boolean running = true;
    private Random random = new Random();
    private int resourcesSpawned = 0; // Ajout de la variable manquante

    public ResourceSpawner(GamePanel gamePanel, int maxResources, int spawnIntervalMin,
                           int spawnIntervalMax, int spawnCountMin, int spawnCountMax) {
        this.gamePanel = gamePanel;
        this.maxResources = maxResources;
        this.spawnIntervalMin = spawnIntervalMin;
        this.spawnIntervalMax = spawnIntervalMax;
        this.spawnCountMin = spawnCountMin;
        this.spawnCountMax = spawnCountMax;
    }

    public void stopSpawning() {
        running = false;
    }

    @Override
    public void run() {
        ThreadManager.incrementThreadCount("RessourceSpawner");
        try {
            while (running && resourcesSpawned < maxResources) {
                int spawnCount = spawnCountMin + random.nextInt(spawnCountMax - spawnCountMin + 1);

                for (int i = 0; i < spawnCount && resourcesSpawned < maxResources; i++) {
                    Position randomPos = generateValidPosition();
                    if (randomPos != null) {
                        Collier collier = new Collier(randomPos);
                        gamePanel.addObjet(collier);
                        gamePanel.getTerrain().incrementResourcesAt(randomPos.getX(), randomPos.getY());

                        GestionRessource gestionRessource = new GestionRessource(collier, 200);
                        gestionRessource.addListener(gamePanel.getInfoPanelUNC());
                        gestionRessource.start();

                        resourcesSpawned++;
                    }
                }

                int delay = spawnIntervalMin + random.nextInt(spawnIntervalMax - spawnIntervalMin);
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            ThreadManager.decrementThreadCount("RessourceSpawner");
        }
    }

    private Position generateValidPosition() {
        int attempts = 0;
        int maxAttempts = 100;

        while (attempts < maxAttempts) {
            int x = random.nextInt(GamePanel.TERRAIN_WIDTH);
            int y = random.nextInt(GamePanel.TERRAIN_HEIGHT);

            if (gamePanel.getTerrain().canAddResourceAt(x, y) && ZoneMover.isInsideAnyZone(new Position(x, y))) {
                return new Position(x, y);
            }
            attempts++;
        }
        return null;
    }
}