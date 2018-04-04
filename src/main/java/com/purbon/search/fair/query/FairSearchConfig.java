package com.purbon.search.fair.query;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

public class FairSearchConfig {

    public static String  DEFAULT_PROPORTION_STRATEGY = "fixed";
    public static Float   DEFAULT_SIGNIFICANCE_LEVEL = 0.1f;
    public static String  DEFAULT_ON_FEW_ELEMENTS_ACTION = "proceed";
    public static Float   DEFAULT_MIN_PROPORTION_PROTECTED = 0.5f;
    public static Integer DEFAULT_LOOKUP_FOR_PROPORTION = 100;

    private final Environment env;
    private final Settings settings;

    private String protectedKey;
    private String protectedValue;

    private float protectedElementsProportion;
    private float significanceLevel;
    private int lookupForProportion;
    private String onFewElementsAction;
    private String proportionStrategy;

    FairSearchConfig(final Environment env, final Settings settings) {
        this.env = env;
        this.settings = settings;

        this.protectedElementsProportion = DEFAULT_MIN_PROPORTION_PROTECTED;
        this.significanceLevel = DEFAULT_SIGNIFICANCE_LEVEL;
        this.lookupForProportion = DEFAULT_LOOKUP_FOR_PROPORTION;
        this.onFewElementsAction = DEFAULT_ON_FEW_ELEMENTS_ACTION;
        this.proportionStrategy = DEFAULT_PROPORTION_STRATEGY;
    }

    public FairSearchConfig() {
      this(null, Settings.EMPTY);
    }

    public String getProtectedKey() {
        return protectedKey;
    }

    public void setProtectedKey(String protectedKey) {
        this.protectedKey = protectedKey;
    }

    public String getProtectedValue() {
        return protectedValue;
    }

    public void setProtectedValue(String protectedValue) {
        this.protectedValue = protectedValue;
    }

    public float getProtectedElementsProportion() {
        return protectedElementsProportion;
    }

    public void setProtectedElementsProportion(float protectedElementsProportion) {
        if (protectedElementsProportion < 0) {
            this.protectedElementsProportion = DEFAULT_MIN_PROPORTION_PROTECTED;
        } else {
            this.protectedElementsProportion = protectedElementsProportion;
        }
    }

    public float getSignificanceLevel() {
        return significanceLevel;
    }

    public void setSignificanceLevel(float significanceLevel) {
        this.significanceLevel = significanceLevel;
    }

    public int getLookupForProportion() {
        return lookupForProportion;
    }

    public void setLookupForProportion(int lookupForProportion) {
        this.lookupForProportion = lookupForProportion;
    }

    public String getOnFewElementsAction() {
        return onFewElementsAction;
    }

    public void setOnFewElementsAction(String onFewElementsAction) {
        if (onFewElementsAction == null) {
            this.onFewElementsAction = DEFAULT_ON_FEW_ELEMENTS_ACTION;
        } else {
            this.onFewElementsAction = onFewElementsAction;
        }
    }

    public String getProportionStrategy() {
        return proportionStrategy;
    }

    public void setProportionStrategy(String proportionStrategy) {
        this.proportionStrategy = proportionStrategy;
    }

    public boolean hasVariableProportionStrategy() {
       return getProportionStrategy().equalsIgnoreCase("variable");
    }

    public boolean hasFixProportionStrategy() {
        return getProportionStrategy().equalsIgnoreCase("fixed");
    }

    public boolean abortOnFewElements() {
        return getOnFewElementsAction().equalsIgnoreCase("abort");
    }
}
