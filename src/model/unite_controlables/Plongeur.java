package model.unite_controlables;

import controler.FuiteHandler;
import model.gains_joueur.Referee;
import model.objets.Position;
import model.objets.Ressource;
import model.objets.UniteControlable;
import model.unite_non_controlables.Calamar;
import view.ButtonAction;
import view.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Plongeur extends UniteControlable {
    private HashMap<model.objets.Ressource, Integer> sac; // Sac pour stocker les ressources et leurs quantités
    private static final int CAPACITE_SAC = 10, MAX_HP  = 100, COOLDOWN_FUITE = 30, MAX_OXYGEN = 100, MAX_STAMINA = 100; // Capacité maximale du sac, vie maximale du plongeur, 30 secondes de cooldown pour la fuite
    private int rayonFuite, oxygen, stamina; // Rayon de fuite du plongeur, niveau d'oxygène
    private boolean faitFuire;

    private List<Ressource> backpack;
    private static final int BACKPACK_CAPACITY = 4;
    private Ressource targetResource;

    private boolean visible = true;

    public Plongeur(int id, Position position) {
        super(id, position, 20, 10, MAX_HP);
        sac = new HashMap<>();
        this.rayonFuite = 50;
        this.faitFuire = false;
        this.oxygen = MAX_OXYGEN;
        this.stamina = MAX_STAMINA;
        this.backpack = new ArrayList<>();

        setImage("plongeur.png");
        setMovingImage("plongeur.gif");
        setUnitIcon(new ImageIcon(GamePanel.getCachedImage("plongeurIcon.png")));



    }

    public List<Ressource>  getBackPac() {
        return backpack;
    }

    public int getRayonFuite() {
        return rayonFuite;
    }

    public boolean isFaitFuire() {
        return faitFuire;
    }
    public int getCurrentOxygen() {
        return oxygen;
    }

    public int getCurrentStamina() {
        return stamina;
    }

    @Override
    public String getInfo() {
        return super.getInfo() + ", Oxygen: " + getCurrentOxygen() + getCurrentStamina();
    }

    @Override
    public ConcurrentHashMap<String, String> getAttributes() {
        ConcurrentHashMap<String, String> attributes = super.getAttributes();
        attributes.put("Oxygen", String.valueOf(getCurrentOxygen()));
        attributes.put("Stamina", String.valueOf(getCurrentStamina()));
        attributes.put("Backpack", String.valueOf(backpack.size()));
        return attributes;
    }

    @Override
    public synchronized Color getColorForKey(String key) {
        return switch (key.toLowerCase()) {
            case "oxygen" -> new Color(0, 100, 200); // Bleu profond
            case "stamina" -> new Color(255, 165, 0); // Orange vif
            case "backpack" -> new Color(128, 0, 128); // Violet
            default -> super.getColorForKey(key);
        };
    }
    @Override
    public synchronized int  getMaxValueForKey(String key) {
        return switch (key.toLowerCase()) {
            case "oxygen" -> MAX_OXYGEN;
            case "stamina" -> MAX_STAMINA;
            case "backpack" -> BACKPACK_CAPACITY;
            default -> super.getMaxValueForKey(key);
        };
    }



    //-------------------Méthodes-------------------



    public void setCurrentOxygen(int oxygen) {
        if(oxygen > MAX_OXYGEN) return;

        this.oxygen = oxygen;
    }

    public double getVitesseXStamina(){
        double curve = 10;
        double exponent = -curve * (stamina - 20) / 100;
        return getVitesseMax() / (1 + Math.exp(exponent));
    }
    public void setCurrentStamina(int stamina) {
        if(stamina > MAX_STAMINA) return;
        if (stamina < 0) stamina = 0; // Ensure stamina does not go negative

        this.stamina = stamina;
        // Adjust speed based on current stamina using a quadratic function
        setVitesseCourante(
                Math.min(getVitesseCourante(), getVitesseXStamina() )
        );

        if(stamina <= 0) {
            setDestination(null);
        }
    }

    public void setFaitFuire(boolean faitFuire) {
        this.faitFuire = faitFuire;
        if (faitFuire) {
            FuiteHandler.getInstance().addPlongeur(this);
        } else {
            FuiteHandler.getInstance().removePlongeur(this);
        }
    }










    public void faireFuirCalamar(Calamar calamar) {
        calamar.fuit();
    }


    public Ressource getTargetResource() {
        return targetResource;
    }
    public void setTargetResource(Ressource resource) {
        this.targetResource = resource;
    }

    // Méthode pour collecter une ressource lorsqu'on est à proximité
    public boolean recolter(Ressource ressource) {
        // Vérifier que l’on est bien à portée (on se base sur le rayon de l’unité et celui de la ressource)
        int dx = this.getPosition().getX() - ressource.getPosition().getX();
        int dy = this.getPosition().getY() - ressource.getPosition().getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        if(distance <= this.getRayon() + ressource.getRayon()) {
            if(backpack.size() < BACKPACK_CAPACITY) {
                // Si la ressource est un Coffre, on arrête le jeu et on déclare la victoire
                if (ressource instanceof model.ressources.Coffre) {
                    System.out.println("Coffre recolté ! Vous avez gagné !");
                    GamePanel.getInstance().removeObjet(ressource, ressource.getCoordGrid());
                    GamePanel.getInstance().getVictoryManager().triggerVictory();
                    return true;
                }
                backpack.add(ressource);
                // Retirer la ressource du jeu
                GamePanel.getInstance().removeObjet(ressource, ressource.getCoordGrid());
                System.out.println("Ressource " + ressource.getNom() + " collectée. Taille du backpack : " + backpack.size());
                return true;
            } else {
                System.out.println("Sac à dos plein !");
                JOptionPane.showMessageDialog(null, "Sac à dos plein !");
                return false;
            }
        }
        return false;
    }





    public Ressource seFaitVoler(){
        if(backpack.isEmpty()) return null;

        Ressource ressource = backpack.get(0);
        backpack.remove(0);
        return ressource;
    }



    // Méthode pour livrer le contenu du backpack à la base (transfert vers le market)
    public void deliverBackpack() {
        if(!backpack.isEmpty()) {
            for(Ressource res : backpack) {
                GamePanel.getInstance().addCollectedResource(res);
                // On pourrait ajouter ici un système de points ou d'argent ???
            }
            backpack.clear();
            System.out.println("Backpack livré au market.");
            JOptionPane.showMessageDialog(null, "Backpack livré !");
        }
    }




    //--------------------logique des actions--------------------




    public void stopAction() {
        super.stopAction();
        if(faitFuire) {
            setFaitFuire(false);
        }
        GamePanel.getInstance().setRecuperationMode(false);
    }

    public List<ButtonAction> getButtonActions() {
        List<ButtonAction> actions = super.getButtonActions();


        actions.add(new ButtonAction("Récupérer (R)", e -> {
            GamePanel.getInstance().setRecuperationMode(true);
        }));

        actions.add(new ButtonAction("Faire fuire (F)", e -> {
            GamePanel gamePanel = GamePanel.getInstance();
            if (gamePanel != null) {
                for (UniteControlable unite : gamePanel.getUnitesSelected()) {
                    if (unite instanceof Plongeur plongeur) {
                        plongeur.setFaitFuire(true);
                    }
                }
            }
        }));


        //ajouter des actions pour le plongeur ici

        return actions;
    }




    public void setVisible(boolean visible) {
        this.visible = visible;
    }






    //-------------------------------------------------------------------------------
    @Override
    public void draw(Graphics2D g2d, Point screenPos) {
        if (isFaitFuire()) {
            // setcolor to  Transparent orange
            g2d.setColor(new Color(255, 165, 0, 100)); // Orange transparent

            g2d.fillOval(
                    screenPos.x - getRayonFuite(),
                    screenPos.y - getRayonFuite(),
                    getRayonFuite() * 2,
                    getRayonFuite() * 2
            );
        }

        // Call the parent class's draw method to render the unit
        super.draw(g2d, screenPos);
    }

    public int getDetectionRange() {
        return 1000;
    }
}
