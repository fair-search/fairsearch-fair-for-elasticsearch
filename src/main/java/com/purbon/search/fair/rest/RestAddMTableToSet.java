package com.purbon.search.fair.rest;

import com.purbon.search.fair.action.MTableStoreAction;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestStatusToXContentListener;

import java.io.IOException;

public class RestAddMTableToSet extends FairRestBaseHandler {

    private final String type;

    public RestAddMTableToSet(Settings settings, RestController controller, String type) {
        super(settings);
        this.type = type;
        controller.registerHandler(RestRequest.Method.POST, "/_fs/_mtable/{name}", this);
    }

    @Override
    public String getName() {
        return "Add an mtable to the set of tables";
    }

    /**
     * Prepare the request for execution. Implementations should consume all request params before
     * returning the runnable for actual execution. Unconsumed params will immediately terminate
     * execution of the request. However, some params are only used in processing the response;
     * implementations can override {@link BaseRestHandler#responseParams()} to indicate such
     * params.
     *
     * @param request the request to execute
     * @param client  client for executing actions on the local node
     * @return the action to execute
     * @throws IOException if an I/O exception occurred parsing the request and preparing for
     *                     execution
     */
    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {

        String routing = request.param("routing");

        if (!request.hasContentOrSourceParam()) {
            throw new IllegalArgumentException("Missing content or source param.");
        }

        String name  = request.param("name");
        float proportion = -1;
        float alpha = -1;

        MTableParamsParser parser = new MTableParamsParser();
        request.applyContentParser(parser::parse);
        proportion = parser.getProportion();
        alpha = parser.getAlpha();

        MTableStoreAction.MTableStoreRequestBuilder builder = MTableStoreAction.INSTANCE.newRequestBuilder(client);
        builder.request().setStore(indexName);
        builder.request().setName(name);
        builder.request().setProportion(proportion);
        builder.request().setAlpha(alpha);
        builder.request().setRouting(routing);

        return (channel) -> builder.execute(new RestStatusToXContentListener<>(channel, (r) -> r.getResponse().getLocation(routing)));
    }


    static class MTableParamsParser {
        public static final ObjectParser<MTableParamsParser, Void> PARSER = new ObjectParser<>("params");

        private static final ParseField PROPORTION_FIELD = new ParseField("proportion");
        private static final ParseField ALPHA_FIELD = new ParseField("alpha");


        private float proportion;
        private float alpha;

        static {
            PARSER.declareFloat(MTableParamsParser::setProportion, PROPORTION_FIELD);
            PARSER.declareFloat(MTableParamsParser::setAlpha, ALPHA_FIELD);
        }

        public void parse(XContentParser parser) throws IOException {
            PARSER.parse(parser, this, null);
        }

        public float getProportion() {
            return proportion;
        }

        public void setProportion(float proportion) {
            this.proportion = proportion;
        }

        public float getAlpha() {
            return alpha;
        }

        public void setAlpha(float alpha) {
            this.alpha = alpha;
        }
    }

}
