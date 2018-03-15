package com.purbon.search.fair.query;

import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryCache;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class UnFairRescoreQuery extends Query {


    /**
     * Expert: Constructs an appropriate Weight implementation for this query.
     * <p>
     * Only implemented by primitive queries, which re-write to themselves.
     *
     * @param searcher
     * @param needsScores True if document scores ({@link Scorer#score}) or match
     *                    frequencies ({@link Scorer#freq}) are needed.
     * @param boost       The boost that is propagated by the parent queries.
     */
    @Override
    public Weight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
        return new RankerWeight(searcher, boost);
    }

    public class RankerWeight extends Weight {

        private final IndexSearcher searcher;
        private final float boost;
        private final Weight weight;

        public RankerWeight(IndexSearcher searcher, float boost) throws IOException {
            super(UnFairRescoreQuery.this);
            this.searcher = searcher;
            this.boost = boost;
            this.weight = searcher.createWeight(getQuery(), true, boost);
        }
        /**
         * Expert: adds all terms occurring in this query to the terms set. If the
         * {@link Weight} was created with {@code needsScores == true} then this
         * method will only extract terms which are used for scoring, otherwise it
         * will extract all terms which are used for matching.
         *
         * @param terms
         */
        @Override
        public void extractTerms(Set<Term> terms) {
            weight.extractTerms(terms);
        }

        /**
         * An explanation of the score computation for the named document.
         *
         * @param context the readers context to create the {@link Explanation} for.
         * @param doc     the document's id relative to the given context's reader
         * @return an Explanation for the score
         * @throws IOException if an {@link IOException} occurs
         */
        @Override
        public Explanation explain(LeafReaderContext context, int doc) throws IOException {
            Scorer scorer = scorer(context);
            List<Explanation> subs = Collections.emptyList();
            return Explanation.match(scorer.score(), "unfrair description", subs);
        }

        /**
         * Returns a {@link Scorer} which can iterate in order over all matching
         * documents and assign them a score.
         * <p>
         * <b>NOTE:</b> null can be returned if no documents will be scored by this
         * query.
         * <p>
         * <b>NOTE</b>: The returned {@link Scorer} does not have
         * {@link LeafReader#getLiveDocs()} applied, they need to be checked on top.
         *
         * @param context the {@link LeafReaderContext} for which to return the {@link Scorer}.
         * @return a {@link Scorer} which scores documents in/out-of order.
         * @throws IOException if there is a low-level I/O error
         */
        @Override
        public Scorer scorer(LeafReaderContext context) throws IOException {
            return new UnFairScorer(this, context);
        }
    }



        /**
     * Prints a query to a string, with <code>field</code> assumed to be the
     * default field and omitted.
     *
     * @param field
     */
    @Override
    public String toString(String field) {
        return null;
    }

    /**
     * Override and implement query instance equivalence properly in a subclass.
     * This is required so that {@link QueryCache} works properly.
     * <p>
     * Typically a query will be equal to another only if it's an instance of
     * the same class and its document-filtering properties are identical that other
     * instance. Utility methods are provided for certain repetitive code.
     *
     * @param obj
     * @see #sameClassAs(Object)
     * @see #classHash()
     */
    @Override
    public boolean equals(Object obj) {
        return false;
    }

    /**
     * Override and implement query hash code properly in a subclass.
     * This is required so that {@link QueryCache} works properly.
     *
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        return 0;
    }
}
