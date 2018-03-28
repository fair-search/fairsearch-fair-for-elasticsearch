package com.purbon.search.fair;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsResponse;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.Before;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

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

    public void testWrongOnFewProtectedElementsSettings() {

       ClusterAdminClient adminClient = client().admin().cluster();

       Settings settings = Settings.builder()
               .put("fairsearch.on_few_protected_elements", "break")
               .build();

       ClusterUpdateSettingsRequest request = new ClusterUpdateSettingsRequest().
               persistentSettings(settings);

        try {
            adminClient.updateSettings(request);
            assertTrue(false);
        } catch (AssertionError ex) {
            assertTrue(true);
        }
    }

}
