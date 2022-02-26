package co.uk.ohmgeek.prometheus.vertx;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Parser;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import java.io.IOException;

public abstract class BaseHandler<T extends AbstractMessage, R extends AbstractMessage> implements Handler<RoutingContext> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Parser<T> parser;
    private final boolean shouldSendResponse;

    public BaseHandler(Parser<T> requestParser, boolean shouldSendResponse) {
        this.parser = requestParser;
        this.shouldSendResponse = shouldSendResponse;
    }
    @Override
    public void handle(RoutingContext ctx) {
        try {
            byte[] uncompress = Snappy.uncompress(ctx.getBody().getBytes());
            T request = parser.parseFrom(uncompress);
            R response = serve(request);

            if (response != null && shouldSendResponse) {
                ctx.response()
                        .setStatusCode(200)
                        .setChunked(true)
                        .end(Buffer.buffer(Snappy.compress(response.toByteArray())));
            }
            logger.trace("Request received: {}", request);
        } catch (Throwable e) {
            // Any error will fail the request.
            logger.error("Failed to invoke handler, msg={}", e.getMessage());
            ctx.fail(e);
        }
    }

    public abstract R serve(T request);

}
