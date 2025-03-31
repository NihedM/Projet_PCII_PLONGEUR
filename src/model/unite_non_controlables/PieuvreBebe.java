package model.unite_non_controlables;

import controler.GestionCollisions;
import controler.TileManager;
import model.objets.Objet;
import model.objets.Position;
import model.objets.Ressource;
import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import view.GamePanel;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class PieuvreBebe extends Enemy {
    public static final int VITESSE_VADROUILLE = 7;
    private UniteControlable target;
    private Pieuvre parent;
    private Ressource ressource;
    private static final double STALKING_DISTANCE = TileManager.TILESIZE * 2;
    private static final double MAX_DISTANCE = TileManager.TILESIZE * 4;
    private static final int ATTENTE_RANGE = TileManager.TILESIZE;


    public PieuvreBebe(Position position, Pieuvre parent) {
        super(position, 5, 120, VITESSE_VADROUILLE);
        this.parent = parent;
        this.target = parent.getTarget();
        setEtat(Etat.ATTENTE);
    }

    public void setTarget(UniteControlable target) {
        this.target = target;
    }

    public UniteControlable getTarget() {
        return target;
    }

    public Pieuvre getParent() {
        return parent;
    }

    public boolean voleTarget(){
        if(GestionCollisions.collisionCC(this, target) > -1){
            if(target instanceof Plongeur plongeur){
                if(!plongeur.getBackPac().isEmpty()){
                    ressource =  plongeur.seFaitVoler();
                    GamePanel.getInstance().getInfoPanel().updateInfo(plongeur);
                    attente();
                    return true;
                }
            }
        }
        return false;

    }



    @Override
    public void attente(){
        if(parent != null){

            if(getDestination() != null)return;

            int offsetX, offsetY;
            double distance;
            Random random = getRandom();

            do {
                offsetX = (random.nextInt(4 * ATTENTE_RANGE) - 2 * ATTENTE_RANGE);
                offsetY = (random.nextInt(4 * ATTENTE_RANGE) - 2 * ATTENTE_RANGE);

                distance = Math.sqrt(offsetX * offsetX + offsetY * offsetY);
            } while (distance < TileManager.TILESIZE / 2.0 || distance > 2 * TileManager.TILESIZE);


            Position newPosition = new Position(parent.getPosition().getX() + offsetX, parent.getPosition().getY() + offsetY);
            setDestination(newPosition);
            return;
        }
        super.attente();

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
        if(parent == null){
            setEtat(Etat.ATTENTE);
            return;
        }
        if(target == null)
            target = parent.getTarget();

        if(getEtat().equals(Etat.ATTENTE)){
            attente();

            if (this.distance(parent) > TileManager.TILESIZE*1.5) {
                setVitesseCourante(getVitesseMax());
            }else
                setVitesseCourante(VITESSE_ATTENTE);
            return;
        }

        if(target == null)
            return;
        double distance = this.distance(target);
        if(distance >= MAX_DISTANCE) {
            target = null;
            setEtat(Etat.ATTENTE);
            return;
        }

        if (getEtat().equals(Etat.VADROUILLE)) {
            if (ressource == null) {
                if(target instanceof Plongeur){

                    Plongeur plongeur = (Plongeur) target;
                    if(plongeur.getBackPac().isEmpty()) {

                        if (distance < STALKING_DISTANCE) return;
                        setDestination(target.getPosition());
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
                if (this.distance(parent) < TileManager.TILESIZE / 2.0) {
                    parent.getSac().add(ressource);
                    ressource = null;
                    setEtat(Etat.ATTENTE);
                }

            }
        }

    }




}
