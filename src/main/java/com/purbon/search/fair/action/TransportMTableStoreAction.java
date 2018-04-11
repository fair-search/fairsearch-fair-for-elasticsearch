package com.purbon.search.fair.action;

import com.purbon.search.fair.ModelStore;
import com.purbon.search.fair.action.MTableStoreAction.MTableStoreRequest;
import com.purbon.search.fair.action.MTableStoreAction.MTableStoreResponse;
import com.purbon.search.fair.utils.MTableGenerator;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexAction;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.elasticsearch.action.ActionListener.wrap;

public class TransportMTableStoreAction extends HandledTransportAction<MTableStoreRequest, MTableStoreResponse> {


    private final ClusterService clusterService;
    private final Client client;

    @Inject
    public TransportMTableStoreAction(Settings settings, ThreadPool threadPool, TransportService transportService,
                                       ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver,
                                       ClusterService clusterService, Client client) {
        super(settings, MTableStoreAction.NAME, false, threadPool, transportService, actionFilters,
                indexNameExpressionResolver, MTableStoreRequest::new);
        this.clusterService = clusterService;
        this.client = client;
    }

    @Override
    protected void doExecute(MTableStoreRequest request, ActionListener<MTableStoreResponse> listener) {
        throw new UnsupportedOperationException("attempt to execute a TransportMTableStoreAction without a task");
    }

    @Override
    protected void doExecute(Task task, MTableStoreRequest request, ActionListener<MTableStoreResponse> listener) {
       // if (!clusterService.state().routingTable().hasIndex(request.getStore())) {
            // To prevent index auto creation
       //     throw new IllegalArgumentException("Store [" + request.getStore() + "] does not exist, please create it first.");
       // }

        if (request.getMtable() == null) {
            MTableGenerator gen = new MTableGenerator(request.getK(), request.getProportion(), request.getAlpha());
            request.setMtable(Arrays.stream(gen.getMTable()).boxed().collect(Collectors.toList()));
        }

        store(request, task, listener);
    }

    private void store(MTableStoreRequest request, Task task, ActionListener<MTableStoreResponse> listener) {

        try {
            IndexRequest indexRequest = buildIndexRequest(task, request);
            client.execute(IndexAction.INSTANCE, indexRequest, wrap(
                    (r) -> {
                        listener.onResponse(new MTableStoreResponse(r));
                    },
                    listener::onFailure));
        } catch (IOException ioe) {
            listener.onFailure(ioe);
        }
    }

    private IndexRequest buildIndexRequest(Task parentTask, MTableStoreRequest request) throws IOException {

        IndexRequest indexRequest = client.prepareIndex(request.getStore(), ModelStore.ES_TYPE, request.getId())
                .setCreate(request.getAction() == MTableStoreRequest.Action.CREATE)
                .setRouting(request.getRouting())
                .setSource(ModelStore.toSource(request))
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .request();
        indexRequest.setParentTask(clusterService.localNode().getId(), parentTask.getId());
        return indexRequest;
    }
}
