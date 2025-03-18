package model.objets;

import java.util.ArrayList;
import java.util.List;

public class GestionRessource extends Thread {
    private Ressource ressource;
    private int intervalle;
    private volatile boolean running = true;
    private List<RessourceListener> listeners = new ArrayList<>(); // Liste des listeners

    public GestionRessource(Ressource ressource, int intervalle) {
        this.ressource = ressource;
        this.intervalle = intervalle;
    }

    public void addListener(RessourceListener listener) {
        listeners.add(listener); // Ajouter un listener
    }

    public void stopThread() {
        running = false;
    }

    @Override
    public void run() {
        while (running && ressource.getEtat() != Ressource.Etat.DETRUIRE) {
            try {
                Thread.sleep(intervalle);
                ressource.evoluer();
                notifyListeners(); // Notifier les listeners après chaque évolution
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread interrupted");
            }
        }
    }

    private void notifyListeners() {
        for (RessourceListener listener : listeners) {
            listener.onRessourceUpdated(ressource); // Notifier chaque listener
        }
    }

    // Interface pour les listeners
    public interface RessourceListener {
        void onRessourceUpdated(Ressource ressource);
    }
}