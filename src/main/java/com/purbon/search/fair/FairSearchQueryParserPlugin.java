package com.purbon.search.fair;

import com.purbon.search.fair.action.ListMTablesAction;
import com.purbon.search.fair.action.MTableStoreAction;
import com.purbon.search.fair.action.TransportListMTablesAction;
import com.purbon.search.fair.action.TransportMTableStoreAction;
import com.purbon.search.fair.query.FairRescoreBuilder;
import com.purbon.search.fair.rest.RestAddMTableToSet;
import com.purbon.search.fair.rest.RestMTableStore;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.search.rescore.Rescorer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

public class FairSearchQueryParserPlugin extends Plugin implements ActionPlugin, SearchPlugin {

    private final Settings settings;

    public FairSearchQueryParserPlugin(final Settings settings) {
        this.settings = settings;
    }

    /**
     * The next {@link Rescorer}s added by this plugin.
     */
    @Override
    public List<RescorerSpec<?>> getRescorers() {

        RescorerSpec<FairRescoreBuilder> rescorer = new RescorerSpec<>(FairRescoreBuilder.NAME,
                FairRescoreBuilder::new,
                xContentParser -> FairRescoreBuilder.fromXContent(xContentParser, settings));

        return singletonList(rescorer);
    }

    @Override
    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        return unmodifiableList(asList(
                new ActionHandler<>(ListMTablesAction.INSTANCE, TransportListMTablesAction.class),
                new ActionHandler<>(MTableStoreAction.INSTANCE, TransportMTableStoreAction.class)));
    }

    @Override
    public List<RestHandler> getRestHandlers(Settings settings, RestController restController,
                                             ClusterSettings clusterSettings, IndexScopedSettings indexScopedSettings,
                                             SettingsFilter settingsFilter, IndexNameExpressionResolver indexNameExpressionResolver,
                                             Supplier<DiscoveryNodes> nodesInCluster) {
        List<RestHandler> list = new ArrayList<>();
        list.add(new RestAddMTableToSet(settings, restController, ModelStore.ES_TYPE));
        list.add(new RestMTableStore(settings, restController, ModelStore.ES_TYPE));
        return unmodifiableList(list);
    }
}