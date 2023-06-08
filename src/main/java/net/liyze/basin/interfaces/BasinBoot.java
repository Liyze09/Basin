package net.liyze.basin.interfaces;

public interface BasinBoot {
    /**
     * Runs on basin loading app jar
     */
    default void onStart() {
    }

    /**
     * Runs after basin starting
     */
    default void afterStart() {
    }

    /**
     * Runs before basin stopping
     */
    default void beforeStop() {
    }
}
