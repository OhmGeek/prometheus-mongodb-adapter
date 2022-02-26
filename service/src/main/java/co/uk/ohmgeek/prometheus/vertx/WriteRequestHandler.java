package co.uk.ohmgeek.prometheus.vertx;

import co.uk.ohmgeek.prometheus.MongoTimeSeriesMetricStore;
import prometheus.Remote;
import prometheus.Types;

public class WriteRequestHandler extends BaseHandler<Remote.WriteRequest, Types.NullMessage> {
    private final MongoTimeSeriesMetricStore mongoTimeSeriesMetricStore;

    public WriteRequestHandler(MongoTimeSeriesMetricStore mongoTimeSeriesMetricStore) {
        super(Remote.WriteRequest.parser(), false);
        this.mongoTimeSeriesMetricStore = mongoTimeSeriesMetricStore;
    }
    @Override
    public Types.NullMessage serve(Remote.WriteRequest request) {
        mongoTimeSeriesMetricStore.writeTimeSeries(request);
        return Types.NullMessage.newBuilder().build();
    }
}
