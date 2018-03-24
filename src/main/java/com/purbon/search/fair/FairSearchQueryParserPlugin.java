package com.purbon.search.fair;

import com.purbon.search.fair.query.FairRescoreBuilder;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.elasticsearch.search.rescore.Rescorer;

import java.util.Arrays;
import java.util.List;

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
}