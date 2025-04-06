package model.unite_non_controlables;

import model.objets.Objet;
import model.objets.Position;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SeaSerpent extends Enemy {
    public static final int VITESSE_VADROUILLE = 2;

    private List<Position> segments;
    private LinkedList<Position> positionHistory;


    public SeaSerpent(Position position) {
        super(position, 20, 120, VITESSE_VADROUILLE);

     /* this.targetsDisponibles = new CopyOnWriteArrayList<>();
        setEtat(Etat.ATTENTE);*/

        this.positionHistory = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
            Position segmentPosition = new Position(position.getX() - i * 10, position.getY());
            segments.add(segmentPosition);
            positionHistory.add(segmentPosition);

        }

    }


    @Override
    public void setup(CopyOnWriteArrayList<Objet> interactionTargets) {}

    @Override
    public void attente(){}

    public void action(){}





}
