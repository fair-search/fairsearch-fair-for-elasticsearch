package com.purbon.search.fair.action;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.StatusToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.elasticsearch.action.ValidateActions.addValidationError;

public class MTableStoreAction extends Action<MTableStoreAction.MTableStoreRequest,
        MTableStoreAction.MTableStoreResponse,
        MTableStoreAction.MTableStoreRequestBuilder> {

    public static final MTableStoreAction INSTANCE = new MTableStoreAction();
    public static final String NAME = "cluster:admin/fsp/store/mtable-store";

    protected MTableStoreAction() {
        super(NAME);
    }

    public MTableStoreRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new MTableStoreRequestBuilder(client, this);
    }

    /**
     * Creates a new response instance.
     */
    @Override
    public MTableStoreResponse newResponse() {
        return new MTableStoreResponse();
    }

    public static class MTableStoreRequestBuilder extends ActionRequestBuilder<MTableStoreRequest,
            MTableStoreResponse,
            MTableStoreRequestBuilder> {

        private ElasticsearchClient client;

        MTableStoreRequestBuilder(ElasticsearchClient client) {
            this(client, INSTANCE);
        }

        MTableStoreRequestBuilder(ElasticsearchClient client, MTableStoreAction action) {
            super(client, action, new MTableStoreRequest());
            this.client = client;
        }

    }

    public static class MTableStoreRequest extends ActionRequest {

        private String store;
        private Action action;
        private Long updatedVersion;
        private String routing;

        private String name;
        private float proportion;
        private float alpha;
        private int k;
        private List<Integer> mtable;
        private int size;

        public String getId() {
            return new StringBuilder()
                    .append("name(")
                        .append(proportion).append(",")
                        .append(alpha).append(",")
                        .append(k)
                    .append(")").toString();
        }

        public enum Action {
            CREATE,
            UPDATE
        }

        public MTableStoreRequest() {}

        public MTableStoreRequest(String store, float proportion, float alpha, int k, int size, List<Integer> mtable, Action action) {
            this.store = Objects.requireNonNull(store);
            this.action = Objects.requireNonNull(action);

            this.proportion = proportion;
            this.alpha = alpha;
            this.k = k;
            this.size = size;
            this.mtable = mtable;
        }

        @Override
        public ActionRequestValidationException validate() {

            ActionRequestValidationException ex = null;
            if (name == null) {
                ex = addValidationError("Name is a required parameter", ex);
            } else if (proportion == 0) {
                ex = addValidationError("Proportion is a required parameter", ex);
            } if (alpha == 0) {
                ex = addValidationError("Significance is a required parameter", ex);
            }
            return ex;
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            super.readFrom(in);
            store = in.readString();
            routing = in.readOptionalString();
            action = Action.values()[in.readVInt()];

            name = in.readString();
            proportion = in.readFloat();
            alpha = in.readFloat();
            k = in.readInt();
            size = in.readInt();
            mtable = Arrays.stream(in.readIntArray()).boxed().collect(Collectors.toList());
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeString(store);
            out.writeOptionalString(routing);
            out.writeVInt(action.ordinal());

            out.writeString(name);
            out.writeFloat(proportion);
            out.writeFloat(alpha);
            out.writeInt(k);
            out.writeInt(size);

            if (mtable != null) {
                out.writeIntArray(mtable.stream().mapToInt(i->i).toArray());
            }
        }

        public String getStore() {
            return store;
        }

        public void setStore(String store) {
            this.store = store;
        }

        public Action getAction() {
            return action;
        }

        public void setAction(Action action) {
            this.action = action;
        }

        public Long getUpdatedVersion() {
            return updatedVersion;
        }

        public void setUpdatedVersion(Long updatedVersion) {
            this.updatedVersion = updatedVersion;
        }

        public String getRouting() {
            return routing;
        }

        public void setRouting(String routing) {
            this.routing = routing;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public float getProportion() {
            return proportion;
        }

        public void setProportion(float proportion) {
            this.proportion = proportion;
        }

        public float getAlpha() {
            return alpha;
        }

        public void setAlpha(float alpha) {
            this.alpha = alpha;
        }

        public List<Integer> getMtable() {
            return mtable;
        }

        public void setMtable(List<Integer> mtable) {
            this.mtable = mtable;
        }

        public int getK() {
            return k;
        }

        public void setK(int k) {
            this.k = k;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }

    public static class MTableStoreResponse extends ActionResponse implements StatusToXContentObject {

        private IndexResponse response;

        public MTableStoreResponse() {

        }

        public MTableStoreResponse(IndexResponse response) {
            this.response = response;
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            super.readFrom(in);
            response = new IndexResponse();
            response.readFrom(in);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            response.writeTo(out);
        }

        /**
         * Returns the REST status to make sure it is returned correctly
         */
        @Override
        public RestStatus status() {
            return response.status();
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            return response.toXContent(builder, params);
        }

        public IndexResponse getResponse() {
            return response;
        }
    }
}
