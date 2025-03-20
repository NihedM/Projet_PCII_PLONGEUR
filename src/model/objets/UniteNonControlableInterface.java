package model.objets;

import java.util.concurrent.CopyOnWriteArrayList;

public interface UniteNonControlableInterface {
    void setup(CopyOnWriteArrayList<Objet> interactionTargets);
    void action();
}
