package model.unite_controlables;


import model.objets.Position;
import model.objets.UniteControlable;

import java.util.concurrent.ConcurrentHashMap;

public class SousMarin extends UniteControlable {
    // Plongeur actuellement à bord, null s'il n'y en a pas
    private Plongeur boardedDiver;

    // Carburant disponible pour se déplacer (essence)
    private int fuel;
    private final int MAX_FUEL = 100;   // Valeur max de carburant
    // Vitesse "normale" et vitesse boostée pour le sous-marin
    private final int NORMAL_SPEED = 10;
    private final int BOOST_SPEED = 15;

    // Indique si le sous-marin est en mode déplacement accéléré (lorsqu'un plongeur est à bord)
    private boolean isBoosted = false;
        public SousMarin(Position position) {
            super(5, position, 10, 40, 100);    //à modifier
            this.fuel = MAX_FUEL; // Carburant initial
        }



        @Override
        public ConcurrentHashMap<String, String> getAttributes() {
            ConcurrentHashMap<String, String> attributes = super.getAttributes();
            attributes.put("Type", "Sous-marin");   // à modifier ?
            return attributes;
        }


    public void boardDiver(Plongeur diver) {
        if (boardedDiver == null) { // Aucun plongeur déjà à bord
            boardedDiver = diver;
            // Rendre le plongeur invisible pour qu'il ne soit plus dessiné
            diver.setVisible(false);
            // Passer en mode boosté et augmenter la vitesse
            setVitesseMax(BOOST_SPEED);
            isBoosted = true;
            // Démarrer la consommation de carburant
            new Thread(() -> {
                while (isBoosted && fuel > 0) {
                    try {
                        Thread.sleep(1000); // Attendre 1 seconde
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    fuel -= 10; // Consommation par seconde (ajustez si nécessaire)
                }
                // Dès que le carburant est épuisé, débarquer le plongeur
                deboardDiver();
            }).start();
        }
    }




    public void deboardDiver() {
        // Arrête le mode boosté et fixe la vitesse à 0 pour bloquer le déplacement
        isBoosted = false;
        setVitesseMax(0); // La vitesse est maintenant nulle : le sous-marin ne bougera plus
        setDestination(getPosition()); // On fixe la destination à sa position actuelle

        // Si un plongeur est à bord, le débarrer et le rendre visible
        if (boardedDiver != null) {
            Position submarinePos = this.getPosition();
            // Exemple : placer le plongeur avec un léger décalage par rapport au sous-marin
            Position newDiverPos = new Position(submarinePos.getX() + 10, submarinePos.getY());
            boardedDiver.setPosition(newDiverPos);
            boardedDiver.setVisible(true);
            boardedDiver = null;
        }
    }



}




