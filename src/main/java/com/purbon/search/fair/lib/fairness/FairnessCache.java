package com.purbon.search.fair.lib.fairness;

import com.purbon.search.fair.ModelStore;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FairnessCache {

    private final Client client;
    private ConcurrentHashMap<String, int[]> map;

    FairnessCache(Client client) {
        map = new ConcurrentHashMap<>();
        this.client = client;
    }

    public int[] get(String docId) {

        if (map.containsKey(docId)) {
            return map.get(docId);
        } else {
            int[] mtable = getById(docId);
            map.put(docId, mtable);
            return mtable;
        }
    }

    private int[] getById(String docId) {

        MatchQueryBuilder queryBuilder = new MatchQueryBuilder("_id", docId);

        SearchRequestBuilder srb = client.prepareSearch(ModelStore.STORE_NAME)
                .setQuery(queryBuilder)
                .setTypes(ModelStore.ES_TYPE);

        SearchResponse response = srb.execute().actionGet();
        SearchHits hits = response.getHits();

        if (hits.totalHits == 0) {
            throw new FairSearchCacheException("MTable (docId="+docId+") store document does not exist");
        }

        return findMTable(hits.getHits()[0]);
    }

    @SuppressWarnings("unchecked")
    private int[] findMTable(SearchHit hit) {

        List<Integer> tableAsList = ((List<Integer>)hit.getSourceAsMap().get("mtable"));
        return tableAsList.stream().mapToInt(i->i).toArray();
    }
}
