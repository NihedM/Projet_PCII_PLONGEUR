package model.unite_non_controlables;

import controler.GameMaster;
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
    private CopyOnWriteArrayList<PieuvreBebe> enfants = new CopyOnWriteArrayList<>();

    private UniteControlable target;
    private static final double STALKING_DISTANCE = TileManager.TILESIZE * 2;
    private static final double MAX_DISTANCE = TileManager.TILESIZE * 3;





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
        }else{
            setEtat(Etat.ATTENTE);
        }
    }

    public UniteControlable getTarget() {
        return target;
    }
    public CopyOnWriteArrayList<Objet> getSac() {
        return sac;
    }


    public void repaireTarget(UniteControlable target){
        setEtat(Etat.VADROUILLE);
        if(!targetsDisponibles.contains(target)) {
            targetsDisponibles.add(target);
            selectTargetPlusProche(targetsDisponibles);
            if(!enfants.isEmpty()){
               enfants.get(0).passTargetToSiblings();
            }
        }
    }


    public boolean voleTarget(){
        if(GestionCollisions.collisionCC(this, target) > -1){
            if(target instanceof Plongeur plongeur){
                if(!plongeur.getBackPac().isEmpty()){
                    sac.add(plongeur.seFaitVoler());
                    GamePanel.getInstance().getInfoPanel().updateInfo(plongeur);
                    attente();
                    return true;
                }
            }
        }
        return false;

    }

    public void addChild(){
        Position position = EnemySpawnPoint.generateRandomPositionInTile(getCoordGrid());
        PieuvreBebe bebe = new PieuvreBebe(position, this);
        enfants.add(bebe);
        GameMaster.getInstance().addEnemy(bebe, new CopyOnWriteArrayList<>(GamePanel.getInstance().getUnitesEnJeu()));
    }
    public CopyOnWriteArrayList<PieuvreBebe> getEnfants() {
        return enfants;
    }



    @Override
    public void action() {



        if(getEtat().equals(Etat.ATTENTE) || target == null){
            attente();
            return;
        }

        double distance = this.distance(target);
        if(distance >= MAX_DISTANCE) {
            targetsDisponibles.remove(target);
            target = null;
            setEtat(Etat.ATTENTE);
            return;
        }

        if (getEtat().equals(Etat.VADROUILLE)) {
            if(targetsDisponibles.isEmpty()){
                setEtat(Etat.ATTENTE);
            }else{
                if(target instanceof Plongeur){

                    Plongeur plongeur = (Plongeur) target;
                    if(plongeur.getBackPac().isEmpty()) {
                        //stalk, la cible se mantient près du plongeu
                        // r

                        if (distance < STALKING_DISTANCE) return;
                        setDestination(target.getPosition());
                    }else {
                        if(getDestination() != target.getPosition())
                            setDestination(target.getPosition());
                        boolean vole = voleTarget();
                        if(vole){
                            targetsDisponibles.remove(target);
                            target = null;
                        }

                    }


                }


            }

        }

    }

    public void removeChild(PieuvreBebe enemy) {
        enfants.remove(enemy);
    }
}
