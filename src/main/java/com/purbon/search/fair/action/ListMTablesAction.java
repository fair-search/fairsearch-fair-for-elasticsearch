package com.purbon.search.fair.action;


import com.purbon.search.fair.ModelStore;
import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.support.master.MasterNodeReadRequest;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListMTablesAction extends Action<ListMTablesAction.ListMTablesActionRequest,
        ListMTablesAction.ListMTablesActionResponse,ListMTablesAction.ListMTablesActionRequestBuilder> {
    public static final String NAME = "fs:mtables/list";
    public static final ListMTablesAction INSTANCE = new ListMTablesAction();

    private ListMTablesAction() {
        super(NAME);
    }

    @Override
    public ListMTablesActionResponse newResponse() {
        return new ListMTablesActionResponse();
    }

    @Override
    public ListMTablesActionRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ListMTablesActionRequestBuilder(client);
    }

    public static class ListMTablesActionRequestBuilder extends ActionRequestBuilder<ListMTablesActionRequest,
            ListMTablesActionResponse, ListMTablesActionRequestBuilder> {
        protected ListMTablesActionRequestBuilder(ElasticsearchClient client) {
            super(client, INSTANCE, new ListMTablesActionRequest());
        }
    }

    public static class ListMTablesActionRequest extends MasterNodeReadRequest<ListMTablesActionRequest> {
        @Override
        public ActionRequestValidationException validate() {
            return null;
        }
    }

    public static class ListMTablesActionResponse extends ActionResponse implements ToXContentObject {
        private Map<String, MTableInfo> tables;

        ListMTablesActionResponse() {}

        public ListMTablesActionResponse(List<MTableInfo> info) {
            tables = info.stream().collect(Collectors.toMap((i) -> i.storeName, (i) -> i));
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            return builder.startObject()
                    .field("tables", tables)
                    .endObject();
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            super.readFrom(in);
            tables = in.readMap(StreamInput::readString, MTableInfo::new);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeMap(tables, StreamOutput::writeString, (w, i) -> i.writeTo(w));
        }

        public Map<String, MTableInfo> getTables() {
            return tables;
        }
    }

    public static class MTableInfo implements Writeable, ToXContent {
        private String storeName;
        private String indexName;
        private int version;
        private Map<String, Integer> counts;

        public MTableInfo(String indexName, int version, Map<String, Integer> counts) {
            this.indexName = Objects.requireNonNull(indexName);
            this.storeName = ModelStore.STORE_NAME;
            this.version = version;
            this.counts = counts;
        }
        public MTableInfo(StreamInput in) throws IOException {
            storeName = in.readString();
            indexName = in.readString();
            version = in.readVInt();
            counts = in.readMap(StreamInput::readString, StreamInput::readVInt);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeString(storeName);
            out.writeString(indexName);
            out.writeVInt(version);
            out.writeMap(counts, StreamOutput::writeString, StreamOutput::writeVInt);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            return builder.startObject()
                    .field("store", storeName)
                    .field("index", indexName)
                    .field("version", version)
                    .field("counts", counts)
                    .endObject();
        }

        public String getStoreName() {
            return storeName;
        }

        public String getIndexName() {
            return indexName;
        }

        public int getVersion() {
            return version;
        }

        public Map<String, Integer> getCounts() {
            return counts;
        }
    }
}
