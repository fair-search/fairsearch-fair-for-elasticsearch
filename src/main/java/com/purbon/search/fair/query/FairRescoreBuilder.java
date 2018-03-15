package com.purbon.search.fair.query;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.ParsingException;
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
import java.util.Arrays;
import java.util.Set;

import static java.util.Arrays.asList;

public class FairRescoreBuilder extends RescorerBuilder<FairRescoreBuilder> {

    public static final String NAME = "fair_rescorer";

    private float factor;

    public  FairRescoreBuilder() {
        this.factor = 0.0f;
    }

    public FairRescoreBuilder(StreamInput in) throws IOException {
        super(in);
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        super.writeTo(out);
    }

    @Override
    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        //builder.startObject(NAME);
        //builder.endObject();
    }

    private static final ConstructingObjectParser<FairRescoreBuilder, Void> PARSER = new ConstructingObjectParser<>(NAME,
            args -> new FairRescoreBuilder());

    public static FairRescoreBuilder fromXContent(XContentParser parser) throws IOException {
        String fieldName = null;
        FairRescoreBuilder rescorer = new FairRescoreBuilder();
        Integer windowSize = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                fieldName = parser.currentName();
            } else if (token.isValue()) {
                if (NAME == fieldName) {
                    windowSize = parser.intValue();
                } else {
                    throw new ParsingException(parser.getTokenLocation(), "rescore doesn't support [" + fieldName + "]");
                }
            } else if (token == XContentParser.Token.VALUE_NULL) {
                //rescorer = parser.namedObject(FairRescoreBuilder.class, fieldName, null);
            } else {
                throw new ParsingException(parser.getTokenLocation(), "unexpected token [" + token + "] after [" + fieldName + "]");
            }
        }

        if (windowSize != null) {
            rescorer.windowSize(windowSize.intValue());
        }
        return rescorer;
    }

    /**
     * Extensions override this to build the context that they need for rescoring.
     *
     * @param windowSize
     * @param context
     */
    @Override
    protected RescoreContext innerBuildContext(int windowSize, QueryShardContext context) throws IOException {
        return new FairRescoreContext(windowSize, context);
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
     *
     * @param ctx
     */
    @Override
    public RescorerBuilder<FairRescoreBuilder> rewrite(QueryRewriteContext ctx) throws IOException {
        return this;
    }

    private class FairRescoreContext extends RescoreContext {
        FairRescoreContext(int windowSize, QueryShardContext context) {
            super(windowSize, FairRescorer.INSTANCE);
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
            int max = Math.min(topDocs.scoreDocs.length, rescoreContext.getWindowSize());
            for(int i=0; i < max; i++) {
                topDocs.scoreDocs[i].score = 10;
            }
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
         *
         * @param searcher
         * @param rescoreContext
         * @param termsSet
         */
        @Override
        public void extractTerms(IndexSearcher searcher, RescoreContext rescoreContext, Set<Term> termsSet) throws IOException {

        }
    }
}
