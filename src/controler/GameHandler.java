package controler;

import view.GamePanel;

public abstract class GameHandler extends Thread {
    protected volatile boolean running = true;
    protected static final int PAUSE_CHECK_INTERVAL = 50;

    @Override
    public void run() {
        ThreadManager.incrementThreadCount(getClass().getSimpleName());

        while (running) {
            handlePause();
            executeHandlerLogic();
            sleepIfNeeded();
        }

        ThreadManager.decrementThreadCount(getClass().getSimpleName());
    }

    protected void handlePause() {
        while (GamePanel.getInstance().isPaused() && running) {
            try {
                Thread.sleep(PAUSE_CHECK_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    protected abstract void executeHandlerLogic();

    protected void sleepIfNeeded() {
        try {
            Thread.sleep(getDelay());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running = false;
        }
    }

    protected abstract int getDelay();

    public void stopHandler() {
        running = false;
        this.interrupt();
    }
}