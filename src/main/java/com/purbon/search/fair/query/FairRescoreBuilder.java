package com.purbon.search.fair.query;

import com.purbon.search.fair.lib.FairTopKImpl;
import com.purbon.search.fair.utils.DocumentPriorityQueue;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.PriorityQueue;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ConstructingObjectParser;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.QueryRewriteContext;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.search.rescore.RescoreContext;
import org.elasticsearch.search.rescore.Rescorer;
import org.elasticsearch.search.rescore.RescorerBuilder;

import java.io.IOException;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.elasticsearch.common.xcontent.ConstructingObjectParser.constructorArg;

public class FairRescoreBuilder extends RescorerBuilder<FairRescoreBuilder> {

    public static final String NAME = "fair_rescorer";

    private static final ParseField PROTECTED_KEY   = new ParseField("protected_key");
    private static final ParseField PROTECTED_VALUE = new ParseField("protected_value");
    private static final ParseField PROTECTED_ELEMENTS_COUNT = new ParseField("protected_elements");

    private float factor;
    private String protectedKey;
    private String protectedValue;
    private int protectedElementsCount;

    public FairRescoreBuilder() {

    }

    public FairRescoreBuilder(String protectedKey, String protectedValue, int protectedElementsCount) {
        this.factor = 0.0f;

        if (protectedKey == null) {
            throw new IllegalArgumentException("[\" + NAME + \"] requires protected_key");
        }

        if (protectedValue == null) {
            throw new IllegalArgumentException("[\" + NAME + \"] requires protected_value");
        }

        this.protectedKey = protectedKey;
        this.protectedValue = protectedValue;
        this.protectedElementsCount = protectedElementsCount;

    }

    public FairRescoreBuilder(StreamInput in) throws IOException {
        super(in);
        this.protectedKey = in.readString();
        this.protectedValue = in.readString();
        this.protectedElementsCount = in.readInt();
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeString(protectedKey);
        out.writeString(protectedValue);
        out.writeInt(protectedElementsCount);
    }

    @Override
    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field(PROTECTED_KEY.getPreferredName(), protectedKey);
        builder.field(PROTECTED_VALUE.getPreferredName(), protectedValue);
        builder.field(PROTECTED_ELEMENTS_COUNT.getPreferredName(), protectedElementsCount);
    }

    private static final ConstructingObjectParser<FairRescoreBuilder, Void> PARSER = new ConstructingObjectParser<>(NAME,
            args -> new FairRescoreBuilder((String)args[0], (String)args[1], (Integer)args[2]));

    static {
        PARSER.declareString(constructorArg(), PROTECTED_KEY);
        PARSER.declareString(constructorArg(), PROTECTED_VALUE);
        PARSER.declareInt(constructorArg(), PROTECTED_ELEMENTS_COUNT);
    }

    public static FairRescoreBuilder fromXContent(XContentParser parser) throws IOException {
        return PARSER.apply(parser, null);
    }

    /**
     * Extensions override this to build the context that they need for rescoring.
     */
    @Override
    protected RescoreContext innerBuildContext(int windowSize, QueryShardContext context) throws IOException {
        return new FairRescoreContext(windowSize, protectedKey, protectedValue, protectedElementsCount, context);
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
        private int protectedElementsCount;

        FairRescoreContext(int windowSize, String protectedKey, String protectedValue, int protectedElementsCount, QueryShardContext context) {
            super(windowSize, FairRescorer.INSTANCE);
            this.protectedKey = protectedKey;
            this.protectedValue = protectedValue;
            this.protectedElementsCount = protectedElementsCount;
        }

    }

    private static class FairRescorer implements Rescorer {

        private static final FairRescorer INSTANCE = new FairRescorer();
        private final FairTopKImpl fairTopK;

        private float proportion = 0.6f;
        private float significance = 0.1f;

        public FairRescorer() {
            this(0.6f, 0.1f);
        }


        public FairRescorer(float proportion, float significance) {
            this.proportion = proportion;
            this.significance = significance;
            this.fairTopK = new FairTopKImpl();
        }

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

            PriorityQueue<ScoreDoc> p0 = new DocumentPriorityQueue(max);
            PriorityQueue<ScoreDoc> p1 = new DocumentPriorityQueue(max);

            for(int i=0; i < max; i++) {
                ScoreDoc scoreDoc = topDocs.scoreDocs[i];
                Document doc = searcher.doc(scoreDoc.doc);
                if (isProtected(doc, context)) {
                    p1.add(scoreDoc);
                } else {
                    p0.add(scoreDoc);
                }
            }
            assert p0.size() + p1.size() == max;

            return fairTopK.fairTopK(p0, p1, context.protectedElementsCount, proportion, significance);
        }

        private boolean isProtected(Document doc, FairRescoreContext context) {
            return doc.get(context.protectedKey).equals(context.protectedValue);
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
