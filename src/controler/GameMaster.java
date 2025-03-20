package controler;

import model.objets.CoordGrid;
import model.objets.Objet;
import model.objets.Ressource;
import model.unite_non_controlables.Calamar;
import model.unite_non_controlables.Enemy;
import view.GamePanel;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameMaster extends Thread{

    private static GameMaster instance;
    private ArrayList<Ressource> ressources;
    private ArrayList<Enemy> enemies;
    private ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Objet>> objetsMap ;


    public GameMaster() {
        this.objetsMap = GamePanel.getInstance().getObjetsMap();
        ressources = new ArrayList<Ressource>();
        enemies = new ArrayList<Enemy>();
        instance = this;
        updateLists();
    }

    public void updateLists() {
        ressources.clear();
        enemies.clear();
        for (CopyOnWriteArrayList<Objet> objets : objetsMap.values()) {
            for (Objet objet : objets) {
                if (objet instanceof Ressource) {
                    ressources.add((Ressource) objet);
                } else if (objet instanceof Enemy) {
                    enemies.add((Enemy) objet);
                }
            }
        }
    }


    //-------------------GETTERS-------------------//

    public static GameMaster getInstance() {
        return instance;
    }
    public ArrayList<Ressource> getRessources() {
        return ressources;
    }

    public void setRessourcesVisibilesJoueur(ArrayList<Ressource> ressources) {
        this.ressources = ressources;
    }




    public void setEnemies(ArrayList<Enemy> enemies) {
        this.enemies = enemies;
    }

    public void addEnemy(Enemy enemy, ArrayList<Objet> targets) {
        this.enemies.add(enemy);


        //TODO
        enemy.setup(targets);
    }



    public void AleaSpawnRessources(int x, int y, int nb) {
    }
    public void SpawnRessources(int x, int y, int nb) {
    }


    /*

    // temporaire tant que les bordures ne sont pas définies
            if (unite instanceof Enemy && (unite.getPosition().getX() <= 5 || unite.getPosition().getY() <= 5 )) {
                panel.removeObjet(unite, unite.getCoordGrid());
            }

     */

    public void updateTargets(){
        for (Enemy enemy : enemies) {
            if (enemy instanceof Calamar) {
                ((Calamar) enemy).setRessourcesDisponibles(ressources);
            }



        }
    }


    @Override
    public void run() {
        ThreadManager.incrementThreadCount("GameMaster");
        updateLists();

        while(true){
            if(enemies == null)continue;
            for (Enemy enemy : enemies) {
                enemy.action();
            }
            updateTargets();
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }

        }
        ThreadManager.decrementThreadCount("GameMaster");


    }
}
