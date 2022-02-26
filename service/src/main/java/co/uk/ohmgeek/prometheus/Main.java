package co.uk.ohmgeek.prometheus;

import io.vertx.core.Vertx;

public class Main {

    public static void main(String... args) throws InterruptedException {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new AdaptorVerticle());

    }
}
