package com.purbon.search.fair.rest;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestStatusToXContentListener;

import java.io.IOException;

import static com.purbon.search.fair.ModelStore.ES_TYPE;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class RestMTableStore extends FairRestBaseHandler {

    private final String type;


    public RestMTableStore(Settings settings, RestController controller, String type) {
        super(settings);
        this.type = type;
        controller.registerHandler(RestRequest.Method.GET, "/_fs/_mtable", this);

    }

    @Override
    public String getName() {
        return "MTable store management";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        if (request.method() == RestRequest.Method.HEAD || request.method() == RestRequest.Method.GET) {
            return search(client, indexName, request);
        } else {
            throw new ElasticsearchException("Invalid request type");
        }
    }

    private RestChannelConsumer search(NodeClient client, String indexName, RestRequest request) {

        int from = request.paramAsInt("from", 0);
        int size = request.paramAsInt("size", 20);
        MatchAllQueryBuilder qb = new MatchAllQueryBuilder();

        return (channel) -> client.prepareSearch(indexName)
                .setTypes(type)
                .setQuery(qb)
                .setSize(size)
                .setFrom(from)
                .execute(new RestStatusToXContentListener<>(channel));

    }

}
