package co.uk.ohmgeek.prometheus;

import co.uk.ohmgeek.prometheus.vertx.WriteRequestHandler;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.concurrent.TimeUnit;

public class AdaptorVerticle extends AbstractVerticle {

    private MongoClient mongoClient;

    @Override
    public void start() throws Exception {
        super.start();

        mongoClient = MongoClients.create(MongoClientSettings.builder()
                .applicationName("prom-to-mongo-adapter")
                .credential(MongoCredential.createCredential("user", "admin", "changeit".toCharArray()))
                .applyConnectionString(new ConnectionString("mongodb://localhost:27017/admin"))
                .applyToSocketSettings((s) -> s.connectTimeout(30, TimeUnit.SECONDS))
                .build());

        DocumentAdapter documentAdapter = new DocumentAdapter();
        MongoTimeSeriesMetricStore mongoTimeSeriesMetricStore
                = new MongoTimeSeriesMetricStore(mongoClient, documentAdapter);

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
