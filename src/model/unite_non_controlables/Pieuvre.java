package model.unite_non_controlables;

import controler.GameMaster;
import controler.GestionCollisions;
import controler.TileManager;
import model.objets.*;
import model.unite_controlables.Plongeur;
import view.GamePanel;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Pieuvre extends Cefalopode{

    private CopyOnWriteArrayList<Objet> sac = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<PieuvreBebe> enfants = new CopyOnWriteArrayList<>();


    public Pieuvre(Position position) {
        super(position, 20, 5);
        setImage("pieuvre.png");
        setMovingImage("pieuvre.png");
    }


    public ConcurrentHashMap<String, String> getAttributes() {
        ConcurrentHashMap<String, String> attributes =super.getAttributes();
        attributes.put("loot", String.valueOf(sac.size()));
        return attributes;
    }


    public CopyOnWriteArrayList<Objet> getSac() {return sac;}

    @Override
    public boolean voleTarget(){
        if(GestionCollisions.collisionCC(this, target) > -1){
            if(target instanceof Plongeur plongeur){
                if(!plongeur.getBackPac().isEmpty()){
                    sac.add(plongeur.seFaitVoler());
                    if (GamePanel.getInstance().getUnitesSelected().size() == 1) {
                        GamePanel.getInstance().getInfoPanel().getAtributInfo().updateInfo(plongeur.getAttributes());
                        GamePanel.getInstance().getInfoPanel().getAtributInfo().repaint();
                    }
                    attente();
                    return true;
                }
            }
        }
        return false;

    }
    @Override
    public void repaireTarget(UniteControlable target){
        super.repaireTarget(target);
        if(!enfants.isEmpty()){
            enfants.get(0).passTargetToSiblings();
        }
    }

    public void addChild(){
        //generer une position aleatoire près du parent
        Random random = new Random();
        int x = random.nextInt(2 * TileManager.TILESIZE) - TileManager.TILESIZE;
        int y = random.nextInt(2 * TileManager.TILESIZE) - TileManager.TILESIZE;
        Position position = new Position(getPosition().getX() + x, getPosition().getY() + y);

        PieuvreBebe bebe = new PieuvreBebe(position, this);
        enfants.add(bebe);
        GameMaster.getInstance().addEnemy(bebe, new CopyOnWriteArrayList<>(GamePanel.getInstance().getUnitesEnJeu()));
    }
    public CopyOnWriteArrayList<PieuvreBebe> getEnfants() {return enfants;}

    public void removeChild(PieuvreBebe enemy) {
        enfants.remove(enemy);
    }
}
