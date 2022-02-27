package co.uk.ohmgeek.prometheus.mongo;

import org.bson.BsonDocument;
import org.bson.Document;

public class DocumentAdapter {

    public DocumentAdapter() {
    }

    // TODO: Avoid a copy operation here.
    public Document fromBson(BsonDocument bson) {
        return Document.parse(bson.toJson());
    }
}