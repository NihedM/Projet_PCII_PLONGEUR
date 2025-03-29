package model.unite_non_controlables;

import controler.GestionCollisions;
import controler.TileManager;
import model.objets.*;
import model.unite_controlables.Plongeur;
import view.GamePanel;

import java.util.concurrent.CopyOnWriteArrayList;

public class Pieuvre extends Enemy {
    public static final int VITESSE_VADROUILLE = 5;
    private CopyOnWriteArrayList<UniteControlable> targetsDisponibles = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<Objet> sac = new CopyOnWriteArrayList<>();
    private UniteControlable target;
    private double stalkingDistance = TileManager.TILESIZE * 2;




    public Pieuvre(Position position) {
        super(position, 10, 120, VITESSE_VADROUILLE);
        this.targetsDisponibles = new CopyOnWriteArrayList<>();
        setEtat(Etat.ATTENTE);
    }

    public void setTargetsDisponibles(CopyOnWriteArrayList<UniteControlable> targetsDisponibles){
        this.targetsDisponibles = targetsDisponibles;
    }

    public void selectTargetPlusProche(CopyOnWriteArrayList<UniteControlable> targets){
        UniteControlable unitePlusProche = (UniteControlable)(super.selectClosest(new CopyOnWriteArrayList<Objet>(targets)));

        if (unitePlusProche != null) {
               this.target = unitePlusProche;
        }
    }


    public void repaireTarget(UniteControlable target){
        setEtat(Etat.VADROUILLE);
        if(!targetsDisponibles.contains(target)) {
            targetsDisponibles.add(target);
            selectTargetPlusProche(targetsDisponibles);
        }
    }


    public void voleTarget(){
        if(GestionCollisions.collisionCC(this, target) > -1){
            if(target instanceof Plongeur plongeur){
                if(plongeur.getSacSize() > 0){
                    plongeur.seFaitVoler();
                    sac.add(plongeur.seFaitVoler());
                    attente();
                }
            }

        }

    }


    @Override
    public void setup(CopyOnWriteArrayList<Objet> interactionTargets) {
        //Todo en attente tend qu'il ne soit pas détécté
    }
    @Override
    public void action() {
        if(getEtat().equals(Etat.ATTENTE)){
            attente();
        }

        if (getEtat().equals(Etat.VADROUILLE)) {
            if(targetsDisponibles.isEmpty()){
                setEtat(Etat.ATTENTE);
            }else{

                //si la cible ce trouve à plus de 5 tiles de distance alors on la retire des targets disponibles
                double distance = this.distance(target);

                if(distance >= TileManager.TILESIZE* 5){
                    targetsDisponibles.remove(target);
                }
                selectTargetPlusProche(targetsDisponibles);


                if(target instanceof Plongeur){
                    Plongeur plongeur = (Plongeur) target;
                    if(plongeur.getSacSize() == 0) {
                        //stalk
                        if (distance < TileManager.TILESIZE * 2) {
                            // Stop moving
                            setDestination(null);
                        } else if (distance >= TileManager.TILESIZE * 2 && distance <= TileManager.TILESIZE * 4) {
                            // Move closer
                            setDestination(plongeur.getPosition());
                        }

                    }else {
                        voleTarget();
                    }

                }


            }

        }

    }
}
