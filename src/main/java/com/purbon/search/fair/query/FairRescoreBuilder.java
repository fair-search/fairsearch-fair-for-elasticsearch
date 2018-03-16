package com.purbon.search.fair.query;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
import org.apache.lucene.search.*;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.index.query.QueryRewriteContext;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.rescore.RescoreContext;
import org.elasticsearch.search.rescore.Rescorer;
import org.elasticsearch.search.rescore.RescorerBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.elasticsearch.common.xcontent.ConstructingObjectParser.constructorArg;

public class FairRescoreBuilder extends RescorerBuilder<FairRescoreBuilder> {

    public static final String NAME = "fair_rescorer";

    private static final ParseField PROTECTED_KEY   = new ParseField("protected_key");
    private static final ParseField PROTECTED_VALUE = new ParseField("protected_value");

    private float factor;
    private String protectedKey;
    private String protectedValue;

    public FairRescoreBuilder() {

    }

    public FairRescoreBuilder(String protectedKey, String protectedValue) {
        this.factor = 0.0f;

        if (protectedKey == null) {
            throw new IllegalArgumentException("[\" + NAME + \"] requires protected_key");
        }

        if (protectedValue == null) {
            throw new IllegalArgumentException("[\" + NAME + \"] requires protected_value");
        }

        this.protectedKey = protectedKey;
        this.protectedValue = protectedValue;
    }

    public FairRescoreBuilder(StreamInput in) throws IOException {
        super(in);
        this.protectedKey = in.readString();
        this.protectedValue = in.readString();
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeString(protectedKey);
        out.writeString(protectedValue);
    }

    @Override
    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field(PROTECTED_KEY.getPreferredName(), protectedKey);
        builder.field(PROTECTED_VALUE.getPreferredName(), protectedValue);
    }

    private static final ConstructingObjectParser<FairRescoreBuilder, Void> PARSER = new ConstructingObjectParser<>(NAME,
            args -> new FairRescoreBuilder((String)args[0], (String)args[1]));

    static {
        PARSER.declareString(constructorArg(), PROTECTED_KEY);
        PARSER.declareString(constructorArg(), PROTECTED_VALUE);
    }

    public static FairRescoreBuilder fromXContent(XContentParser parser) throws IOException {
        return PARSER.apply(parser, null);
    }

    /**
     * Extensions override this to build the context that they need for rescoring.
     */
    @Override
    protected RescoreContext innerBuildContext(int windowSize, QueryShardContext context) throws IOException {
        return new FairRescoreContext(windowSize, protectedKey, protectedValue, context);
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

        private String protectedKey;
        private String protectedValue;

        FairRescoreContext(int windowSize, String protectedKey, String protectedValue, QueryShardContext context) {
            super(windowSize, FairRescorer.INSTANCE);
            this.protectedKey = protectedKey;
            this.protectedValue = protectedValue;
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
            int max = Math.min(topDocs.scoreDocs.length, rescoreContext.getWindowSize());

            for(int i=0; i < max; i++) {

                ScoreDoc scoredoc = topDocs.scoreDocs[i];
                Document doc = searcher.doc(scoredoc.doc);
                assert doc != null;

                if (doc.get("gender").equals(context.protectedValue)) {
                    topDocs.scoreDocs[i].score = 1;
                } else {
                    topDocs.scoreDocs[i].score = 0;
                }
            }

            // Sort by score descending, then docID ascending, just like lucene's QueryRescorer
            Arrays.sort(topDocs.scoreDocs, (a, b) -> {
                if (a.score > b.score) {
                    return -1;
                }
                if (a.score < b.score) {
                    return 1;
                }
                // Safe because doc ids >= 0
                return a.doc - b.doc;
            });
            return topDocs;
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
            FairRescoreContext context = (FairRescoreContext) rescoreContext;
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
