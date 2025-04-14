package model.unite_non_controlables;

import controler.GestionCollisions;
import model.objets.Position;
import model.objets.UniteControlable;
import model.unite_controlables.Plongeur;
import view.GamePanel;

import java.awt.*;
import java.util.List;

public class Kraken extends Cefalopode{

    private final Position origin;
    private static final int MAX_DISTANCE_FROM_ORIGIN = 400;
    private static final int ATTACK_RADIUS = 200;
    private final int DAMAGETENTACLE = 40;
    private static final int ATTACK_INTERVAL = 2000;
    private long lastAttackTime = 0;

    public Kraken(Position position) {
        super(position, 100, 4);
        this.origin = new Position(position.getX(), position.getY());
        setMaxHp(500);
        set_Hp(500);

        setImage("kraken.png");
        setMovingImage("kraken.png");
    }



    @Override
    public void attente() {
        setVitesseCourante(VITESSE_ATTENTE);

        // If the Kraken already has a destination, do nothing
        if (getDestination() != null) return;


        // Generate a random position within the ATTENTE_RANGE
        int dx = random.nextInt(2 * ATTENTE_RANGE) * (random.nextBoolean() ? 1 : -1);
        int dy = random.nextInt(2 * ATTENTE_RANGE) * (random.nextBoolean() ? 1 : -1);

        // Calculate the new destination
        int targetX = getPosition().getX() + dx;
        int targetY = getPosition().getY() + dy;

        // Ensure the destination is within MAX_DISTANCE_FROM_ORIGIN
        double distanceFromOrigin = Math.sqrt(Math.pow(targetX - origin.getX(), 2) + Math.pow(targetY - origin.getY(), 2));
        if (distanceFromOrigin > MAX_DISTANCE_FROM_ORIGIN) {
            // Scale the offsets to fit within the allowed radius
            double scale = MAX_DISTANCE_FROM_ORIGIN / distanceFromOrigin;
            dx = (int) (dx * scale);
            dy = (int) (dy * scale);
            targetX = getPosition().getX() + dx;
            targetY = getPosition().getY() + dy;
        }

        // Set the destination
        this.setDestination(new Position(targetX, targetY));
    }
    @Override
    public void action() {
        attente();

        if(getEtat().equals(Etat.ATTENTE) || target == null){
            attente();
            return;
        }

        double distance = this.distance(target);
        if(distance >=  MAX_DISTANCE_FROM_ORIGIN ) {
            //targetsDisponibles.remove(target);
            target = null;
            setDestination(origin);
            setEtat(Etat.ATTENTE);
            return;
        }


        if (getEtat().equals(Etat.VADROUILLE)) {
            if (targetsDisponibles.isEmpty()) {
                setEtat(Etat.ATTENTE);
            } else {

                System.out.println("Kraken: " + target.getClass().getSimpleName() + " distance: " + distance);

                //attaque la cible
                if (getDestination() != target.getPosition())
                    setDestination(target.getPosition());
                if (distance <= ATTACK_RADIUS) {
                    attack(target);
                    if (target.get_Hp() <= 0)
                        target = null;
                } else {
                    setDestination(target.getPosition());
                }
            }
        }
    }

    private synchronized void attack(UniteControlable target) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastAttackTime >= ATTACK_INTERVAL) {
            target.takeDamage(DAMAGETENTACLE);
            lastAttackTime = currentTime;
            set_Hp(get_Hp() +DAMAGETENTACLE);
        }
    }

    @Override
    public void draw(Graphics2D g2d, Point screenPos) {
       

        // Draw the Kraken's attack radius
        g2d.setColor(new Color(255, 0, 0, 50));
        g2d.fillOval(
                screenPos.x - ATTACK_RADIUS,
                screenPos.y - ATTACK_RADIUS,
                ATTACK_RADIUS * 2,
                ATTACK_RADIUS * 2
        );
        super.draw(g2d, screenPos);
    }
}