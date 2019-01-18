package com.purbon.search.fair.lib.fairness;

import com.purbon.search.fair.lib.FairnessTableLookup;
import com.purbon.search.fair.lib.NotImplementedException;
import org.elasticsearch.client.Client;

public class InternalFairnessTableLookup implements FairnessTableLookup {

    private FairnessCache cache;

    public InternalFairnessTableLookup(Client client) {
        this.cache = new FairnessCache(client);
    }

    public int fairness(int k, float proportion, float significance) {
        throw new NotImplementedException();
    }

    @Override
    public int[] fairnessAsTable(int k, float p, float a) {
        return cache.get(k, p, a);
    }

}
