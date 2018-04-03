package com.purbon.search.fair;

import com.purbon.search.fair.query.FairRescoreBuilder;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsResponse;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.Before;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

@ESIntegTestCase.ClusterScope(scope = ESIntegTestCase.Scope.TEST)
public class FairRescoreTests extends ESIntegTestCase {

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

    public void testWrongOnFewProtectedElementsSettings() throws ExecutionException, InterruptedException {

       ClusterAdminClient adminClient = client().admin().cluster();

       Settings settings = Settings.builder()
               .put("fairsearch.on_few_protected_elements", "abort")
               .build();

       ClusterUpdateSettingsRequest request = new ClusterUpdateSettingsRequest().
               transientSettings(settings);

        try {

            IndexRequestBuilder[] builders = new IndexRequestBuilder[] {
                    client().prepareIndex("test", "test").setSource("{\"test_field\" : \"foobar\"}", XContentType.JSON),
                    client().prepareIndex("test", "test").setSource(new BytesArray("{\"test_field\" : \"foobar\"}"), XContentType.JSON),
                    client().prepareIndex("test", "test").setSource(new BytesArray("{\"test_field\" : \"foobar\"}"), XContentType.JSON),
            };
            indexRandom(true, builders);

            ActionFuture<ClusterUpdateSettingsResponse> response = adminClient.updateSettings(request);

            SearchRequestBuilder builder = client().prepareSearch("test");
            builder.setQuery( new MatchAllQueryBuilder().queryName("foo"))
                    .addRescorer(new FairRescoreBuilder("gender", "female", 0.99f, request.transientSettings()));

            builder.execute().actionGet();

            assertTrue(false);
        } catch (AssertionError ex) {
            assertTrue(true);
        }
    }

}
