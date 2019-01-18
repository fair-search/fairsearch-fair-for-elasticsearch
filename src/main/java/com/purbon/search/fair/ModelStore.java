package com.purbon.search.fair;

import com.purbon.search.fair.action.MTableStoreAction;
import com.purbon.search.fair.action.MTableStoreAction.MTableStoreRequest;
import com.purbon.search.fair.utils.MTableGenerator;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.List;

public class ModelStore {

    public static String STORE_NAME = ".fs_store";

    public static final String ES_TYPE = "store";

    public static boolean isStore(String indexName) {
        return STORE_NAME.equals(indexName);
    }

    public static XContentBuilder toSource(MTableStoreRequest request) throws IOException {

        XContentBuilder source = XContentFactory.contentBuilder(Requests.INDEX_CONTENT_TYPE);
        source.startObject();
            source.field("type", "mtable");
            source.field("proportion", request.getProportion());
            source.field("alpha", request.getAlpha());
            source.field("k", request.getK());
            source.field("mtable", request.getMtable());
        source.endObject();
        return source;
    }

    public static XContentBuilder toSource(double proportion, double alpha, int k, List<Integer> mtable) throws IOException {

        XContentBuilder source = XContentFactory.contentBuilder(Requests.INDEX_CONTENT_TYPE);
        source.startObject();
            source.field("type", "mtable");
            source.field("proportion", proportion);
            source.field("alpha", alpha);
            source.field("k", k);
            source.field("mtable", mtable);
        source.endObject();
        return source;
    }

    public static String generateId(float proportion, float alpha, int k) {
        return new StringBuilder()
                .append("mtable(")
                .append(proportion).append(",")
                .append(alpha).append(",")
                .append(k)
                .append(")").toString();
    }
}
