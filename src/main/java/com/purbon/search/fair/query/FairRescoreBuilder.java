package com.purbon.search.fair.query;

import com.purbon.search.fair.lib.FairTopK;
import com.purbon.search.fair.lib.FairTopKImpl;
import com.purbon.search.fair.utils.DocumentPriorityQueue;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.PriorityQueue;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.query.QueryRewriteContext;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.search.rescore.RescoreContext;
import org.elasticsearch.search.rescore.Rescorer;
import org.elasticsearch.search.rescore.RescorerBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.elasticsearch.common.xcontent.ConstructingObjectParser.constructorArg;
import static org.elasticsearch.common.xcontent.ConstructingObjectParser.optionalConstructorArg;

public class FairRescoreBuilder extends RescorerBuilder<FairRescoreBuilder> {

    public static final String NAME = "fair_rescorer";

    private static final ParseField PROTECTED_KEY   = new ParseField("protected_key");
    private static final ParseField PROTECTED_VALUE = new ParseField("protected_value");

    private static final ParseField PROTECTED_ELEMENTS_PROPORTION = new ParseField("min_proportion_protected");
    private static final ParseField SIGNIFICANCE_LEVEL = new ParseField("significance_level");
    private static final ParseField PROPORTION_STRATEGY = new ParseField("proportion_strategy");
    private static final ParseField LOOKUP_FOR_PROPORTION = new ParseField("lookup_for_measuring_proportion");
    private static final ParseField ON_FEW_ELEMENTS_ACTION = new ParseField("on_few_protected_elements");

    private static Logger logger = ESLoggerFactory.getLogger("fair rescorer");

    private static FairSearchConfig config = new FairSearchConfig();

    @Deprecated
    public FairRescoreBuilder() {

    }

    @Deprecated
    public FairRescoreBuilder(String protectedKey, String protectedValue,
                              float protectedElementsProportion, float significance, String proportionStrategy,
                              int lookupForProportion, String onFewElementsAction) {
        this(protectedKey, protectedValue, protectedElementsProportion, significance, proportionStrategy,
               lookupForProportion, onFewElementsAction, null);
    }

    public FairRescoreBuilder(StreamInput in) throws IOException {
        super(in);

        config.setProtectedKey(in.readString());
        config.setProtectedValue(in.readString());
        config.setProtectedElementsProportion(in.readFloat());
        config.setSignificanceLevel(in.readFloat());
        config.setProportionStrategy(in.readOptionalString());
        config.setLookupForProportion(in.readOptionalVInt());
        config.setOnFewElementsAction(in.readOptionalString());
    }

    public FairRescoreBuilder(String protectedKey, String protectedValue,
                              float protectedElementsProportion, float significance, String proportionStrategy,
                              int lookupForProportion, String onFewElementsAction, Settings settings) {

        if (protectedKey == null) {
            throw new IllegalArgumentException("[\" + NAME + \"] requires protected_key");
        }

        if (protectedValue == null) {
            throw new IllegalArgumentException("[\" + NAME + \"] requires protected_value");
        }

        config = new FairSearchConfig(new Environment(settings, null), settings);
        config.setProtectedKey(protectedKey);
        config.setProtectedValue(protectedValue);
        config.setProtectedElementsProportion(protectedElementsProportion);
        config.setSignificanceLevel(significance);
        config.setProportionStrategy(proportionStrategy);
        config.setLookupForProportion(lookupForProportion);
        config.setOnFewElementsAction(onFewElementsAction);
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeString(config.getProtectedKey());
        out.writeString(config.getProtectedValue());
        out.writeFloat(config.getProtectedElementsProportion());
        out.writeFloat(config.getSignificanceLevel());

        if (FairSearchConfig.DEFAULT_PROPORTION_STRATEGY.equals(config.getProportionStrategy())) {
            out.writeOptionalString(null);
        } else {
            out.writeOptionalString(config.getProportionStrategy().toString());
        }

        out.writeOptionalVInt(config.getLookupForProportion());

        if (FairSearchConfig.DEFAULT_ON_FEW_ELEMENTS_ACTION.equals(config.getOnFewElementsAction())) {
            out.writeOptionalString(null);
        } else {
            out.writeOptionalString(config.getOnFewElementsAction().toString());
        }

    }

