package model.unite_non_controlables;

import controler.GestionCollisions;
import controler.TileManager;
import model.objets.Objet;
import model.objets.Position;
import model.objets.Ressource;
import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import view.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PieuvreBebe extends Cefalopode{

    private Pieuvre parent;
    private Ressource ressource;

    public PieuvreBebe(Position position, Pieuvre parent) {
        super(position, 10,7);
        this.parent = parent;
        this.target = parent.getTarget();
        setEtat(Etat.ATTENTE);

        setImage("pieuvre.png");
        setMovingImage("pieuvre.png");
        setUnitIcon(new ImageIcon(GamePanel.getCachedImage("pieuvreIcon.png")));
    }

    public ConcurrentHashMap<String, String> getAttributes() {
        ConcurrentHashMap<String, String> attributes =super.getAttributes();
        attributes.put("loot", String.valueOf(hasResource()?1:0));
        return attributes;
    }


    @Override
    public synchronized int getMaxValueForKey(String key) {
        if (key.equalsIgnoreCase("loot")) {
            return 1;
        }
        return super.getMaxValueForKey(key);
    }

    public boolean hasResource() {
        return ressource != null;
    }
    public Pieuvre getParent() {
        return parent;
    }

    public boolean voleTarget(){
        if(GestionCollisions.collisionCC(this, target) > -1){
            if(target instanceof Plongeur plongeur){
                if(!plongeur.getBackPac().isEmpty()){
                    ressource =  plongeur.seFaitVoler();
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





    public void passTargetToSiblings() {
        if (parent != null && target != null) {
            for (PieuvreBebe sibling : parent.getEnfants()) {
                if (sibling != this) {
                    sibling.setTarget(this.target);
                }
            }
        }
    }


    @Override
    public void action() {
        if(parent == null || getEtat().equals(Etat.ATTENTE) || target == null){
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

            if (ressource == null) {

                selectTargetPlusProche(targetsDisponibles);
                if(target instanceof Plongeur){
                    Plongeur plongeur = (Plongeur) target;
                    if(plongeur.getBackPac().isEmpty()) {

                        if (distance >STALKING_DISTANCE)
                            setDestination(getStalkingPosition());
                    }else {
                        if(getDestination() != target.getPosition())
                            setDestination(target.getPosition());
                        boolean vole = voleTarget();
                        if(vole){
                            target = null;
                        }

                    }
                }


            }else {//on amene la ressource au parent
                setDestination(parent.getPosition());
                if (this.distance(parent) < TileManager.TILESIZE / 4.0) {
                    parent.getSac().add(ressource);
                    ressource = null;
                    setEtat(Etat.ATTENTE);
                }

            }
        }

    }




}
