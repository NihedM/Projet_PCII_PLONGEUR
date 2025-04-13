package model.unite_controlables;

import model.objets.*;
import model.unite_non_controlables.Enemy;
import view.ButtonAction;
import view.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlongeurArme extends Plongeur {

    private static final int MAX_AMMO = 10;
    public final int DAMAGE = 10, DAMAGEATTTACK = 2;
    private static final int SHOOTING_RANGE = 1000;
    private Unite target;


    private int ammo;
    private boolean hasWeapon;

    public PlongeurArme(int id, Position position) {
        super(id, position);
        this.ammo = MAX_AMMO;
        this.hasWeapon = true;

        setImage("plongeurArme.png");
        setMovingImage("plongeurArme.gif");
        setUnitIcon(new ImageIcon(GamePanel.getCachedImage("plongeurArmeIcon.png")));
    }
    public int getAmmo(){return ammo;}
    public void reload(int amount){ammo = Math.min(ammo + amount, MAX_AMMO);}
    public boolean shoot(Position target){
        if (ammo > 0 && this.distance(target) <= SHOOTING_RANGE) {
            ammo--;
            // Créer une nouvelle instance de la balle
            Ammo ammoInstance = new Ammo(new Position(getPosition().getX(),getPosition().getY() ), 5, target, DAMAGE, 10);
            return true;
        }
        return false;
    }

    @Override
    public String getInfo() {return super.getInfo() + ", Munitions: " + getAmmo();}

    @Override
    public ConcurrentHashMap<String, String> getAttributes() {
        ConcurrentHashMap<String, String> attributes = super.getAttributes();
        attributes.put("Ammo", String.valueOf(getAmmo()));
        return attributes;
    }
    @Override
    public synchronized Color getColorForKey(String key) {
        if (key.equalsIgnoreCase("Ammo")) {
            return new Color(255, 0, 0); // Rouge
        }
        return super.getColorForKey(key);
    }


    @Override
    public synchronized int getMaxValueForKey(String key) {
        if (key.equalsIgnoreCase("Ammo")) {
            return MAX_AMMO;
        }
        return super.getMaxValueForKey(key);
    }

    //-------------------------------------------------------------------------------


    public void setTarget(Unite target) {
        this.target = target;
    }

    public Unite getTarget() {
        return target;
    }

    //attaque au corps à corps
    public void attack(Enemy enemy) {
        setTarget(enemy);
        setDestination(enemy.getPosition());

    }

    @Override
    public void stopAction() {
        super.stopAction();
        GamePanel.getInstance().setAttackinggMode(false);
    }
    @Override
    public List<ButtonAction> getButtonActions() {
        List<ButtonAction> actions = super.getButtonActions();

        actions.removeIf(a -> a.getLabel().contains("Récupérer"));

        actions.add(new ButtonAction("Attack (A)", e -> {
            GamePanel.getInstance().setAttackinggMode(true);

        }));

        return actions;
    }


}
