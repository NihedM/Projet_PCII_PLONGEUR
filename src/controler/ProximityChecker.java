package controler;

import model.constructions.Base;
import model.constructions.Construction;
import model.objets.*;
import model.unite_controlables.Plongeur;
import model.unite_controlables.PlongeurArme;
import model.unite_controlables.SousMarin;
import model.unite_non_controlables.*;
import view.GamePanel;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProximityChecker extends Thread{
    private final ConcurrentHashMap<CoordGrid, CopyOnWriteArrayList<Objet>> objetsMap;
    private CopyOnWriteArrayList<UniteControlable> unitesEnJeu;

    private static ProximityChecker instance;

    private static final int DELAY = 30;

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
            if (!unite.isDynamicZoneCreated()) {
                //System.out.println("Unité en dehors de la caméra : ");
                createNewDynamicZone(unite);
                unite.setDynamicZoneCreated(true);
            }
        }else{
            destroyDynamicZone(unite);
            unite.setDynamicZoneCreated(false);
        }
    }

    private void createNewDynamicZone(UniteControlable unite) {
        //if(unite.isOutsideCamera()) return;     //une zone deja suit cette unite

        GamePanel gamePanel = GamePanel.getInstance();
        ZoneEnFonctionnement mainZone = gamePanel.getMainZone();
        Position position = unite.getPosition();
        int buffer = GamePanel.UNIT_BUFFER;

        int newMinX = position.getX() - buffer;//Math.min(mainZone.getMinX(), position.getX() - buffer);
        int newMinY = position.getY() - buffer;//Math.min(mainZone.getMinY(), position.getY() - buffer);
        int newMaxX = position.getX() + buffer ;//Math.max(mainZone.getMaxX(), position.getX() + buffer);
        int newMaxY = position.getY() + buffer;//Math.max(mainZone.getMaxY(), position.getY() + buffer);

        ZoneEnFonctionnement newZone = new ZoneEnFonctionnement(newMinX, newMinY, newMaxX, newMaxY);
        gamePanel.addDynamicZone(newZone);
        newZone.setUnite(unite);

    }

    private void destroyDynamicZone(UniteControlable unite) {

        ZoneEnFonctionnement dynamicZoneToRemove = null;
        for (ZoneEnFonctionnement zone : GamePanel.getInstance().getDynamicZones()) {
            if (zone.getUnit() != null && zone.getUnit().equals(unite)) {
                dynamicZoneToRemove = zone;
                break;
            }
        }
        if (dynamicZoneToRemove != null) {
            GamePanel.getInstance().removeDynamicZone(dynamicZoneToRemove);
        }

    }


        @Override
    public void run() {
        controler.ThreadManager.incrementThreadCount("ProximityChecker");
        try{
            while (running) {
                for (UniteControlable unite : unitesEnJeu) {

                    checkCollisionWithCameraBorder(unite);

                    CopyOnWriteArrayList<Objet> voisins = getVoisins(unite);
                    for (Objet voisin : voisins) {


                        if (unite instanceof Plongeur) {

                            if (voisin.equals(((Plongeur) unite).getTargetResource())) {
                                if (GestionCollisions.collisionCC(unite, voisin) > -1) {
                                    if (((Ressource) voisin).estRecoltable()) {
                                        boolean collected = ((Plongeur) unite).recolter((Ressource) voisin);
                                        if (collected) {
                                            // Une fois collectée, on réinitialise la cible et désactive le flag targeted
                                            ((Plongeur) unite).getTargetResource().setTargeted(false);
                                            ((Plongeur) unite).setTargetResource(null);
                                        }
                                    }
                                }
                            }

                            if (unite instanceof PlongeurArme plongeurArme) {
                                if (voisin instanceof Enemy enemy && plongeurArme.isDefending() && GestionCollisions.collisionDefendCircle(plongeurArme, enemy) > -1) {
                                    if (plongeurArme.canShoot()) { // Check if enough time has passed since the last shot
                                        plongeurArme.shoot(enemy.getPosition());
                                        plongeurArme.updateLastShotTime(); // Update the last shot time
                                    }
                                }

                                if (((PlongeurArme) unite).getTarget() != null && GamePanel.getInstance().isAttackingMode() && controler.GestionCollisions.collisionCC(unite, ((PlongeurArme) unite).getTarget()) > -1) {
                                    synchronized (((PlongeurArme) unite).getTarget()) {
                                        try {
                                            ((PlongeurArme) unite).getTarget().takeDamage(((PlongeurArme) unite).DAMAGEATTTACK);
                                            GamePanel.getInstance().setAttackinggMode(false);
                                            ((PlongeurArme) unite).setTarget(null);
                                            unite.stopAction();
                                        } catch (Exception e) {
                                            System.out.println("Erreur lors de l'attaque : " + e.getMessage());
                                        }
                                    }
                                }
                            }

                            if (voisin instanceof Calamar) {
                                if (((Plongeur) unite).isFaitFuire() && controler.GestionCollisions.collisionPerimetreFuite((Plongeur) unite, (Calamar) voisin) > -1) {
                                    ((Plongeur) unite).faireFuirCalamar((Calamar) voisin);
                                }
                            } else if (voisin instanceof Cefalopode) {
                                ((Cefalopode) voisin).repaireTarget(unite);
                                if (voisin instanceof PieuvreBebe)
                                    ((PieuvreBebe) voisin).passTargetToSiblings();

                            } else if (voisin instanceof Base) {
                                Position[] coins = ((Base) voisin).getCoints();
                                if (GestionCollisions.estDans(coins[0].getX(), coins[0].getY(), coins[3].getX(), coins[3].getY(), unite.getPosition().getX(), unite.getPosition().getY())) {
                                    ((Plongeur) unite).deliverBackpack();
                                    Plongeur plongeur = (Plongeur) unite;
                                    plongeur.setCurrentOxygen(plongeur.getCurrentOxygen() + OxygenHandler.OXYGEN_INCREMENT);
                                    if (unite instanceof PlongeurArme plongeurArme) {
                                        plongeurArme.reload(plongeurArme.getMaxAmmo());
                                        System.out.println("PlongeurArme bullets refilled: " + plongeurArme.getAmmo());
                                    }
                                }
                            } else if (voisin instanceof model.unite_controlables.SousMarin) {
                                if (GestionCollisions.collisionCC(unite, voisin) > -1) {
                                    ((SousMarin) voisin).boardDiver((Plongeur) unite); // le plongeur entre dans le sous-marin
                                    GamePanel.getInstance().killUnite(unite); // on le retire du terrain
                                    break;
                                }
                            }
                        }


                        if (controler.GestionCollisions.collisionCC(unite, voisin) > -1) {
                            GestionCollisions.preventOverlap(unite, voisin);
                        }

                    }

                }
                Thread.sleep(DELAY);
            }


        } catch (InterruptedException ignored) {
        }
            ThreadManager.decrementThreadCount("ProximityChecker");

    }
}

