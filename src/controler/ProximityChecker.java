package controler;

import model.objets.*;
import model.unite_controlables.Plongeur;
import model.unite_non_controlables.Calamar;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProximityChecker extends Thread{
    private final ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Objet>> objetsMap;
    private CopyOnWriteArrayList<UniteControlable> unitesEnJeu;

    private static ProximityChecker instance;


    public ProximityChecker(ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Objet>> objetsMap, CopyOnWriteArrayList<UniteControlable> unitesEnJeu) {
        this.objetsMap = objetsMap;
        this.unitesEnJeu = unitesEnJeu;
        instance = this;
    }
    public static controler.ProximityChecker getInstance() {
        return instance;
    }

    //------------------------------------------------------------------------------------------------


    public synchronized CopyOnWriteArrayList<Objet> getObjetDansMemeTile(Objet objet){
        CopyOnWriteArrayList<Objet> voisins = objetsMap.get(objet.getCoordGrid());

        if (voisins == null)
            voisins = new CopyOnWriteArrayList<>();

        CopyOnWriteArrayList<Objet> voisinsCopy = new CopyOnWriteArrayList<>(voisins);

        voisinsCopy.remove(objet);
        return voisinsCopy;
    }

    public synchronized CopyOnWriteArrayList<Objet> getObjetTilesVoisines(Objet objet) {
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



    @Override
    public void run() {
        controler.ThreadManager.incrementThreadCount("ProximityChecker");
        while (true){
            //GamePanel.printGridContents(objetsMap);
            synchronized (objetsMap) {
                for (UniteControlable unite : unitesEnJeu) {

                    CopyOnWriteArrayList<Objet> voisins = getVoisins(unite);

                    for(Objet voisin: voisins){
                        if (voisin instanceof Ressource && ((Ressource) voisin).isFixed()) {
                            continue;
                        }
                        if (controler.GestionCollisions.collisionCC(unite, voisin) > -1) {
                            if (voisin instanceof Unite) {
                                GestionCollisions.rebound((Unite) unite, (Unite) voisin);
                            }
                            GestionCollisions.preventOverlap(unite, voisin);
                        }

                        if(unite instanceof Plongeur){
                            if(voisin instanceof Calamar){
                                if( ((Plongeur) unite).isFaitFuire() && controler.GestionCollisions.collisionPerimetreFuite((Plongeur) unite, (Calamar) voisin) > -1){
                                    ((Plongeur) unite).faireFuirCalamar((Calamar) voisin);
                                }
                            }



                        }

                        if(controler.GestionCollisions.collisionCC(unite, voisin) > -1){
                            if(voisin instanceof Unite){
                                GestionCollisions. rebound((Unite) unite, (Unite) voisin);
                            }
                            GestionCollisions.preventOverlap(unite, voisin);
                        }

                    }
                }
            }



            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }

        }
        ThreadManager.decrementThreadCount("ProximityChecker");

    }
}

