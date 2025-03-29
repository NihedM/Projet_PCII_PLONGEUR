package controler;

import model.constructions.Base;
import model.objets.Objet;
import model.objets.Unite;
import model.unite_controlables.Plongeur;
import model.unite_non_controlables.Enemy;

public class GestionCollisions{

    /*detection de 2 objets circulaires en collision
    en sortie:
        -1: si pas de collision
        0: si collision
        1: si a est incluse dans b
        2: si b est incluse dans a
     */

    private static int collision(int x1, int x2, int y1, int y2, int r1, int r2){

        double d = Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
        if(d > Math.pow(r1 + r2, 2)) return -1;
        if(r1 > r2 && d < Math.pow(r1 -r2, 2)) return 1;
        if(r2 > r1 && d < Math.pow(r2 -r1, 2)) return 2;
        return 0;

    }
    public static int collisionCC(Objet a, Objet b){
        if (a == null || b == null)
            return -1;
        int x1 = a.getPosition().getX();
        int y1 = a.getPosition().getY();
        int x2 = b.getPosition().getX();
        int y2 = b.getPosition().getY();
        int r1 = a.getRayon();
        int r2 = b.getRayon();

        return  collision(x1,x2,y1, y2, r1, r2);
    }

    public static int collisionPerimetreFuite(Plongeur p, Enemy e){
        return collision(p.getPosition().getX(), e.getPosition().getX(), p.getPosition().getY(), e.getPosition().getY(), p.getRayonFuite(),e.getRayon());
    }



    //collsion entre un objet circulaire et un objet rectangulaire


    public static boolean estDans(int xA, int yA, int xB, int yB, int x, int y){
        int xmin = Math.min(xA, xB),
                xmax = Math.max(xA, xB),
                ymin = Math.min(yA, yB),
                ymax = Math.max(yA, yB);

        return xmin <= x && x <= xmax && ymin <= y && y <= ymax;
    }





        public static void rebound(Unite a, Unite b) {
        // Calcul de la normale
        double normalX = b.getPosition().getX() - a.getPosition().getX();
        double normalY = b.getPosition().getY() - a.getPosition().getY();
        double magnitude = Math.sqrt(normalX * normalX + normalY * normalY);
        normalX /= magnitude;
        normalY /= magnitude;

        // Produit scalaire pour projeter la vitesse sur la normale
        double relativeVelocityX = a.getVx() - b.getVx();
        double relativeVelocityY = a.getVy() - b.getVy();
        double dotProduct = relativeVelocityX * normalX + relativeVelocityY * normalY;

        a.setVx(a.getVx() - 2 * dotProduct * normalX);
        a.setVy(a.getVy() - 2 * dotProduct * normalY);
        b.setVx(b.getVx() + 2 * dotProduct * normalX);
        b.setVy(b.getVy() + 2 * dotProduct * normalY);


        /*// Échange des vitesses projetées (simplification sans conservation d'énergie)
        double newVx = a.getVx() - dotProduct * nx + dotProductOther * nx;
        double newVy = a.getVy() - dotProduct * ny + dotProductOther * ny;
        double newVxOther = b.getVx() - dotProductOther * nx + dotProduct * nx;
        double newVyOther = b.getVy() - dotProductOther * ny + dotProduct * ny;

        a.setVx(newVx);
        a.setVy(newVy);
        b.setVx(newVxOther);
        b.setVy(newVyOther);*/

    }



    public static void preventOverlap(Objet a, Objet b){
        double dx = b.getPosition().getX() - a.getPosition().getX();
        double dy = b.getPosition().getY() - a.getPosition().getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        double overlap = a.getRayon() + b.getRayon() - distance;

        // Normalize the direction vector
        dx /= distance;
        dy /= distance;

        // Move objects apart based on their velocities
        a.getPosition().setX(a.getPosition().getX() - (int) (dx * overlap ));
        a.getPosition().setY(a.getPosition().getY() - (int) (dy * overlap ));
        if (!(b instanceof Base)) {
            b.getPosition().setX(b.getPosition().getX() + (int) (dx * overlap));
            b.getPosition().setY(b.getPosition().getY() + (int) (dy * overlap));
        }



    }


}

