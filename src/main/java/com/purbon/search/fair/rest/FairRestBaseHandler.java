package com.purbon.search.fair.rest;

import com.purbon.search.fair.ModelStore;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;

public abstract class FairRestBaseHandler extends BaseRestHandler {

    public FairRestBaseHandler(Settings settings) {
        super(settings);
    }
    protected String indexName = ModelStore.STORE_NAME;

}
