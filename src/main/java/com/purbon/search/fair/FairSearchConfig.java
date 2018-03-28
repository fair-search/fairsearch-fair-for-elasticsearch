package com.purbon.search.fair;

import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Setting.Property;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

public class FairSearchConfig {

    public static final String KEY = "fairsearch";

    public static final String MIN_PROPORTION_PROTECTED_KEY    = "min_proportion_protected";

    public static final String SIGNIFICANCE_LEVEL_KEY          = "significance_level";

    public static final String PROPORTION_STRATEGY_KEY         = "proportion_strategy";

    public static final String LOOKUP_MEASURING_PROPORTION_KEY = "lookup_for_measuring_proportion";

    public static final String ON_FEW_PROTECTED_ELEMENTS_KEY   = "on_few_protected_elements";

    static final Setting<String> PROPORTION_STRATEGY_SETTING = new Setting<String>(KEY+"."+PROPORTION_STRATEGY_KEY,
            "fixed",
            (s) -> new String(s),
            (value, settings) -> {
                if (!value.equalsIgnoreCase("fixed") && !value.equalsIgnoreCase("variable")) {
                    throw new IllegalArgumentException("Value [" + value + "] is not a valid setting for proportion_strategy");

                }
            },
            Property.NodeScope,
            Property.Dynamic);

    static final Setting<Float> SIGNIFICANCE_LEVEL_SETTING = Setting.floatSetting(KEY+"."+SIGNIFICANCE_LEVEL_KEY,
            0.1f,
            Property.NodeScope,
            Property.Dynamic);


    static final Setting<String> ON_FEW_PROTECTED_ELEMENTS_SETTING = new Setting<>(KEY + "." + ON_FEW_PROTECTED_ELEMENTS_KEY,
            "proceed",
            (s) -> new String(s),
            (value, settings) -> {
                if (!value.equalsIgnoreCase("abort") && !value.equalsIgnoreCase("proceed")) {
                    throw new IllegalArgumentException("Value [" + value + "] is not a valid setting for on_few_protected_elements");

                }
            },
            Property.NodeScope,
            Property.Dynamic
    );

    /**
     * minimum proportion of elements having the protected attribute.
     */
    static final Setting<Float> MIN_PROPORTION_PROTECTED_SETTING = Setting.floatSetting(KEY+"."+MIN_PROPORTION_PROTECTED_KEY,
            0.5f,
            Property.NodeScope,
            Property.Dynamic);

    /**
     * The number of top elements that are examined to determine the target proportion of protected elements.
     */
    static final Setting<Float> LOOKUP_MEASURING_PROPORTION_SETTING = Setting.floatSetting(KEY+"."+LOOKUP_MEASURING_PROPORTION_KEY,
            100f,
            Property.NodeScope,
            Property.Dynamic);


    private String proportionStrategy;
    private String onFewProtectedElements;
    private Float significanceLevel;
    private Float proportionProtected;
    private Float lookupMeasuringProportion;

    FairSearchConfig(final Environment env, final Settings settings) {

        this.proportionStrategy = PROPORTION_STRATEGY_SETTING.get(settings);
        this.significanceLevel = SIGNIFICANCE_LEVEL_SETTING.get(settings);
        this.onFewProtectedElements = ON_FEW_PROTECTED_ELEMENTS_SETTING.get(settings);
        this.proportionProtected = MIN_PROPORTION_PROTECTED_SETTING.get(settings);
        this.lookupMeasuringProportion = LOOKUP_MEASURING_PROPORTION_SETTING.get(settings);
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

    public Float getLookupMeasuringProportion() {
        return lookupMeasuringProportion;
    }
}
