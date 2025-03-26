package controler;

import model.objets.CoordGrid;
import model.objets.Objet;
import model.objets.Ressource;
import model.unite_non_controlables.Calamar;
import model.unite_non_controlables.Enemy;
import model.unite_non_controlables.Pieuvre;
import view.GamePanel;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameMaster extends Thread{

    private static GameMaster instance;
    private CopyOnWriteArrayList<Ressource> ressources;
    private CopyOnWriteArrayList<Enemy> enemies;
    private ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Objet>> objetsMap ;


    public GameMaster() {
        this.objetsMap = GamePanel.getInstance().getObjetsMap();
        ressources = new CopyOnWriteArrayList<Ressource>();
        enemies = new CopyOnWriteArrayList<Enemy>();
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
    public CopyOnWriteArrayList<Ressource> getRessources() {
        return ressources;
    }

    public void setRessourcesVisibilesJoueur(CopyOnWriteArrayList<Ressource> ressources) {
        this.ressources = ressources;
    }




    public void setEnemies(CopyOnWriteArrayList<Enemy> enemies) {
        this.enemies = enemies;
    }

    public void addEnemy(Enemy enemy, CopyOnWriteArrayList<Objet> targets) {
        this.enemies.add(enemy);

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
            }else if(enemy instanceof Pieuvre){
                ((Pieuvre) enemy).setTargetsDisponibles(GamePanel.getInstance().getUnitesEnJeu());
            }



            else{
                throw new UnsupportedOperationException("Enemy type not supported");

            }
        }
    }


    @Override
    public void run() {
        ThreadManager.incrementThreadCount("GameMaster");
        updateLists();

        while(true) {
            if(enemies != null) {  // Inverser la condition
                for (Enemy enemy : enemies) {
                    enemy.action();
                }
                updateTargets();
            }
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
