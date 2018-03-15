package com.purbon.search.fair;

import com.purbon.search.fair.query.FairQueryBuilder;
import com.purbon.search.fair.query.FairRescoreBuilder;
import org.apache.lucene.search.Query;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;

import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.SettingsFilter;

import org.elasticsearch.env.Environment;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.search.rescore.Rescorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class FairSearchQueryParserPlugin extends Plugin implements ActionPlugin, SearchPlugin {


    private final FairSearchConfig config;

    public FairSearchQueryParserPlugin(final Settings settings) {
        this.config = new FairSearchConfig(new Environment(settings, null), settings);
    }

    @Override
    public List<Setting<?>> getSettings()
    {
        return Arrays.asList(FairSearchConfig.FLOAT_SETTING,
                             FairSearchConfig.SIMPLE_SETTING);
    }

    /**
     * Additional node settings loaded by the plugin. Note that settings that are explicit in the nodes settings can't be
     * overwritten with the additional settings. These settings added if they don't exist.
     */
    @Override
    public Settings additionalSettings() {
        final Settings.Builder builder = Settings.builder();

        builder.put(FairSearchConfig.SIMPLE_SETTING.getKey(), config.getSimple());
        builder.put(FairSearchConfig.FLOAT_SETTING.getKey(), config.getFloat());

        return builder.build();
    }

    /**
     * The next {@link Rescorer}s added by this plugin.
     */
    @Override
    public List<RescorerSpec<?>> getRescorers() {
        return singletonList(
                new RescorerSpec<FairRescoreBuilder>(FairRescoreBuilder.NAME, FairRescoreBuilder::new, FairRescoreBuilder::fromXContent)
        );
    }

    /**
     * The new {@link Query}s defined by this plugin.
     */
    @Override
    public List<QuerySpec<?>> getQueries() {
        return asList(
                new QuerySpec<>(FairQueryBuilder.NAME, FairQueryBuilder::new, FairQueryBuilder::fromXContent)
        );
    }

    /**
     * Rest handlers added by this plugin.
     *
     * @param settings
     * @param restController
     * @param clusterSettings
     * @param indexScopedSettings
     * @param settingsFilter
     * @param indexNameExpressionResolver
     * @param nodesInCluster
     */
    @Override
    public List<RestHandler> getRestHandlers(Settings settings, RestController restController,
                                             ClusterSettings clusterSettings, IndexScopedSettings indexScopedSettings,
                                             SettingsFilter settingsFilter,
                                             IndexNameExpressionResolver indexNameExpressionResolver,
                                             Supplier<DiscoveryNodes> nodesInCluster) {

        List<RestHandler> list = new ArrayList<RestHandler>();
        list.add(new PlainTextRestAction(settings, restController));

        return list;
    }
}