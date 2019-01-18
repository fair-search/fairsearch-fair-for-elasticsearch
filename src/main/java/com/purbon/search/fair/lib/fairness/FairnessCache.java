package com.purbon.search.fair.lib.fairness;

import com.purbon.search.fair.ModelStore;
import com.purbon.search.fair.utils.MTableGenerator;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FairnessCache {

    private static Logger logger = ESLoggerFactory.getLogger(FairnessCache.class);

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
        GetResponse response = client
                .prepareGet(ModelStore.STORE_NAME, ModelStore.ES_TYPE, docId)
                .get();

        if (!response.isExists()) {
            throw new FairSearchCacheException("MTable (docId="+docId+") store document does not exist");
        }

        return findMTable(response);
    }


    public int[] get(int k, float p, float a) {
        String id = ModelStore.generateId(p,a,k);
        if (map.containsKey(id)) {
            return map.get(id);
        } else {
            int[] mtable = generateMtable(k, p ,a, id);
            map.put(id, mtable);
            return mtable;
        }

    }

    @SuppressWarnings("unchecked")
    private int[] findMTable(SearchHit hit) {
        List<Integer> tableAsList = ((List<Integer>)hit.getSourceAsMap().get("mtable"));
        return tableAsList.stream().mapToInt(i->i).toArray();
    }

    @SuppressWarnings("unchecked")
    private int[] findMTable(GetResponse response) {
        List<Integer> tableAsList = ((List<Integer>)response.getSourceAsMap().get("mtable"));
        return tableAsList.stream().mapToInt(i->i).toArray();
    }

    private int[] generateMtable(int k, float p, float a, String id) {
        //check if the index exists
        boolean mTableStoreExists = client
                .admin().indices().prepareExists(ModelStore.STORE_NAME)
                .get().isExists();

        //create the index, if it does not exist
        if(!mTableStoreExists) {
            client.admin().indices()
                    .prepareCreate(ModelStore.STORE_NAME)
                    .get();
        }

        // check if the specified mtable exists
        GetResponse response = client
                .prepareGet(ModelStore.STORE_NAME, ModelStore.ES_TYPE, id)
                .get();

        // create the mtable if we don't have it
        if(!response.isExists()) {
            //create the mtable
            MTableGenerator gen = new MTableGenerator(k, p, a, true);
            List<Integer> mtable = Arrays.stream(gen.getMTable()).boxed().collect(Collectors.toList());

            //index the mtable
            try {
                client.prepareIndex(ModelStore.STORE_NAME, ModelStore.ES_TYPE, id)
                        .setSource(ModelStore.toSource(p, a, k, mtable))
                        .get();
            }
            catch (IOException ioe){
                throw new FairSearchCacheException("Could not create MTable " + ioe.getMessage());
            }

            return gen.getMTable();
        } else {
            return findMTable(response);
        }
    }
}
