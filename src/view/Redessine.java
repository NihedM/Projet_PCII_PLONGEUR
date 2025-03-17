package view;

import controler.ThreadManager;

public class Redessine extends Thread {
    private view.GamePanel panel;
    private static final int DELAY = 60;
    private volatile boolean running = true;

    public Redessine() {

    }

    public void stopThread() {
        running = false;
    }

    @Override
    public void run() {
        ThreadManager.incrementThreadCount("Redessine");
        while (running) {
            GamePanel.getInstance().repaint();
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread interrupted");
            }
        }
        ThreadManager.decrementThreadCount("Redessine");
    }
}

