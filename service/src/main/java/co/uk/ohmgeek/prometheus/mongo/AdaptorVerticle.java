package co.uk.ohmgeek.prometheus.mongo;

import co.uk.ohmgeek.prometheus.mongo.handler.WriteRequestHandler;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.client.MongoClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class AdaptorVerticle extends AbstractVerticle {

    @Inject
    private MongoClient mongoClient;
    @Inject
    private DocumentAdapter documentAdapter;
    @Inject
    private MongoTimeSeriesMetricStore mongoTimeSeriesMetricStore;

    public AdaptorVerticle() {
        this(Guice.createInjector(new MongoModule()));
    }

    @VisibleForTesting
    AdaptorVerticle(Injector injector) {
        injector.injectMembers(this);
    }

    @Override
    public void start() throws Exception {
        super.start();

        mongoTimeSeriesMetricStore.init();
        HttpServer httpServer = vertx.createHttpServer(new HttpServerOptions().setPort(17017));
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Handle the write path - read the write request and for now just print out the BSON.
        router.route("/api/write").handler(new WriteRequestHandler(mongoTimeSeriesMetricStore));
        httpServer.requestHandler(router).listen();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        mongoClient.close();
    }
}
