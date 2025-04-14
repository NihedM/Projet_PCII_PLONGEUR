package model.unite_non_controlables;

import controler.GestionCollisions;
import controler.TileManager;
import model.objets.Objet;
import model.objets.Position;
import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import view.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Cefalopode extends Enemy{
    public final int VITESSE_VADROUILLE;
    private static final AtomicInteger nextId = new AtomicInteger(1);
    private final int id;

    protected UniteControlable target;
    protected static final double STALKING_DISTANCE = TileManager.TILESIZE *1.0 ;
    protected static final double SAFE_STALKING_DISTANCE = TileManager.TILESIZE *2.0 ;
    protected static final int MAX_DISTANCE = TileManager.TILESIZE * 6;

    protected CopyOnWriteArrayList<UniteControlable> targetsDisponibles;

    public Cefalopode(Position position, int rayon, int vitesse_vadrouille) {
        super(position, rayon, 120, vitesse_vadrouille);
        VITESSE_VADROUILLE = vitesse_vadrouille;
        this.id = nextId.getAndIncrement();

        this.targetsDisponibles = new CopyOnWriteArrayList<>(GamePanel.getInstance().getUnitesEnJeu());
        setEtat(Etat.ATTENTE);

        setImage("pieuvre.png");
        setMovingImage("pieuvre.png");
        setUnitIcon(new ImageIcon(GamePanel.getCachedImage("pieuvreIcon.png")));
    }

    //---------------------------------------------------------------------------------
    public int getId() {return id;}
    public void setTargetsDisponibles(CopyOnWriteArrayList<UniteControlable> targetsDisponibles){this.targetsDisponibles = targetsDisponibles;}
    public void selectTargetPlusProche(CopyOnWriteArrayList<UniteControlable> targets){
        UniteControlable unitePlusProche = (UniteControlable)(super.selectClosest(new CopyOnWriteArrayList<Objet>(targets)));

        if (unitePlusProche != null) {
            this.target = unitePlusProche;
        }else{
            setEtat(Etat.ATTENTE);
        }
    }
    public void setTarget(UniteControlable target) {this.target = target;}

    public UniteControlable getTarget() {return target;}

    public void repaireTarget(UniteControlable target){
        setEtat(Etat.VADROUILLE);
        if(!targetsDisponibles.contains(target)) {
            targetsDisponibles.add(target);
            selectTargetPlusProche(targetsDisponibles);
        }
        this.target = target;
    }

    protected Position getStalkingPosition(){
        Random random = new Random(id);
        int minDistance = (int)SAFE_STALKING_DISTANCE;
        int offsetX = random.nextInt(MAX_DISTANCE/2 - minDistance) - TileManager.TILESIZE;
        int offsetY = random.nextInt(MAX_DISTANCE/2 - minDistance) - TileManager.TILESIZE;
        return new Position(target.getPosition().getX() + offsetX, target.getPosition().getY() + offsetY);
    }

    public boolean voleTarget(){return false;}

    @Override

    public void fuit(){}

    @Override
    public void action() {


        if(getEtat().equals(Etat.ATTENTE) || target == null){
            attente();
            return;
        }

        double distance = this.distance(target);
        if(distance >= MAX_DISTANCE) {
            //targetsDisponibles.remove(target);
            target = null;
            setEtat(Etat.ATTENTE);
            return;
        }

        if (getEtat().equals(Etat.VADROUILLE)) {
            if(targetsDisponibles.isEmpty()){
                setEtat(Etat.ATTENTE);
            }else{
                selectTargetPlusProche(targetsDisponibles);
                if(target instanceof Plongeur){
                    Plongeur plongeur = (Plongeur) target;
                    if(plongeur.getBackPac().isEmpty()) {
                        //stalk, la cible se mantient près du plongeur

                        if (distance >STALKING_DISTANCE)
                            setDestination(getStalkingPosition());

                    }else {
                        //vole la cible
                        if(getDestination() != target.getPosition())
                            setDestination(target.getPosition());
                        boolean vole = voleTarget();
                        if(vole){
                            target = null;
                        }

                    }
                }


            }

        }

    }
    @Override
    public synchronized Color getColorForKey(String key) {
        if (key.equalsIgnoreCase("loot")) {
            return Color.GREEN ;
        }
        return super.getColorForKey(key);
    }



}
