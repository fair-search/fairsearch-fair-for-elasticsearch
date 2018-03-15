package com.purbon.search.fair.query;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

import java.io.IOException;

public class UnFairScorer extends Scorer {

    private final LeafReaderContext context;
    private final DocIdSetIterator iterator;

    public UnFairScorer(Weight weight, LeafReaderContext context) {
        super(weight);
        this.context  = context;
        this.iterator = DocIdSetIterator.all(context.reader().maxDoc());
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
        return iterator.docID();
    }

    /**
     * Returns the score of the current document matching the query.
     * Initially invalid, until {@link DocIdSetIterator#nextDoc()} or
     * {@link DocIdSetIterator#advance(int)} is called on the {@link #iterator()}
     * the first time, or when called from within {@link LeafCollector#collect}.
     */
    @Override
    public float score() throws IOException {

        Terms terms = context.reader().getTermVector(docID(), "title");
        Scorer scorer = weight.scorer(context);
        if (terms != null) {
            return scorer.score()*terms.getDocCount();
        } else {
            return 1.0f;
        }
    }

    /**
     * Returns the freq of this Scorer on the current document
     */
    @Override
    public int freq() throws IOException {
        Scorer scorer = weight.scorer(context);
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
        return iterator;
    }
}
