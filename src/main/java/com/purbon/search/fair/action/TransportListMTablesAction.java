package com.purbon.search.fair.action;

import com.purbon.search.fair.ModelStore;
import com.purbon.search.fair.action.ListMTablesAction.ListMTablesActionRequest;
import com.purbon.search.fair.action.ListMTablesAction.ListMTablesActionResponse;
import com.purbon.search.fair.action.ListMTablesAction.MTableInfo;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.master.TransportMasterNodeReadAction;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.elasticsearch.action.ActionListener.wrap;
import static org.elasticsearch.common.collect.Tuple.tuple;

public class TransportListMTablesAction  extends TransportMasterNodeReadAction<ListMTablesActionRequest, ListMTablesActionResponse> {

    private final Client client;

    @Inject
    public TransportListMTablesAction(Settings settings, TransportService transportService, ClusterService clusterService,
                                      ThreadPool threadPool, ActionFilters actionFilters,
                                      IndexNameExpressionResolver indexNameExpressionResolver, Client client) {
        super(settings, ListMTablesAction.NAME, transportService, clusterService,
                threadPool, actionFilters, indexNameExpressionResolver, ListMTablesActionRequest::new);
        this.client = client;
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.SAME;
    }

    @Override
    protected ListMTablesActionResponse newResponse() {
        return new ListMTablesActionResponse();
    }

    @Override
    protected void masterOperation(ListMTablesActionRequest request,
                                   ClusterState state,
                                   ActionListener<ListMTablesActionResponse> listener) {

        String[] indexNames = indexNameExpressionResolver.concreteIndexNames(state,
                new ClusterStateRequest().indices(ModelStore.STORE_NAME));

        final MultiSearchRequestBuilder requestBuilder = client.prepareMultiSearch();
        final List<Tuple<String, Integer>> versions = new ArrayList<>();

        Stream.of(indexNames)
                .filter(ModelStore::isStore)
                .map((s) -> clusterService.state().metaData().getIndices().get(s))
                .filter(Objects::nonNull)
                .forEach((m) -> {
                    requestBuilder.add(countSearchRequest(m));
                    versions.add(tuple(m.getIndex().getName(), 1));
                });

        if (versions.isEmpty()) {
            listener.onResponse(new ListMTablesActionResponse(Collections.emptyList()));
        } else {
            requestBuilder.execute(wrap((r) -> listener.onResponse(toResponse(r, versions)), listener::onFailure));
        }
    }

    private SearchRequestBuilder countSearchRequest(IndexMetaData meta) {
        return client.prepareSearch(meta.getIndex().getName())
                .setQuery(QueryBuilders.matchAllQuery())
                .setSize(0)
                .addAggregation(AggregationBuilders.terms("type").field("type").size(100));
    }

    private ListMTablesActionResponse toResponse(MultiSearchResponse response, List<Tuple<String, Integer>> versions) {
        assert versions.size() == response.getResponses().length;
        Iterator<Tuple<String, Integer>> vs = versions.iterator();
        Iterator<MultiSearchResponse.Item> rs = response.iterator();
        List<MTableInfo> infos = new ArrayList<>(versions.size());
        while (vs.hasNext() && rs.hasNext()) {
            MultiSearchResponse.Item it = rs.next();
            Tuple<String, Integer> idxAndVersion = vs.next();
            Map<String, Integer> counts = Collections.emptyMap();
            if (!it.isFailure()) {
                Terms aggs = it.getResponse()
                        .getAggregations()
                        .get("type");
                counts = aggs
                        .getBuckets()
                        .stream()
                        .collect(toMap(MultiBucketsAggregation.Bucket::getKeyAsString,
                                (b) -> (int) b.getDocCount()));
            }
            infos.add(new MTableInfo(idxAndVersion.v1(), idxAndVersion.v2(), counts));
        }
        return new ListMTablesActionResponse(infos);
    }

    @Override
    protected ClusterBlockException checkBlock(ListMTablesActionRequest request, ClusterState state) {
        return null;
    }
}
