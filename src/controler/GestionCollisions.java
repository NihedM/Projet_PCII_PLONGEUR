package controler;

import model.objets.Objet;

public class GestionCollisions{

    /*detection de 2 objets circulaires en collision
    en sortie:
        -1: si pas de collision
        0: si collision
        1: si a est incluse dans b
        2: si b est incluse dans a
     */

    public static int collisionCC(Objet a, Objet b){
        int x1 = a.getPosition().getX();
        int y1 = a.getPosition().getY();
        int x2 = b.getPosition().getX();
        int y2 = b.getPosition().getY();
        int r1 = a.getRayon();
        int r2 = b.getRayon();

        double d = Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
        if(d > Math.pow(r1 + r2, 2)) return -1;
        if(r1 > r2 && d < Math.pow(r1 -r2, 2)) return 1;
        if(r2 > r1 && d < Math.pow(r2 -r1, 2)) return 1;
        return 0;
    }

    //todo collsion entre un objet circulaire et un objet rectangulaire





    //temporaire
    public static void preventOverlap(Objet a, Objet b, int c){
        if(collisionCC(a,b) == -1) return;
        int aX = a.getPosition().getX(), aY = a.getPosition().getY(), bX = b.getPosition().getX(), bY = b.getPosition().getY();
        int rayon = Math.max(a.getRayon(), b.getRayon());

        if(aX < bX) a.getPosition().setX(bX - rayon);
        else a.getPosition().setX(bX + rayon);

        if(aY < bY) a.getPosition().setY(bY - rayon);
        else a.getPosition().setY(bY + rayon);

    }


}

