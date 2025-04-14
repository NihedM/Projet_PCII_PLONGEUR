package model.ressources;

import model.objets.Position;
import model.objets.Ressource;

import java.awt.*;

public class Collier extends Ressource {
    public Collier(Position position) {

        super(position, 10,20, 20,"Collier ");
        setImage("collier.png");
    }

    @Override
    public void draw(Graphics2D g2d, Point screenPos) {
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(screenPos.x- getRayon(), screenPos.y- getRayon() , getRayon()*2, getRayon()*2);
        super.draw(g2d, screenPos);
    }

}
