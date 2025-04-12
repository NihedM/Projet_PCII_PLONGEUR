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
    private static final int DAMAGE = 100;
    private static final int SHOOTING_RANGE = 1000;
    private final int bayonetRange = getRayon()*2 + getRayon()/2;


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
    public boolean shoot(Unite target){
        if (ammo > 0) {
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



    public void attack(Enemy enemy) {
        if (ammo > 0) {
            if (this.distance(enemy) <= SHOOTING_RANGE) {
                if (shoot(enemy)) {
                    //enemy.takeDamage(DAMAGE);
                    System.out.println("Ennemi touché ! Munitions restantes: " + ammo);

                    if (!enemy.isAlive()) {
                        System.out.println("Ennemi éliminé !");
                        GamePanel.getInstance().killUnite(enemy);
                    }
                }
            } else {
                System.out.println("Ennemi trop loin !");
            }
        }else{
            //attaque au corps à corps
            setDestination(enemy.getPosition());
            if(this.distance(enemy) <= bayonetRange*2 ){
                enemy.takeDamage(DAMAGE*2);
                if (!enemy.isAlive())
                    GamePanel.getInstance().killUnite(enemy);
            }
        }

    }

    @Override
    public void stopAction() {
        super.stopAction();
        GamePanel.getInstance().setShootingMode(false);
    }
    @Override
    public List<ButtonAction> getButtonActions() {
        List<ButtonAction> actions = super.getButtonActions();

        actions.removeIf(a -> a.getLabel().contains("Récupérer"));

        actions.add(new ButtonAction("Attack (A)", e -> {
            GamePanel.getInstance().setShootingMode(true);

        }));

        return actions;
    }


}
