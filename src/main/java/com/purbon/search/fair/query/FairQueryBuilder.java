package com.purbon.search.fair.query;

import org.apache.lucene.search.Query;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryShardContext;

import java.io.IOException;
import java.util.Objects;

public class FairQueryBuilder extends AbstractQueryBuilder<FairQueryBuilder>  {

    public static final String NAME = "fair-match";

    private static final ParseField QUERY_FIELD = new ParseField("query");

    private static final ParseField PROTECTED_KEY   = new ParseField("protected_key");
    private static final ParseField PROTECTED_VALUE = new ParseField("protected_value");
    private static final ParseField PROTECTED_ELEMENTS_COUNT = new ParseField("protected_elements");

    private Object value;
    private String fieldName;

    private String protectedKey;
    private String protectedValue;
    private int protectedElementsCount;

    public FairQueryBuilder() {

    }

    public FairQueryBuilder(String fieldName, Object value) {
        this(fieldName, value, "", "", -1);
    }

    public FairQueryBuilder(String fieldName, Object value, String protectedKey, String protectedValue, int protectedElementsCount) {
        if (fieldName == null) {
            throw new IllegalArgumentException("[\" + NAME + \"] requires fieldName");
        }

        if (value == null) {
            throw new IllegalArgumentException("[\" + NAME + \"] requires query value");
        }

        this.fieldName = fieldName;
        this.value     = value;
        this.protectedKey = protectedKey;
        this.protectedValue = protectedValue;
        this.protectedElementsCount = protectedElementsCount;
    }

    public FairQueryBuilder(StreamInput in) throws IOException {
        super(in);
        this.fieldName = in.readString();
        this.value = in.readGenericValue();
        this.protectedKey = in.readString();
        this.protectedValue = in.readString();
        this.protectedElementsCount = in.readInt();
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeString(fieldName);
        out.writeGenericValue(value);
        out.writeString(protectedKey);
        out.writeString(protectedValue);
        out.writeInt(protectedElementsCount);
    }

    @Override
    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME);
        builder.startObject(fieldName);
        builder.field(QUERY_FIELD.getPreferredName(), value);
        builder.field(PROTECTED_KEY.getPreferredName(), protectedKey);
        builder.field(PROTECTED_VALUE.getPreferredName(), protectedValue);
        builder.field(PROTECTED_ELEMENTS_COUNT.getPreferredName(), protectedElementsCount);
        printBoostAndQueryName(builder);
        builder.endObject();
        builder.endObject();
    }

    @Override
    protected Query doToQuery(QueryShardContext context) throws IOException {
        FairQuery fairQuery = new FairQuery(context);
        return fairQuery.parse(fieldName, value);
    }

    /**
     * Indicates whether some other {@link QueryBuilder} object of the same type is "equal to" this one.
     */
    @Override
    protected boolean doEquals(FairQueryBuilder other) {
        return Objects.equals(fieldName, other.fieldName) && Objects.equals(value, other.value);
    }



    @Override
    protected int doHashCode() {
        return Objects.hash(fieldName, value);
    }

    public static FairQueryBuilder fromXContent(XContentParser parser) throws IOException {
        String fieldName = null;
        Object value = null;
        String protectedKey = null;
        String protectedValue = null;
        int protectedElementsCount = -1;

        float boost = AbstractQueryBuilder.DEFAULT_BOOST;

        String currentFieldName = null;
        String queryName = null;
        XContentParser.Token token;

        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_OBJECT) {
                throwParsingExceptionOnMultipleFields(NAME, parser.getTokenLocation(), fieldName, currentFieldName);
                fieldName = currentFieldName;
                while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                    if (token == XContentParser.Token.FIELD_NAME) {
                        currentFieldName = parser.currentName();
                    } else if (token.isValue()) {
                        if (QUERY_FIELD.match(currentFieldName)) {
                            value = parser.objectText();
                        } else if (PROTECTED_KEY.match(currentFieldName)) {
                            protectedKey = parser.text();
                        } else if (PROTECTED_VALUE.match(currentFieldName)) {
                            protectedValue = parser.text();
                        } else if (PROTECTED_ELEMENTS_COUNT.match(currentFieldName)) {
                            protectedElementsCount = parser.intValue();
                        } else if (AbstractQueryBuilder.BOOST_FIELD.match(currentFieldName)) {
                            boost = parser.floatValue();
                        } else if (AbstractQueryBuilder.NAME_FIELD.match(currentFieldName)) {
                            queryName = parser.text();
                        }
                        else {
                            throw new ParsingException(parser.getTokenLocation(),
                                    "[" + NAME + "] query does not support [" + currentFieldName + "]");
                        }
                    } else {
                        throw new ParsingException(parser.getTokenLocation(),
                                "[" + NAME + "] unknown token [" + token + "] after [" + currentFieldName + "]");
                    }
                }
            } else {
                throwParsingExceptionOnMultipleFields(NAME, parser.getTokenLocation(), fieldName, parser.currentName());
                fieldName = parser.currentName();
                value = parser.objectText();
            }
        }

        if (value == null) {
            throw new ParsingException(parser.getTokenLocation(), "No text specified for text query");
        }

        FairQueryBuilder queryBuilder = new FairQueryBuilder(fieldName, value, protectedKey, protectedValue, protectedElementsCount);
        queryBuilder.boost(boost);
        queryBuilder.queryName(queryName);
        return queryBuilder;
    }


    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public String getFieldName() {
        return fieldName;
    }

    /**
     * Returns the name of the writeable object
     */
    @Override
    public String getWriteableName() {
        return NAME;
    }
}
