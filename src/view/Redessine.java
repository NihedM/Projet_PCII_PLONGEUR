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

    public void run() {
        ThreadManager.incrementThreadCount("Redessine");
        long targetTime = 1000 / 60; // 60 FPS
        long startTime, elapsed, waitTime;

        while (running) {
            startTime = System.nanoTime();

            GamePanel.getInstance().repaint();

            elapsed = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - elapsed;

            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        ThreadManager.decrementThreadCount("Redessine");
    }
}

