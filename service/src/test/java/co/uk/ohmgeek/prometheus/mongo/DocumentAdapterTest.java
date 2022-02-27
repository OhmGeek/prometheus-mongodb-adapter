package co.uk.ohmgeek.prometheus.mongo;

import com.google.common.collect.ImmutableList;
import org.bson.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentAdapterTest {

    @Test
    public void shouldConvert() {
        DocumentAdapter sut = new DocumentAdapter();

        // Given
        long time = System.currentTimeMillis();
        BsonDocument bsonDocument = new BsonDocument();
        bsonDocument.put("stringVal", new BsonString("value"));
        bsonDocument.put("dateVal", new BsonDateTime(time));
        bsonDocument.put("arrayVal", new BsonArray(ImmutableList.of(new BsonString("a"), new BsonString("b"))));

        // When we convert
        Document converted = sut.fromBson(bsonDocument);
        // then it should contain the three fields.
        assertTrue(converted.containsKey("stringVal"));
        assertTrue(converted.containsKey("dateVal"));
        assertTrue(converted.containsKey("arrayVal"));

        assertEquals("value", converted.getString("stringVal"));
        assertEquals(time, converted.getDate("dateVal").getTime());
        assertEquals(2, converted.getList("arrayVal", String.class).size());
    }
}