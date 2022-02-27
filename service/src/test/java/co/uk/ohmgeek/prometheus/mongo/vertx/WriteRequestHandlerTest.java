package co.uk.ohmgeek.prometheus.mongo.vertx;

import co.uk.ohmgeek.prometheus.mongo.MongoTimeSeriesMetricStore;
import co.uk.ohmgeek.prometheus.mongo.handler.WriteRequestHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import prometheus.Remote;
import prometheus.Types;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

class WriteRequestHandlerTest {

    private MongoTimeSeriesMetricStore metricStore;
    private WriteRequestHandler sut;

    @BeforeEach
    public void before() {
        metricStore = mock(MongoTimeSeriesMetricStore.class);
        sut = new WriteRequestHandler(metricStore);
    }

    @Test
    public void shouldPassRequestToMongoStore() {
        // Given a request
        Remote.WriteRequest request = Remote.WriteRequest.newBuilder()
                .addTimeseries(Types.TimeSeries.newBuilder()
                        .addSamples(Types.Sample.newBuilder()
                                .setValue(10)
                                .setTimestamp(System.currentTimeMillis())
                                .build())
                        .build())
                .build();

        // When we serve.
        sut.serve(request);

        // Then, verify we write this once, and once only.
        verify(metricStore, times(1)).writeTimeSeries(same(request));

    }
}