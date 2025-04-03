package controler;

import model.constructions.Base;
import model.constructions.Construction;
import model.objets.*;
import model.unite_controlables.Plongeur;
import model.unite_non_controlables.Calamar;
import model.unite_non_controlables.Enemy;
import model.unite_non_controlables.Pieuvre;
import model.unite_non_controlables.PieuvreBebe;
import view.GamePanel;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProximityChecker extends Thread{
    private final ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Objet>> objetsMap;
    private CopyOnWriteArrayList<UniteControlable> unitesEnJeu;

    private static ProximityChecker instance;

    private volatile boolean running = true;


    public ProximityChecker(ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Objet>> objetsMap, CopyOnWriteArrayList<UniteControlable> unitesEnJeu) {
        this.objetsMap = objetsMap;
        this.unitesEnJeu = unitesEnJeu;
        instance = this;
    }
    public static controler.ProximityChecker getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ProximityChecker n'a pas encore été instancié");
        }
        return instance;
    }

    //------------------------------------------------------------------------------------------------


    public CopyOnWriteArrayList<Objet> getObjetDansMemeTile(Objet objet){
        CopyOnWriteArrayList<Objet> voisins = objetsMap.get(objet.getCoordGrid());

        if (voisins == null)
            voisins = new CopyOnWriteArrayList<>();

        CopyOnWriteArrayList<Objet> voisinsCopy = new CopyOnWriteArrayList<>(voisins);

        voisinsCopy.remove(objet);
        return voisinsCopy;
    }

    public  CopyOnWriteArrayList<Objet> getObjetTilesVoisines(Objet objet) {
        CopyOnWriteArrayList<Objet> voisins = new CopyOnWriteArrayList<>();
        int x = objet.getCoordGrid().getX();
        int y = objet.getCoordGrid().getY();

        for(int j = -1; j <= 1; j++) {
            for(int k = -1; k <= 1; k++) {
                if(j == 0 && k == 0) continue;

                // Vérification des bords de la map avec les nouvelles variables
                if(x + j < 0 || x + j >= TileManager.nbTilesWidth ||
                        y + k < 0 || y + k >= TileManager.nbTilesHeight) {
                    continue;
                }

                int xVoisin = x + j;
                int yVoisin = y + k;
                CoordGrid coordVoisin = TileManager.getCoordTile(xVoisin, yVoisin);
                CopyOnWriteArrayList<Objet> objetsDansTile = objetsMap.get(coordVoisin);
                if(objetsDansTile != null) {
                    voisins.addAll(objetsDansTile);
                }
            }
        }
        return voisins;
    }

    public synchronized CopyOnWriteArrayList<Objet> getVoisins(UniteControlable unite) {
        CopyOnWriteArrayList<Objet> voisins = new CopyOnWriteArrayList<>();
        voisins.addAll(getObjetTilesVoisines(unite));
        voisins.addAll(getObjetDansMemeTile(unite));
        return voisins;
    }

    public boolean isRunning() {
        return running;
    }

    private void checkCollisionWithCameraBorder(UniteControlable unite) {

        GamePanel gamePanel = GamePanel.getInstance();
        int viewportMinX = gamePanel.getCameraX();
        int viewportMinY = gamePanel.getCameraY();
        int viewportMaxX = viewportMinX + GamePanel.VIEWPORT_WIDTH;
        int viewportMaxY = viewportMinY + GamePanel.VIEWPORT_HEIGHT;
        Position position = unite.getPosition();

        if (position.getX() < viewportMinX || position.getX() > viewportMaxX ||
                position.getY() < viewportMinY || position.getY() > viewportMaxY) {

            createNewDynamicZone(unite);
        }
    }

    private void createNewDynamicZone(UniteControlable unite) {
        //if(unite.isOutsideCamera()) return;     //une zone deja suit cette unite

        GamePanel gamePanel = GamePanel.getInstance();
        ZoneEnFonctionnement mainZone = gamePanel.getMainZone();
        Position position = unite.getPosition();
        int buffer = GamePanel.UNIT_BUFFER;

        int newMinX = Math.min(mainZone.getMinX(), position.getX() - buffer);
        int newMinY = Math.min(mainZone.getMinY(), position.getY() - buffer);
        int newMaxX = Math.max(mainZone.getMaxX(), position.getX() + buffer);
        int newMaxY = Math.max(mainZone.getMaxY(), position.getY() + buffer);

        ZoneEnFonctionnement newZone = new ZoneEnFonctionnement(newMinX, newMinY, newMaxX, newMaxY);
        unite.setOutsideCamera(true);
        gamePanel.addDynamicZone(newZone);

        newZone.setUnite(unite);

    }


    @Override
    public void run() {
        controler.ThreadManager.incrementThreadCount("ProximityChecker");
        try{
            while (running){
                for (UniteControlable unite : unitesEnJeu) {

                        checkCollisionWithCameraBorder(unite);

                        CopyOnWriteArrayList<Objet> voisins = getVoisins(unite);
                        for (Objet voisin : voisins) {

                            if (controler.GestionCollisions.collisionCC(unite, voisin) > -1) {
                                GestionCollisions.preventOverlap(unite, voisin);
                            }

                            if (unite instanceof Plongeur) {
                                if (voisin instanceof Calamar) {
                                    if (((Plongeur) unite).isFaitFuire() && controler.GestionCollisions.collisionPerimetreFuite((Plongeur) unite, (Calamar) voisin) > -1) {
                                        ((Plongeur) unite).faireFuirCalamar((Calamar) voisin);
                                    }
                                }else if (voisin instanceof Pieuvre) {
                                    ((Pieuvre) voisin).repaireTarget(unite);
                                } else if (voisin instanceof PieuvreBebe) {
                                    ((Enemy)voisin).vadrouille();
                                    ((PieuvreBebe) voisin).setTarget(unite);
                                    ((PieuvreBebe) voisin).passTargetToSiblings();
                                }else if(voisin instanceof Base){
                                    Position[] coins = ((Base) voisin).getCoints();
                                    if(GestionCollisions.estDans(coins[0].getX(), coins[0].getY(), coins[3].getX(), coins[3].getY(), unite.getPosition().getX(), unite.getPosition().getY())){
                                        ((Plongeur)unite).deliverBackpack();
                                        Plongeur plongeur = (Plongeur) unite;
                                        plongeur.setCurrentOxygen(plongeur.getCurrentOxygen() + OxygenHandler.OXYGEN_INCREMENT);
                                    }

                                }
                            }
                        }
                }

                Thread.sleep(50);
            }


        } catch (InterruptedException ignored) {
        }
            ThreadManager.decrementThreadCount("ProximityChecker");

    }
}

