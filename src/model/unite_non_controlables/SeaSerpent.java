package model.unite_non_controlables;

import model.objets.Objet;
import model.objets.Position;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class SeaSerpent extends Enemy {
    public static final int VITESSE_VADROUILLE = 2;

    private List<Position> segments;
    private LinkedList<Position> positionHistory;
    private final Position origin;
    private Position targetPosition;


    public SeaSerpent(Position position) {
        super(position, 20, 120, VITESSE_VADROUILLE);
        this.origin = new Position(position.getX(), position.getY());

        this.segments = new LinkedList<>();
        targetPosition = generateRandomTarget();

        this.positionHistory = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
            Position segmentPosition = new Position(position.getX() - i * 10, position.getY());
            segments.add(segmentPosition);
            positionHistory.add(segmentPosition);

        }

    }

    private Position generateRandomTarget() {
        Random random = new Random();
        int radius = 400;

        // Generate random offsets within the radius
        int offsetX = random.nextInt(2 * radius + 1) - radius; // Random value between -400 and 400
        int offsetY = random.nextInt(2 * radius + 1) - radius;

        // Calculate the new target position
        int targetX = origin.getX() + offsetX;
        int targetY = origin.getY() + offsetY;

        return new Position(targetX, targetY);
    }


    @Override
    public void setup(CopyOnWriteArrayList<Objet> interactionTargets) {}

    @Override
    public void attente(){
        if (this.distance(targetPosition) <= getRayon()) {
            // Generate a new random target position
            targetPosition = generateRandomTarget();
            System.out.println("New target position generated: " + targetPosition);
        }

        // Move toward the target position
        moveToTarget(targetPosition);
    }

    @Override
    public void action() {
        attente();
    }

    private void moveToTarget(Position target) {
        int dx = target.getX() - getPosition().getX();
        int dy = target.getY() - getPosition().getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            int moveX = (int) (VITESSE_VADROUILLE * dx / distance);
            int moveY = (int) (VITESSE_VADROUILLE * dy / distance);
            getPosition().setX(getPosition().getX() + moveX);
            getPosition().setY(getPosition().getY() + moveY);

            // Update the position history for the body segments
            positionHistory.addFirst(new Position(getPosition().getX(), getPosition().getY()));
            if (positionHistory.size() > segments.size() + 1) {
                positionHistory.removeLast();
            }

            // Update the body segments
            for (int i = 0; i < segments.size(); i++) {
                segments.set(i, positionHistory.get(Math.min(i + 1, positionHistory.size() - 1)));
            }
        }
    }


    @Override
    public void draw(Graphics2D g2d, Point screenPos) {
        // Draw the head
        g2d.setColor(Color.RED);
        g2d.fillOval(screenPos.x - getRayon(), screenPos.y - getRayon(), getRayon() * 2, getRayon() * 2);

        // Draw the body segments
        g2d.setColor(Color.ORANGE);
        for (Position segment : segments) {
            int screenX = segment.getX() - screenPos.x + screenPos.x;
            int screenY = segment.getY() - screenPos.y + screenPos.y;
            g2d.fillOval(screenX - getRayon() / 2, screenY - getRayon() / 2, getRayon(), getRayon());
        }
    }


}
