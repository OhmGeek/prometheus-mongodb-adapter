package co.uk.ohmgeek.prometheus.mongo;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.util.concurrent.TimeUnit;

/**
 * A Guice module that provides Mongo details.
 */
public class MongoModule extends AbstractModule {

    @Override
    protected void configure() {
        super.configure();
        bind(DocumentAdapter.class).asEagerSingleton();
        bind(MongoTimeSeriesMetricStore.class).asEagerSingleton();
    }

    @Provides
    @Singleton
    public MongoClient provideMongoClient() {
        return MongoClients.create(MongoClientSettings.builder()
                .applicationName("prom-to-mongo-adapter")
                .credential(MongoCredential.createCredential("user", "admin", "changeit".toCharArray()))
                .applyConnectionString(new ConnectionString("mongodb://localhost:27017/admin"))
                .applyToSocketSettings((s) -> s.connectTimeout(30, TimeUnit.SECONDS))
                .build());
    }
}
