package model.objets;

public class GestionRessource extends Thread {
    private Ressource ressource;
    private int intervalle;
    private volatile boolean running = true;

    public GestionRessource(Ressource ressource, int intervalle) {
        this.ressource = ressource;
        this.intervalle = intervalle;
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
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread interrupted");
            }
        }
    }
}
