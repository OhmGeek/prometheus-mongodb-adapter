package co.uk.ohmgeek.prometheus.mongo;

import com.google.inject.*;
import com.google.inject.util.Modules;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.xerial.snappy.Snappy;
import prometheus.Remote;
import prometheus.Types;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static co.uk.ohmgeek.prometheus.mongo.MongoTimeSeriesMetricStore.COLLECTION;
import static co.uk.ohmgeek.prometheus.mongo.MongoTimeSeriesMetricStore.DATABASE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("rawtypes")
public class AdaptorVerticleTest {

    private Vertx vertx;
    private MongoClient mongoClient;
    private MongoCollection<Document> collection;
    @BeforeEach
    public void before() {
        vertx = Vertx.vertx();
        mongoClient = mock(MongoClient.class);

        MongoDatabase database = mock(MongoDatabase.class);
        collection = mock(MongoCollection.class);
        when(mongoClient.getDatabase(DATABASE)).thenReturn(database);
        when(database.getCollection(COLLECTION)).thenReturn(collection);
        Injector injector = Guice.createInjector(Modules.override(new MongoModule()).with(new AbstractModule() {
            @Provides
            @Singleton
            public MongoClient provideMongoClient() {
                return mongoClient;
            }
        }));
        vertx.deployVerticle(new AdaptorVerticle(injector));
    }

    @Test
    public void shouldHandleWriteApi() throws IOException, ExecutionException, InterruptedException {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        when(collection.bulkWrite(captor.capture()))
                .thenReturn(BulkWriteResult.acknowledged(1, 0, 0, 1, null, null));

        // Given a write request
        WebClient webClient = WebClient.create(vertx);
        Remote.WriteRequest request = Remote.WriteRequest.newBuilder()
                .addTimeseries(Types.TimeSeries.newBuilder()
                        .addSamples(Types.Sample.newBuilder()
                                .setValue(5.0)
                                .setTimestamp(System.currentTimeMillis())
                                .build())
                        .addLabels(Types.Label.newBuilder().setName("__name__").setValue("metric").build())
                        .build())
                .build();

        // When we send the request
        HttpResponse<Buffer> response = webClient.post(17017, "localhost", "/api/write")
                .sendBuffer(Buffer.buffer(Snappy.compress(request.toByteArray())))
                .toCompletionStage()
                .toCompletableFuture()
                .get();

        // Verify we get a 200 status code.
        assertEquals(200, response.statusCode());

        // Also verify we make a bulk write call to Mongo
        Mockito.verify(mongoClient, times(2)).getDatabase(DATABASE);

        List mongoWriteList = captor.getValue();
        // We should write a single document using the "insert one" model.
        assertEquals(1, mongoWriteList.size());
        assertInstanceOf(InsertOneModel.class, mongoWriteList.get(0));

        Document document = ((InsertOneModel<Document>) mongoWriteList.get(0)).getDocument();

        assertTrue(document.containsKey("timestamp"));
        assertTrue(document.containsKey("value"));
        assertTrue(document.containsKey("labels"));
        assertEquals(5.0, document.getDouble("value"));
    }

    @AfterEach
    public void after() {
        vertx.close();
    }
}