    @Override
    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field(PROTECTED_KEY.getPreferredName(), config.getProtectedKey());
        builder.field(PROTECTED_VALUE.getPreferredName(), config.getProtectedValue());
        builder.field(PROTECTED_ELEMENTS_PROPORTION.getPreferredName(), config.getProtectedElementsProportion());
        builder.field(SIGNIFICANCE_LEVEL.getPreferredName(), config.getSignificanceLevel());
        builder.field(PROPORTION_STRATEGY.getPreferredName(), config.getProportionStrategy().toString());
        builder.field(LOOKUP_FOR_PROPORTION.getPreferredName(), config.getLookupForProportion());
        builder.field(ON_FEW_ELEMENTS_ACTION.getPreferredName(), config.getOnFewElementsAction().toString());
    }

    private static final ConstructingObjectParser<FairRescoreBuilder, ParserContext> PARSER = new ConstructingObjectParser<>(NAME,
            false,
            (args, context) -> {
                float proportion = -1.0f;
                if (args[2] != null) {
                    proportion = (float)args[2];
                }

                float significance = -1.0f;
                if (args[3] != null) {
                    significance = (float)args[3];
                }

                String proportionStrategy = null;
                if (args.length > 4 && args[4] != null) {
                    proportionStrategy = (String)args[4];
                }

                int lookupForProportion = -1;
                if (args[5] != null) {
                    lookupForProportion = (int)args[5];
                }

                String onFewElementsAction = null;
                if (args.length > 6 && args[6] != null) {
                    onFewElementsAction = (String)args[6];
                }
                return new FairRescoreBuilder((String) args[0], (String) args[1], proportion, significance,
                        proportionStrategy, lookupForProportion, onFewElementsAction, context.getConfig());
            });

    static {
        PARSER.declareString(constructorArg(), PROTECTED_KEY);
        PARSER.declareString(constructorArg(), PROTECTED_VALUE);
        PARSER.declareFloat(optionalConstructorArg(), PROTECTED_ELEMENTS_PROPORTION);
        PARSER.declareFloat(optionalConstructorArg(), SIGNIFICANCE_LEVEL);
        PARSER.declareString(optionalConstructorArg(), PROPORTION_STRATEGY);
        PARSER.declareInt(optionalConstructorArg(), LOOKUP_FOR_PROPORTION);
        PARSER.declareString(optionalConstructorArg(), ON_FEW_ELEMENTS_ACTION);
    }

    public static FairRescoreBuilder fromXContent(XContentParser parser, Settings settings) {
        return PARSER.apply(parser, new ParserContext(settings));
    }

    private static class ParserContext {

        private Settings settings;

        ParserContext(Settings settings) {
            this.settings = settings;
        }

        public Settings getConfig() {
            return settings;
        }
    }

    /**
     * Extensions override this to build the context that they need for rescoring.
     */
    @Override
    protected RescoreContext innerBuildContext(int windowSize, QueryShardContext context) throws IOException {
        return new FairRescoreContext(windowSize, config, context);
    }

    /**
     * Returns the name of the writeable object
     */
    @Override
    public String getWriteableName() {
        return NAME;
    }

    /**
     * Rewrites this instance based on the provided context. The returned
     * objects will be the same instance as this if no changes during the
     * rewrite were applied.
     */
    @Override
    public RescorerBuilder<FairRescoreBuilder> rewrite(QueryRewriteContext ctx) throws IOException {
        return this;
    }


    private class FairRescoreContext extends RescoreContext {

        private final FairSearchConfig config;
        private final FairTopK fairTopK;
        private QueryShardContext context;

        FairRescoreContext(int windowSize, FairSearchConfig config, QueryShardContext context) {
            super(windowSize, FairRescorer.INSTANCE);
            this.context = context;
            this.config = config;

            this.fairTopK = new FairTopKImpl(context.getClient());
        }

        public QueryShardContext getShardContext() {
            return context;
        }

        public FairSearchConfig getConfig() {
            return config;
        }
    }

    private static class FairRescorer implements Rescorer {

        private static final FairRescorer INSTANCE = new FairRescorer();

        /**
         * Modifies the result of the previously executed search ({@link TopDocs})
         * in place based on the given {@link RescoreContext}.
         *
         * @param topDocs        the result of the previously executed search
         * @param searcher       the searcher used for this search. This will never be <code>null</code>.
         * @param rescoreContext the {@link RescoreContext}. This will never be <code>null</code>
         * @throws IOException if an {@link IOException} occurs during rescoring
         */
        @Override
        public TopDocs rescore(TopDocs topDocs, IndexSearcher searcher, RescoreContext rescoreContext) throws IOException {

            FairRescoreContext context = (FairRescoreContext)rescoreContext;
            FairSearchConfig config = context.getConfig();
            FairTopK fairTopK = context.fairTopK;

            // Check if the index where this rescore is happening have the correct setup of shards.
            int numOfShards   = context.getShardContext().getIndexSettings().getNumberOfShards();
            int numOfReplicas = context.getShardContext().getIndexSettings().getNumberOfReplicas();

            if (numOfShards > 1 || numOfReplicas > 1) {
                String message = "Unfortunately this plugin needs your index to have only one shard and one replica";
                throw new ElasticsearchException(message);
            }

            int max = Math.min(topDocs.scoreDocs.length, rescoreContext.getWindowSize());

            List<ScoreDoc> npQueue = new ArrayList<>();
            List<ScoreDoc> pQueue = new ArrayList<>();

            for(int i=0; i < max; i++) {
                ScoreDoc scoreDoc = topDocs.scoreDocs[i];
                Document doc = searcher.doc(scoreDoc.doc);
                if (isProtected(doc, config)) {
                    pQueue.add(scoreDoc);
                } else {
                    npQueue.add(scoreDoc);
                }
            }
            assert npQueue.size() + pQueue.size() == max;

            float significance         = config.getSignificanceLevel();
            float proportion           = config.getProtectedElementsProportion();
            int protectedElementsCount = Math.round(proportion * topDocs.scoreDocs.length);

            if (protectedElementsCount > max) {
                String message = "The protected elements count (k) can not be bigger than";
                       message += "the number of elements to be processed in the rescore phase.";
                throw new ElasticsearchException(message);
            }

            if ( config.hasVariableProportionStrategy() ) {
                if (config.abortOnFewElements() && config.getLookupForProportion() < topDocs.scoreDocs.length) {
                    throw new ElasticsearchException("Lookup proportion below number of docs returned by the query");
                }
                int count = 0;
                for(int i=0; i < config.getLookupForProportion(); i++) {
                   ScoreDoc scoreDoc = topDocs.scoreDocs[i];
                   Document doc = searcher.doc(scoreDoc.doc);
                   if (isProtected(doc, config)) {
                      count+=1;
                   }
                }
                proportion = (float)(count / (config.getLookupForProportion()*1.0));
                protectedElementsCount = count;
            }

            if ( config.abortOnFewElements() && config.hasFixProportionStrategy() &&
                    npQueue.size() < protectedElementsCount) {
                throw new ElasticsearchException("Fair rescorer can not proceed, too few protected elements");
            }

            return fairTopK.fairTopK(npQueue, pQueue, topDocs.scoreDocs.length, proportion, significance);
        }

        private boolean isProtected(Document doc, FairSearchConfig config) {
            try {
                return doc.get(config.getProtectedKey()).equals(config.getProtectedValue());
            } catch (Exception ex) {
                throw new ElasticsearchException(config.getProtectedKey()+" should be an stored value for this plugin to work properly.");
            }
        }

        /**
         * Executes an {@link Explanation} phase on the rescorer.
         *
         * @param topLevelDocId     the global / top-level document ID to explain
         * @param searcher          the searcher used for this search. This will never be <code>null</code>.
         * @param rescoreContext    context for this rescorer
         * @param sourceExplanation explanation of the source of the documents being fed into this rescore
         * @return the explain for the given top level document ID.
         * @throws IOException if an {@link IOException} occurs
         */
        @Override
        public Explanation explain(int topLevelDocId, IndexSearcher searcher,
                                   RescoreContext rescoreContext,
                                   Explanation sourceExplanation) throws IOException {
            //FairRescoreContext context = (FairRescoreContext) rescoreContext;
            return Explanation.match(10.0f, "fair-rescoring", asList(sourceExplanation));
        }

        /**
         * Extracts all terms needed to execute this {@link Rescorer}. This method
         * is executed in a distributed frequency collection roundtrip for
         * {@link SearchType#DFS_QUERY_THEN_FETCH}
         */
        @Override
        public void extractTerms(IndexSearcher searcher, RescoreContext rescoreContext, Set<Term> termsSet) throws IOException {

        }
    }
}
