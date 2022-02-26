package co.uk.ohmgeek.prometheus;

import org.bson.BsonDocument;
import org.bson.Document;

public class DocumentAdapter {

    public DocumentAdapter() {
    }

    public Document fromBson(BsonDocument bson) {
        return Document.parse(bson.toJson());
    }

    public BsonDocument toBson(Document document) {
        // This is shockingly bad. Fix this.
        return BsonDocument.parse(document.toJson());
    }
}