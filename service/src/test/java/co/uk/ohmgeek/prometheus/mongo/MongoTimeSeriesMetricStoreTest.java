package co.uk.ohmgeek.prometheus.mongo;

import com.google.common.collect.ImmutableList;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.junit.jupiter.api.Test;
import prometheus.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MongoTimeSeriesMetricStoreTest {

    @Test
    public void canConvertSingleSample() {
        long timestamp = System.currentTimeMillis();
        Types.Sample sample = Types.Sample.newBuilder()
                .setTimestamp(timestamp)
                .setValue(5.0)
                .build();
        BsonDocument bson = MongoTimeSeriesMetricStore.samplesToBsonArray(ImmutableList.of(sample));

        assertEquals(timestamp, bson.getDateTime("timestamp").getValue());
        assertEquals(5.0, bson.getDouble("value").doubleValue());
    }

    @Test
    public void shouldOnlyConvertFirstSample() {
        long timestamp = System.currentTimeMillis();
        Types.Sample sample1 = Types.Sample.newBuilder()
                .setTimestamp(timestamp)
                .setValue(5.0)
                .build();

        Types.Sample sample2 = Types.Sample.newBuilder()
                .setTimestamp(timestamp)
                .setValue(6.0)
                .build();
        BsonDocument bson = MongoTimeSeriesMetricStore.samplesToBsonArray(ImmutableList.of(sample1, sample2));

        // Only the first one should be there.
        assertEquals(timestamp, bson.getDateTime("timestamp").getValue());
        assertEquals(5.0, bson.getDouble("value").doubleValue());
    }

    @Test
    public void shouldConvertLabels() {
        Types.Label label1 = Types.Label.newBuilder()
                .setName("label1")
                .setValue("value1")
                .build();
        Types.Label label2 = Types.Label.newBuilder()
                .setName("label1")
                .setValue("value1")
                .build();
        BsonArray bsonValues = MongoTimeSeriesMetricStore.toMongoArray(ImmutableList.of(label1, label2));

        // Verify the conversion.
        assertEquals(2, bsonValues.size());
    }
}