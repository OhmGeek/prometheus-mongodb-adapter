package co.uk.ohmgeek.prometheus;

import com.mongodb.MongoCommandException;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.TimeSeriesGranularity;
import com.mongodb.client.model.TimeSeriesOptions;
import org.bson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import prometheus.Remote;
import prometheus.Types;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MongoTimeSeriesMetricStore {
    private static final Logger logger = LoggerFactory.getLogger(MongoTimeSeriesMetricStore.class);
    public static final String DATABASE = "prometheus";
    public static final String COLLECTION = "prometheus";
    private final MongoClient mongoClient;
    private final DocumentAdapter documentAdapter;

    public MongoTimeSeriesMetricStore(MongoClient mongoClient, DocumentAdapter documentAdapter) {
        this.mongoClient = mongoClient;
        this.documentAdapter = documentAdapter;
    }

    public static BsonDocument toMongoDocument(Types.TimeSeries timeSeries) {
        Types.Sample sample = timeSeries.getSamples(0);
        return new BsonDocument()
                .append("labels", toMongoArray(timeSeries.getLabelsList()))
                .append("timestamp", new BsonDateTime(sample.getTimestamp()))
                .append("value", new BsonDouble(sample.getValue()));
    }

    public static BsonArray toMongoArray(List<Types.Label> labels) {
        BsonArray output = new BsonArray();

        for (Types.Label label : labels) {
            output.add(new BsonDocument()
                    .append("name", new BsonString(label.getName()))
                    .append("value", new BsonString(label.getValue())));
        }
        return output;
    }

    public static BsonDocument samplesToBsonArray(List<Types.Sample> samples) {
        // We only get the first.
        Types.Sample sample = samples.get(0);
        return new BsonDocument()
                .append("value", new BsonDouble(sample.getValue()))
                .append("timestamp", new BsonDateTime(sample.getTimestamp()));
    }

    public void init() {
            logger.info("Creating collection");
            try {
                mongoClient.getDatabase(DATABASE)
                        .createCollection(COLLECTION, new CreateCollectionOptions()
                                .timeSeriesOptions(new TimeSeriesOptions("timestamp").metaField("labels").granularity(TimeSeriesGranularity.MINUTES))
                                .expireAfter(24, TimeUnit.HOURS));
            } catch (MongoCommandException e) {
                logger.info("skipping as collection exists");
            }
    }

    public void writeTimeSeries(Remote.WriteRequest request) {
        MongoCollection<Document> timeSeriesCollection = mongoClient.getDatabase(DATABASE)
                .getCollection(COLLECTION);

        List<InsertOneModel<Document>> collect = request.getTimeseriesList()
                .stream()
                .map(MongoTimeSeriesMetricStore::toMongoDocument)
                .map(documentAdapter::fromBson)
                .map(InsertOneModel::new)
                .toList();

        if (collect.size() > 1) {
            BulkWriteResult bulkWriteResult = timeSeriesCollection.bulkWrite(collect);

            logger.info("{} documents written", bulkWriteResult.getInsertedCount());

        } else {
            logger.info("Nothing to write. Ignoring.");
        }
    }
}
