package com.purbon.search.fair.action;

import org.elasticsearch.client.ElasticsearchClient;

public class IndicesAction {

    public RequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new RequestBuilder(client);
    }

    public static class RequestBuilder {

        private ElasticsearchClient client;

        RequestBuilder(ElasticsearchClient client) {
            this.client = client;
        }

        public void test() {
        }
    }
}
