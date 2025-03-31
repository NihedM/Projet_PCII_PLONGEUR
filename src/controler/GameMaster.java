package controler;

import model.constructions.Base;
import model.objets.CoordGrid;
import model.objets.Objet;
import model.objets.Ressource;
import model.objets.UniteNonControlableInterface;
import model.unite_non_controlables.Calamar;
import model.unite_non_controlables.Enemy;
import model.unite_non_controlables.Pieuvre;
import model.unite_non_controlables.PieuvreBebe;
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

    public CopyOnWriteArrayList<Enemy> getEnemies() {
        return enemies;
    }



    public void setEnemies(CopyOnWriteArrayList<Enemy> enemies) {
        this.enemies = enemies;
    }

    public void addEnemy(Enemy enemy, CopyOnWriteArrayList<Objet> targets) {
        this.enemies.add(enemy);

        enemy.setup(targets);
        GamePanel.getInstance().addObjet(enemy);
    }


    public void removeEnemy(Enemy enemy) {
        if(enemies.contains(enemy))
            enemies.remove(enemy);
        else
            throw new UnsupportedOperationException("Enemy not found in list");
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
            }else if(enemy instanceof Pieuvre || enemy instanceof PieuvreBebe) {
                continue;
            }



            else{
                throw new UnsupportedOperationException("Enemy type not supported: "+enemy.getClass());

            }

        }
    }
    public void reset() {
        ressources.clear();
        updateLists();
    }


    @Override
    public void run() {
        ThreadManager.incrementThreadCount("GameMaster");
        updateLists();

        while(true) {
            if(enemies != null) {  // Inverser la condition
                for (Enemy enemy : enemies) {
                    enemy.action();

                    // Gestion du hors terrain
                    if (!GamePanel.getInstance().isWithinTerrainBounds(enemy.getPosition())) {
                        GamePanel.getInstance().killUnite(enemy);
                    }
                    Base base = GamePanel.getInstance().getMainBase();
                    if(GestionCollisions.estDans(base.getCoints()[0].getX(), base.getCoints()[0].getY(), base.getCoints()[3].getX(), base.getCoints()[3].getY(), enemy.getPosition().getX(), enemy.getPosition().getY())){
                        if(enemy instanceof PieuvreBebe){
                            ((PieuvreBebe) enemy).getParent().removeChild((PieuvreBebe) enemy);
                        }
                        GamePanel.getInstance().killUnite(enemy);
                    }
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
