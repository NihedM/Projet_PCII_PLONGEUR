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

                        if(getDestination() != null)return;

                        int dx = target.getPosition().getX() - this.getPosition().getX();
                        int dy = target.getPosition().getY() - this.getPosition().getY();

                        double norm = Math.sqrt(dx * dx + dy * dy);
                        double unitDx = dx / norm;
                        double unitDy = dy / norm;

                        setVx((int) (unitDx * getVitesseCourante()));
                        setVy((int) (unitDy * getVitesseCourante()));

                        boolean targetNotMoving = plongeur.getVitesseCourante() == 0;
                        double randomAngle = Math.random() * 2 * Math.PI;

                        if (targetNotMoving) {
                            randomAngle *= 2; // Increase the random angle
                            setVitesseCourante(getVitesseAttente()); // Reduce the speed
                        }

                        double randomDistance = Math.random() * stalkingDistance + stalkingDistance - target.getRayon();

                        int stalkX = target.getPosition().getX() - (int) (unitDx * randomDistance );
                        int stalkY = target.getPosition().getY() - (int) (unitDy * randomDistance );

                        setDestination(new Position(stalkX, stalkY));




                    }else {

                        //still
                    }

                }


            }

        }

    }
}
