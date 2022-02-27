package co.uk.ohmgeek.prometheus.mongo;

import io.vertx.core.Vertx;

/**
 * The main class. This is what runs the adapter.
 */
public class Main {

    public static void main(String... args) throws InterruptedException {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new AdaptorVerticle());
    }
}
