package model.unite_non_controlables;

import controler.GestionCollisions;
import model.objets.*;
import model.unite_controlables.Plongeur;
import view.GamePanel;

import java.util.concurrent.CopyOnWriteArrayList;

public class Pieuvre extends Enemy {
    public static final int VITESSE_VADROUILLE = 5;
    private CopyOnWriteArrayList<UniteControlable> targetsDisponibles = new CopyOnWriteArrayList<>();

    private UniteControlable target;



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
        //definir la ressource la plus proche comme objectif
        if (unitePlusProche != null) {
            this.setDestination(unitePlusProche.getPosition());
            //System.out.println("Ressource " + ressourcePlusProche.getPosition().getX() + " " + ressourcePlusProche.getPosition().getY());
            this.target = unitePlusProche;
        }
    }


    public void voleTarget(){

        //retire du sac

        //ajoute dans le sac

        //attente temporaire

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

        /*

        if(GestionCollisions.collisionCC(this, target) > -1 ) {
            if (targetsDisponibles.contains(target)) {

                voleTarget();
                target = null;
            }

        }

        if (!targetsDisponibles.isEmpty())
            selectTargetPlusProche(targetsDisponibles);*/

    }
}
