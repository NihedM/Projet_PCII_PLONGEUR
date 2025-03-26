package model.objets;

import controler.GameMaster;
import model.objets.Position;
import model.objets.GestionRessource;
import model.ressources.Collier;
import view.GamePanel;

import java.util.Random;

public class ResourceSpawner extends Thread {
    private GamePanel gamePanel;
    private int maxResources; // Nombre maximum de ressources à générer
    private int spawnIntervalMin; // Intervalle minimum entre chaque apparition (en millisecondes)
    private int spawnIntervalMax; // Intervalle maximum entre chaque apparition (en millisecondes)
    private int spawnCountMin; // Nombre minimum de ressources à générer à chaque intervalle
    private int spawnCountMax; // Nombre maximum de ressources à générer à chaque intervalle
    private volatile boolean running = true;

    public ResourceSpawner(GamePanel gamePanel, int maxResources, int spawnIntervalMin, int spawnIntervalMax, int spawnCountMin, int spawnCountMax) {
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
        Random random = new Random();
        int resourcesSpawned = 0;
        controler.ThreadManager.incrementThreadCount("RessourceSpawner");

        while (running && resourcesSpawned < maxResources) {
            // Déterminer combien de ressources générer à cet intervalle
            int spawnCount = spawnCountMin + random.nextInt(spawnCountMax - spawnCountMin + 1);

            // Générer les ressources
            for (int i = 0; i < spawnCount && resourcesSpawned < maxResources; i++) {
                // Générer une ressource à une position aléatoire
                int x = random.nextInt(GamePanel.PANELDIMENSION);
                int y = random.nextInt(GamePanel.PANELDIMENSION);
                Position position = new Position(x, y);
                Collier collier = new Collier(position);

                // Ajouter la ressource au jeu
                gamePanel.addObjet(collier);

                // Créer et démarrer un thread GestionRessource pour cette ressource
                GestionRessource gestionRessource = new GestionRessource(collier, 20000); // Intervalle de 1 seconde
                gestionRessource.addListener(gamePanel.getInfoPanelUNC()); // Ajouter InfoPanelUNC comme listener
                gestionRessource.start(); // Démarrer le thread

                resourcesSpawned++;

                GameMaster.getInstance().updateLists();

            }

            // Attendre un délai aléatoire avant de générer la prochaine vague de ressources
            try {
                int delay = spawnIntervalMin + random.nextInt(spawnIntervalMax - spawnIntervalMin);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("ResourceSpawner interrupted");
                break;
            }
        }
        controler.ThreadManager.decrementThreadCount("ResourceSpawner");

        System.out.println("ResourceSpawner a terminé. Ressources générées : " + resourcesSpawned);
    }
}