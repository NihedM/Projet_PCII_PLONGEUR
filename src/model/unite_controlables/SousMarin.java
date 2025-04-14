package model.unite_controlables;


import model.objets.Position;
import model.objets.UniteControlable;
import view.ButtonAction;
import view.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;

public class SousMarin extends UniteControlable {

    private Plongeur boardedDiver;
    private int fuel;
    private final int MAX_FUEL = 100;   // Valeur max de carburant
    private final int BOOST_SPEED = 15;
    private boolean isBoosted = false;

    public SousMarin(Position position) {
        super(5, position, 75, 40, 100);    //à modifier
        this.fuel = MAX_FUEL; // Carburant initial

        setImage("sous-marin.png");
        setMovingImage("sous-marinMove.png");
        setUnitIcon(new ImageIcon(GamePanel.getCachedImage("sous-marin.png")));

        setScalingFactor(1.0);
        updateDimensions();

    }



    @Override
    public ConcurrentHashMap<String, String> getAttributes() {
        ConcurrentHashMap<String, String> attributes = super.getAttributes();
        attributes.put("Type", "Sous-marin");   // Attribut existant
        // Ajout de l'attribut Fuel avec sa valeur actuelle
        attributes.put("Fuel", String.valueOf(fuel));
        return attributes;
    }

    @Override
    public Color getColorForKey(String key) {
        if(key.equalsIgnoreCase("fuel"))
            return new Color(0, 128, 0); // Couleur verte pour le fuel
        return super.getColorForKey(key);
    }

    @Override
    public int getMaxValueForKey(String key) {
        if(key.equalsIgnoreCase("fuel"))
            return MAX_FUEL;  // MAX_FUEL est déjà défini (ici 100)
        return super.getMaxValueForKey(key);
    }


    public void boardDiver(Plongeur diver) {
        if (boardedDiver == null) { // Aucun plongeur déjà à bord
            boardedDiver = diver;
            // Rendre le plongeur invisible pour qu'il ne soit plus dessiné
            diver.setVisible(false);
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
                    if (Math.abs(getVx()) > 0.1 || Math.abs(getVy()) > 0.1) {
                        fuel--;
                    }
                }
                // Dès que le carburant est épuisé, débarquer le plongeur
                deboardDiver();
            }).start();
        }
    }



    public void deboardDiver() {
        isBoosted = false;
        setVitesseMax(0); // Sous-marin ne peut plus bouger
        setDestination(getPosition()); // On fige sa position

        if (boardedDiver != null) {
            Position submarinePos = this.getPosition();

            // Nouvelle position du plongeur : à côté du sous-marin
            Position newDiverPos = new Position(submarinePos.getX() + 20, submarinePos.getY());

            boardedDiver.setPosition(newDiverPos);
            boardedDiver.setVisible(true);
            boardedDiver.setDestination(null);

            // remettre le plongeur dans le jeu
            GamePanel.getInstance().addUniteControlable(boardedDiver);

            boardedDiver = null;
        }
    }

    public void rechargeFuel() {
        this.fuel = MAX_FUEL;
    }




}