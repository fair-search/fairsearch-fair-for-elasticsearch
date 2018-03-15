package com.purbon.search.fair;

import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Table;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.admin.cluster.RestNodesUsageAction;
import org.elasticsearch.rest.action.cat.RestTable;

import java.io.IOException;

public class PlainTextRestAction extends BaseRestHandler {


    protected PlainTextRestAction(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(RestRequest.Method.GET, "_explainME", this);
    }

    /**
     * @return the name of this handler. The name should be human readable and
     * should describe the action that will performed when this API is
     * called. This name is used in the response to the
     * {@link RestNodesUsageAction}.
     */
    @Override
    public String getName() {
        return "This is a plan text rest handler";
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
        GetIndexRequest indexRequest = new GetIndexRequest();
        String[] indices = client.admin().indices().getIndex(indexRequest).actionGet().indices();
        return restChannel -> {
            RestTable.buildResponse(getTableWithHeader(request, indices), restChannel);
        };
    }

    protected Table getTableWithHeader(final RestRequest request, String[] indices) {
        Table table = new Table();
        for(int i=0; i < indices.length; i++) {
            table.startRow();
            table.addCell("name", indices[i]);
            table.addCell("count", ""+i);
            table.endRow();
        }
        table.endHeaders();
        return table;
    }
}
