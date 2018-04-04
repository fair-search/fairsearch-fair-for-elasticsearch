package com.purbon.search.fair;

import com.purbon.search.fair.query.FairRescoreBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.elasticsearch.search.rescore.Rescorer;

import java.util.List;

import static java.util.Collections.singletonList;

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
}