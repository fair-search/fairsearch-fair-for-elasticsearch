package com.purbon.search.fair.query;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

public class FairSearchConfig {


    private static Logger logger = ESLoggerFactory.getLogger(FairSearchConfig.class);

    public enum ProportionStrategy {
        fixed ("fixed"),
        variable ("variable");

        private final String name;

        ProportionStrategy(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    public enum OnFewElementsAction {
        proceed ("proceed"),
        abort ("abort");

        private final String name;

        OnFewElementsAction(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }


    public static ProportionStrategy DEFAULT_PROPORTION_STRATEGY = ProportionStrategy.fixed;
    public static Float   DEFAULT_SIGNIFICANCE_LEVEL = 0.1f;
    public static OnFewElementsAction DEFAULT_ON_FEW_ELEMENTS_ACTION = OnFewElementsAction.proceed;
    public static Float   DEFAULT_MIN_PROPORTION_PROTECTED = 0.5f;
    public static Integer DEFAULT_LOOKUP_FOR_PROPORTION = 100;

    private final Environment env;
    private final Settings settings;

    private String protectedKey;
    private String protectedValue;

    private float protectedElementsProportion;
    private float significanceLevel;
    private int lookupForProportion;
    private OnFewElementsAction onFewElementsAction;
    private ProportionStrategy proportionStrategy;

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
            if (getProportionStrategy().equals(ProportionStrategy.variable)) {
                String msg = "using min_proportion_protected is not permitted if the proportion strategy is variable";
                logger.error(msg);
                throw new ElasticsearchException(msg);
            }
            this.protectedElementsProportion = protectedElementsProportion;
        }
    }

    public float getSignificanceLevel() {
        return significanceLevel;
    }

    public void setSignificanceLevel(float significanceLevel) {
        if (significanceLevel < 0) {
            this.significanceLevel = DEFAULT_SIGNIFICANCE_LEVEL;
        } else {
            this.significanceLevel = significanceLevel;
        }
    }

    public int getLookupForProportion() {
        return lookupForProportion;
    }

    public void setLookupForProportion(int lookupForProportion) {
        if (lookupForProportion < 0) {
            this.lookupForProportion = DEFAULT_LOOKUP_FOR_PROPORTION;
        } else {
            if (getProportionStrategy().equals(ProportionStrategy.variable)) {
                String msg = "using lookup_for_measuring_proportion is not permitted if the proportion strategy is variable";
                logger.error(msg);
                throw new ElasticsearchException(msg);
            }
            this.lookupForProportion = lookupForProportion;
        }
    }

    public OnFewElementsAction getOnFewElementsAction() {
        return onFewElementsAction;
    }

    public void setOnFewElementsAction(String onFewElementsAction) {
        if (onFewElementsAction == null) {
            setOnFewElementsAction(DEFAULT_ON_FEW_ELEMENTS_ACTION);
        } else {
            try {
                setOnFewElementsAction(OnFewElementsAction.valueOf(onFewElementsAction));
            } catch (IllegalArgumentException ex){
                StringBuilder msgBuilder = new StringBuilder().
                        append("Value [").
                        append(onFewElementsAction).
                        append("] is not a valid setting for on_few_protected_elements");
                throw new ElasticsearchException(msgBuilder.toString());
            }
        }
    }

    public void setOnFewElementsAction(OnFewElementsAction onFewElementsAction) {
        if (onFewElementsAction == null) {
            this.onFewElementsAction = DEFAULT_ON_FEW_ELEMENTS_ACTION;
        } else {
            this.onFewElementsAction = onFewElementsAction;
        }
    }

    public ProportionStrategy getProportionStrategy() {
        return proportionStrategy;
    }

    public void setProportionStrategy(String proportionStrategy) {
        if (proportionStrategy == null) {
            setProportionStrategy(DEFAULT_PROPORTION_STRATEGY);
        } else {
            try {
                setProportionStrategy(ProportionStrategy.valueOf(proportionStrategy));
            } catch (IllegalArgumentException ex){
                StringBuilder msgBuilder = new StringBuilder().
                        append("Value [").
                        append(proportionStrategy).
                        append("] is not a valid setting for proportion_strategy");
                throw new ElasticsearchException(msgBuilder.toString());
            }
        }
    }

    public void setProportionStrategy(ProportionStrategy proportionStrategy) {
        if (proportionStrategy == null) {
            this.proportionStrategy = DEFAULT_PROPORTION_STRATEGY;
        } else {
            this.proportionStrategy = proportionStrategy;
        }
    }

    public boolean hasVariableProportionStrategy() {
        return getProportionStrategy().equals(ProportionStrategy.variable);
    }

    public boolean hasFixProportionStrategy() {
        return getProportionStrategy().equals(ProportionStrategy.fixed);
    }

    public boolean abortOnFewElements() {
        return getOnFewElementsAction().equals(OnFewElementsAction.abort);
    }
}
