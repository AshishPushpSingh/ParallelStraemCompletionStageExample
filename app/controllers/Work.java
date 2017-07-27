package controllers;

import play.Logger;

/**
 * Created by Ashish Pushp Singh on 27/7/17.
 */

public class Work {
    private final int duration;
    public Work(int duration) {
        this.duration = duration;
    }
    public int calculate() {
        Logger.info(Thread.currentThread().getName());
        try {
            Thread.sleep(duration * 1000);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
        return duration;
    }
}
