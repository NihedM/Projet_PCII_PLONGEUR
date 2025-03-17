package model.objets;

import model.unite_non_controlables.Enemy;

import java.util.TimerTask;

public class Fuite extends TimerTask {

    private final Enemy enemy;

    public Fuite(Enemy enemy) {
        this.enemy = enemy;
    }

    @Override
    public void run() {
        enemy.fuit();
    }

}
