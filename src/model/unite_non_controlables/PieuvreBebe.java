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
    private static final double MAX_DISTANCE = TileManager.TILESIZE * 3;
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
            Random random = new Random();
            // Generate a random position around the parent but not too close
            int offsetX = (random.nextInt(2 * ATTENTE_RANGE) - ATTENTE_RANGE) * (random.nextBoolean() ? 1 : -1);
            int offsetY = (random.nextInt(2 * ATTENTE_RANGE) - ATTENTE_RANGE) * (random.nextBoolean() ? 1 : -1);

            // Ensure the new position is not too close to the parent
            while (Math.abs(offsetX) < TileManager.TILESIZE && Math.abs(offsetY) < TileManager.TILESIZE) {
                offsetX = (random.nextInt(2 * ATTENTE_RANGE) - ATTENTE_RANGE) * (random.nextBoolean() ? 1 : -1);
                offsetY = (random.nextInt(2 * ATTENTE_RANGE) - ATTENTE_RANGE) * (random.nextBoolean() ? 1 : -1);
            }

            Position newPosition = new Position(parent.getPosition().getX() + offsetX, parent.getPosition().getY() + offsetY);
            setDestination(newPosition);
        }
        super.attente();

    }


    @Override
    public void action() {
        if(getEtat().equals(Etat.ATTENTE)){
            attente();
            return;
        }
        if(target == null)
            target = parent.getTarget();
        if(target == null){
            setEtat(Etat.ATTENTE);
            return;
        }


        double distance = this.distance(target);
        if(distance >= MAX_DISTANCE) {
            target = null;
            setEtat(Etat.ATTENTE);
            return;
        }

        if (getEtat().equals(Etat.VADROUILLE)) {
            if (ressource == null) {
                boolean vole = voleTarget();
                if (!vole) {
                    setEtat(Etat.ATTENTE);
                }
            }
        }

    }




}
