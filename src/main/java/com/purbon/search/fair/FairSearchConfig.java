package com.purbon.search.fair;

import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Setting.Property;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

public class FairSearchConfig {


    public static final String MIN_PROPORTION_PROTECTED_KEY = "fairsearch.min_proportion_protected";

    public static final String SIGNIFICANCE_LEVEL_KEY = "fairsearch.significance_level";

    public static final String PROPORTION_STRATEGY_KEY = "fairsearch.proportion_strategy";


    static final Setting<String> PROPORTION_STRATEGY_SETTING = Setting.simpleString(PROPORTION_STRATEGY_KEY,
            (value, settings) -> {
                if (value.equalsIgnoreCase("fixed") || value.equalsIgnoreCase("variable")) {
                    throw new IllegalArgumentException("Value [" + value + "] is not a valid setting for proportion_strategy");

                }
            },
            Property.NodeScope,
            Property.Dynamic);

    static final Setting<Float> SIGNIFICANCE_LEVEL_SETTING = Setting.floatSetting(SIGNIFICANCE_LEVEL_KEY,
            0.1f,
            Property.NodeScope,
            Property.Dynamic);

    static final Setting<String> ON_FEW_PROTECTED_ELELEMENTS_SETTING = Setting.simpleString("fairsearch.on_few_protected_elements",
            (value, settings) -> {
                if (value.equalsIgnoreCase("abort") || value.equalsIgnoreCase("proceed")) {
                    throw new IllegalArgumentException("Value [" + value + "] is not a valid setting for on_few_protected_elements");

                }
            },
            Property.NodeScope,
            Property.Dynamic);

    static final Setting<Float> MIN_PROPORTION_PROTECTED_SETTING = Setting.floatSetting(MIN_PROPORTION_PROTECTED_KEY,
            0.5f,
            Property.NodeScope,
            Property.Dynamic);

    private String proportionStrategy;
    private String onFewProtectedElements;
    private Float significanceLevel;
    private Float proportionProtected;

    FairSearchConfig(final Environment env, final Settings settings) {

        this.proportionStrategy = PROPORTION_STRATEGY_SETTING.get(settings);
        this.significanceLevel = SIGNIFICANCE_LEVEL_SETTING.get(settings);
        this.onFewProtectedElements = ON_FEW_PROTECTED_ELELEMENTS_SETTING.get(settings);
        this.proportionProtected = MIN_PROPORTION_PROTECTED_SETTING.get(settings);
    }

    public Float getProportionProtected() {
        return proportionProtected;
    }

    public String getOnFewProtectedElements() {
        return onFewProtectedElements;
    }

    public String getProportionStrategy() {
        return proportionStrategy;
    }

    public Float getSignificanceLevel() {
        return significanceLevel;
    }
}
