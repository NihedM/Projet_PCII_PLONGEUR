package controler;

import view.debeug.ThreadManagerPanel;

import java.util.HashMap;
import java.util.Map;

public class ThreadManager {
    private static int totalThreadCount = 0;
    private static Map<String, Integer> runningThreadCounts = new HashMap<>();

    private static ThreadManagerPanel threadManagerPanel;


    static {
        runningThreadCounts.put("DisplayThread", 0);
        runningThreadCounts.put("Redessine", 0);
        runningThreadCounts.put("GestionnairesDeRessources", 0);
        runningThreadCounts.put("TileUpdater", 0);
        runningThreadCounts.put("ProximityChecker", 0);
        runningThreadCounts.put("GameMaster", 0);
        runningThreadCounts.put("SpawnManager", 0);
        runningThreadCounts.put("RessourceSpawner",0);
        runningThreadCounts.put("EnemySpawnPoints",0);
        runningThreadCounts.put("VictoryCheckThread", 0);
        runningThreadCounts.put("ResourceGenerationThread", 0);
        runningThreadCounts.put("DeplacementThread", 0);
        runningThreadCounts.put("FuiteHandler", 0);
    }


    public static void setThreadManagerPanel(ThreadManagerPanel panel) {
        threadManagerPanel = panel;
    }

    public static synchronized void incrementThreadCount(String threadType) {
        totalThreadCount++;
        runningThreadCounts.put(threadType, runningThreadCounts.getOrDefault(threadType, 0) + 1);
        updatePanel();
    }

    public static synchronized void decrementThreadCount(String threadType) {
        totalThreadCount--;
        runningThreadCounts.put(threadType, runningThreadCounts.getOrDefault(threadType, 0) - 1);
        updatePanel();
    }


    private static void updatePanel() {
        if (threadManagerPanel != null) {
            threadManagerPanel.updateThreadCounts(runningThreadCounts, totalThreadCount);
        }
    }

    public static synchronized void startDisplayThread() {

        new Thread(() -> {
            controler.ThreadManager.incrementThreadCount("DisplayThread");
            while (true) {
                updatePanel();
                try {
                    Thread.sleep(10); // Adjust the sleep time as needed
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Display thread interrupted");
                }
            }
            //ThreadManager.decrementThreadCount("DisplayThread");
        }).start();
    }



}

