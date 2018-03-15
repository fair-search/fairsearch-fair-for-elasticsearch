package com.purbon.search.fair.query;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.IndexOptions;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.SynonymQuery;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.LeafCollector;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.common.unit.Fuzziness;

import org.elasticsearch.index.cache.query.QueryCache;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.index.query.support.QueryParsers;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import static org.elasticsearch.common.lucene.search.Queries.newLenientFieldQuery;
import static org.elasticsearch.common.lucene.search.Queries.newUnmappedFieldQuery;
import static org.elasticsearch.index.search.MatchQuery.DEFAULT_LENIENCY;

public class FairQuery extends Query {

    private QueryShardContext context;
    private String fieldName;
    private Object value;

    private Fuzziness fuzziness = null;
    private int fuzzyPrefixLength = FuzzyQuery.defaultPrefixLength;
    private int maxExpansions = FuzzyQuery.defaultMaxExpansions;
    private boolean transpositions = FuzzyQuery.defaultTranspositions;

    private boolean lenient = DEFAULT_LENIENCY;

    private BooleanClause.Occur occur = BooleanClause.Occur.SHOULD;

    FairQuery(QueryShardContext context) {
        this.context = context;
    }

    FairQuery(String fieldName, Object value) {
        this.fieldName = fieldName;
        this.value = value;
    }

    private boolean hasPositions(MappedFieldType fieldType) {
        return fieldType.indexOptions().compareTo(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS) >= 0;
    }

    protected Analyzer getAnalyzer(MappedFieldType fieldType, boolean quoted) {
        return quoted ? context.getSearchQuoteAnalyzer(fieldType) : context.getSearchAnalyzer(fieldType);
    }

    public Query parse(String fieldName, Object value) throws IOException {

        MappedFieldType fieldType = context.fieldMapper(fieldName);
        if (fieldType == null) {
            return newUnmappedFieldQuery(fieldName);
        }
        final String field = fieldType.name();

        Analyzer analyzer = getAnalyzer(fieldType, false);
        assert analyzer != null;
        FairQueryBuilder builder = new FairQueryBuilder(analyzer, fieldType);
        Query query = builder.createBooleanQuery(field, value.toString(), occur);

        return query;
    }

    protected final Query termQuery(MappedFieldType fieldType, BytesRef value, boolean lenient) {
        try {
            return fieldType.termQuery(value, context);
        } catch (RuntimeException e) {
            if (lenient) {
                return newLenientFieldQuery(fieldType.name(), e);
            }
            throw e;
        }
    }

