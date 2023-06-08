package net.liyze.basin.core;

/**
 * Basin AIO server SPI
 */
public interface Server {
    /**
     * Stop the server.
     */
    void stop();

    /**
     * Start the server.
     */
    Server start();
}
