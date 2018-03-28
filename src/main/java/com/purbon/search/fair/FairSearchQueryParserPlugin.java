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
    private final Settings settings;

    public FairSearchQueryParserPlugin(final Settings settings) {
        this.config   = new FairSearchConfig(new Environment(settings, null), settings);
        this.settings = settings;
    }

    @Override
    public List<Setting<?>> getSettings()
    {
        return Arrays.asList(FairSearchConfig.SIGNIFICANCE_LEVEL_SETTING,
                FairSearchConfig.PROPORTION_STRATEGY_SETTING,
                FairSearchConfig.ON_FEW_PROTECTED_ELEMENTS_SETTING,
                FairSearchConfig.MIN_PROPORTION_PROTECTED_SETTING,
                FairSearchConfig.LOOKUP_MEASURING_PROPORTION_SETTING);
    }

    /**
     * Additional node settings loaded by the plugin. Note that settings that are explicit in the nodes settings can't be
     * overwritten with the additional settings. These settings added if they don't exist.
     */
    @Override
    public Settings additionalSettings() {
        final Settings.Builder builder = Settings.builder();

        builder.put(FairSearchConfig.PROPORTION_STRATEGY_SETTING.getKey(), config.getProportionStrategy());
        builder.put(FairSearchConfig.SIGNIFICANCE_LEVEL_SETTING.getKey(), config.getSignificanceLevel());
        builder.put(FairSearchConfig.ON_FEW_PROTECTED_ELEMENTS_SETTING.getKey(), config.getOnFewProtectedElements());
        builder.put(FairSearchConfig.MIN_PROPORTION_PROTECTED_SETTING.getKey(), config.getProportionProtected());
        builder.put(FairSearchConfig.LOOKUP_MEASURING_PROPORTION_SETTING.getKey(), config.getProportionProtected());

        return builder.build();
    }

    /**
     * The next {@link Rescorer}s added by this plugin.
     */
    @Override
    public List<RescorerSpec<?>> getRescorers() {

        RescorerSpec<FairRescoreBuilder> rescorer = new RescorerSpec<FairRescoreBuilder>(FairRescoreBuilder.NAME,
                FairRescoreBuilder::new,
                xContentParser -> FairRescoreBuilder.fromXContent(xContentParser, settings));

        return singletonList(rescorer);
    }
}