package com.purbon.search.fair;

import com.purbon.search.fair.query.FairQueryBuilder;
import org.apache.lucene.search.Query;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.search.internal.SearchContext;
import org.elasticsearch.test.AbstractQueryTestCase;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.core.IsNull.notNullValue;

public class FairQueryBuilderTests extends AbstractQueryTestCase<FairQueryBuilder> {

    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Arrays.asList(FairSearchQueryParserPlugin.class);
    }

    /**
     * Create the query that is being tested
     */
    @Override
    protected FairQueryBuilder doCreateTestQueryBuilder() {

        int terms = randomIntBetween(1, 5);
        StringBuilder sbuilder = new StringBuilder();
        for (int i = 0; i < terms; i++) {
            sbuilder.append(randomAlphaOfLengthBetween(1, 10)).append(" ");
        }
        String value = sbuilder.toString().trim();

        return new FairQueryBuilder(STRING_FIELD_NAME, value);
    }


    public void testFairQuery() throws Exception {
        String query = " {" +
                "  \"fair-match\": {" +
                "        \"title\": \"test\"" +
                "  }" +
                "}";
        FairQueryBuilder builder = (FairQueryBuilder)parseQuery(query);
        assertNotNull(builder.getFieldName());
        assertNotNull(builder.getValue());
    }

    public void testFairQueryString() throws Exception {
        String query = "{"+
            " \"fair-match\": {"+
            " \"title\": {"+
            "   \"query\": \"test\""+
            "}"+
            "}}";

        FairQueryBuilder builder = (FairQueryBuilder)parseQuery(query);
        assertNotNull(builder.getFieldName());
        assertNotNull(builder.getValue());
    }


    /**
     * Checks the result of {@link QueryBuilder#toQuery(QueryShardContext)} given the original {@link QueryBuilder}
     * and {@link QueryShardContext}. Contains the query specific checks to be implemented by subclasses.
     */
    @Override
    protected void doAssertLuceneQuery(FairQueryBuilder queryBuilder, Query query, SearchContext context) throws IOException {
        assertThat(query, notNullValue());
    }
}