    /**
     * Expert: Constructs an appropriate Weight implementation for this query.
     * <p>
     * Only implemented by primitive queries, which re-write to themselves.
     */
    @Override
    public Weight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
        return new FairQuery.FairWeight(searcher, needsScores);
    }

    /**
     * Override and implement query instance equivalence properly in a subclass.
     * This is required so that {@link QueryCache} works properly.
     * <p>
     * Typically a query will be equal to another only if it's an instance of
     * the same class and its document-filtering properties are identical that other
     * instance. Utility methods are provided for certain repetitive code.
     *
     * @see #sameClassAs(Object)
     * @see #classHash()
     */
    @Override
    public boolean equals(Object other) {
        return sameClassAs(other) &&
                equalsTo(getClass().cast(other));
    }

    private boolean equalsTo(FairQuery other) {
        return Objects.equals(fieldName, other.fieldName);
    }

    public String toString(String field) {
        return value.toString();
    };

    /**
     * Override and implement query hash code properly in a subclass.
     * This is required so that {@link QueryCache} works properly.
     *
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        return  Objects.hash(fieldName);
    }

    private class FairWeight extends Weight {

        protected final Weight weight;

        FairWeight(IndexSearcher searcher, boolean needsScores) throws IOException {
            super(FairQuery.this);
            weight = searcher.createWeight(FairQuery.this, true, 1f);

        }

        /**
         * Expert: adds all terms occurring in this query to the terms set. If the
         * {@link Weight} was created with {@code needsScores == true} then this
         * method will only extract terms which are used for scoring, otherwise it
         * will extract all terms which are used for matching.
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

            if (scorer != null) {
                int newDoc = scorer.iterator().advance(doc);
                if (newDoc == doc) {
                    return Explanation.match(
                            scorer.score(),
                            "Stat Score" );
                }
            }
            return Explanation.noMatch("no matching term");
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
            Scorer subscorer = weight.scorer(context);
            return new FairScorer(weight, context, subscorer);
        }
    }

    private class FairScorer extends Scorer {

        private LeafReaderContext context;
        private Scorer scorer;

        FairScorer(Weight weight, LeafReaderContext context, Scorer subscorer) {
            super(weight);
            this.context = context;
            this.scorer = subscorer;
        }

        /**
         * Returns the doc ID that is currently being scored.
         * This will return {@code -1} if the {@link #iterator()} is not positioned
         * or {@link DocIdSetIterator#NO_MORE_DOCS} if it has been entirely consumed.
         *
         * @see DocIdSetIterator#docID()
         */
        @Override
        public int docID() {
            return scorer.docID();
        }

        /**
         * Returns the score of the current document matching the query.
         * Initially invalid, until {@link DocIdSetIterator#nextDoc()} or
         * {@link DocIdSetIterator#advance(int)} is called on the {@link #iterator()}
         * the first time, or when called from within {@link LeafCollector#collect}.
         */
        @Override
        public float score() throws IOException {
            return scorer.score();
        }

        /**
         * Returns the freq of this Scorer on the current document
         */
        @Override
        public int freq() throws IOException {
            return scorer.freq();
        }

        /**
         * Return a {@link DocIdSetIterator} over matching documents.
         * <p>
         * The returned iterator will either be positioned on {@code -1} if no
         * documents have been scored yet, {@link DocIdSetIterator#NO_MORE_DOCS}
         * if all documents have been scored already, or the last document id that
         * has been scored otherwise.
         * <p>
         * The returned iterator is a view: calling this method several times will
         * return iterators that have the same state.
         */
        @Override
        public DocIdSetIterator iterator() {
            return null;
        }
    }

    private class FairQueryBuilder extends QueryBuilder {

        private final MappedFieldType mapper;

        /**
         * Creates a new QueryBuilder using the given analyzer.
         */
        FairQueryBuilder(Analyzer analyzer, MappedFieldType mapper) {
            super(analyzer);
            this.mapper = mapper;
        }


        @Override
        protected Query newTermQuery(Term term) {
            return blendTermQuery(term, mapper);
        }

        @Override
        protected Query newSynonymQuery(Term[] terms) {
            return blendTermsQuery(terms, mapper);
        }

        @Override
        protected Query analyzePhrase(String field, TokenStream stream, int slop) throws IOException {
            if (hasPositions(mapper) == false) {
                IllegalStateException exc =
                        new IllegalStateException("field:[" + field + "] was indexed without position data; cannot run PhraseQuery");
                if (lenient) {
                    return newLenientFieldQuery(field, exc);
                } else {
                    throw exc;
                }
            }
            return super.analyzePhrase(field, stream, slop);
        }

        protected Query blendTermsQuery(Term[] terms, MappedFieldType fieldType) {
            return new SynonymQuery(terms);
        }

        protected Query blendTermQuery(Term term, MappedFieldType fieldType) {
            if (fuzziness != null) {
                try {
                    Query query = fieldType.fuzzyQuery(term.text(), fuzziness, fuzzyPrefixLength, maxExpansions, transpositions);
                    if (query instanceof FuzzyQuery) {
                        QueryParsers.setRewriteMethod((FuzzyQuery) query, FuzzyQuery.CONSTANT_SCORE_REWRITE);
                    }
                    return query;
                } catch (RuntimeException e) {
                    if (lenient) {
                        return newLenientFieldQuery(fieldType.name(), e);
                    } else {
                        throw e;
                    }
                }
            }
            return termQuery(fieldType, term.bytes(), lenient);
        }
    }

}
