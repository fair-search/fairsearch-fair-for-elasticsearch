package com.purbon.search.fair;

import com.purbon.search.fair.action.MTableStoreAction;
import com.purbon.search.fair.action.MTableStoreAction.MTableStoreRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;

public class ModelStore {

    public static String STORE_NAME = ".fs_store";

    public static final String ES_TYPE = "store";

    public static boolean isStore(String indexName) {
        return STORE_NAME.equals(indexName);
    }

    public static XContentBuilder toSource(MTableStoreRequest request) throws IOException {

        XContentBuilder source = XContentFactory.contentBuilder(Requests.INDEX_CONTENT_TYPE);
        source.startObject();
            source.field("name", request.getName());
            source.field("type", "mtable");
            source.field("proportion", request.getProportion());
            source.field("alpha", request.getAlpha());
            source.field("k", request.getK());
            source.field("mtable", request.getMtable());
        source.endObject();
        return source;
    }
}
