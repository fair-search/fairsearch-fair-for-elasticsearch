package com.purbon.search.fair;

import com.purbon.search.fair.query.FairQueryBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESIntegTestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertHitCount;

public class FairQueryIT extends ESIntegTestCase {

    private static final String INDEX = "test";

    /**
     * Returns a collection of plugins that should be loaded on each node.
     */
    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Arrays.asList(FairSearchQueryParserPlugin.class);
    }

    @Override
    public Settings nodeSettings(int nodeOrdinal) {
        return Settings.builder().put(super.nodeSettings(nodeOrdinal))
                .put("thread_pool.search.size", 1)
                .put("thread_pool.search.queue_size", 1)
                .build();
    }

    public void testSimpleMatch() throws ExecutionException, InterruptedException {

        indexRandom(true, false, docs());
        SearchResponse response = client().prepareSearch(INDEX).setQuery(
               new FairQueryBuilder("text", "Berlin")
        ).get();

        assertHitCount(response, 1L);

        assertEquals("Berlin Wall",
                      response.getHits().getAt(0).getSourceAsMap().get("text"));
    }

    /**
     * For subclasses to override. Overrides must call {@code super.tearDown()}.
     */
    private XContentBuilder createMapping() throws IOException {
        return XContentFactory.jsonBuilder()
                .startObject()
                    .startObject(INDEX)
                        .startObject("properties")
                        .startObject("field")
                            .field("text", "text")
                        .endObject()
                        .endObject()
                    .endObject()
                .endObject();
    }


    private List<IndexRequestBuilder> docs() {
        List<IndexRequestBuilder> list= new ArrayList<>();
        list.add(prepareDoc("test", "1","text", "Berlin Wall"));
        list.add(prepareDoc("test", "2","text", "Checkpoint Charlie"));
        return list;
    }

    private IndexRequestBuilder prepareDoc(String indexName, String id, Object ... source) {
        return client().prepareIndex(indexName, indexName).setSource(source).setId(id);
    }

}
