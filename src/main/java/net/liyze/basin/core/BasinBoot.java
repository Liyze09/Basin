package net.liyze.basin.core;

/**
 * Basin's Plugin/Boot SPI
 */
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
