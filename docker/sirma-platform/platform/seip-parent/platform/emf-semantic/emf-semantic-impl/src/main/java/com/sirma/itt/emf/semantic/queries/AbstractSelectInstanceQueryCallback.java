package com.sirma.itt.emf.semantic.queries;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.search.NamedQueries.Projections;
import com.sirma.itt.seip.search.NamedQueries.Params;
import com.sirma.itt.semantic.queries.QueryBuilderCallback;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Abstract class for query builder extension for semantic query that allows loading one or more instances.
 *
 * @author Boyan Tonchev.
 */
public abstract class AbstractSelectInstanceQueryCallback implements QueryBuilderCallback {

    protected static final String URI = " ?" + Projections.URI;

    protected static final String IS_DELETED = "\t " + URI + " " + SPARQLQueryHelper.IS_NOT_DELETED + "\n";

    protected static final String SELECT_SINGLE = URI + " = %s ";

    protected static final Set<String> PARAM_NAMES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(Params.URIS)));

    protected static final String SELECT_MULTIPLE_END = ").}\n";

    @Override
    public String singleValue(Serializable object, Map<String, Object> params, List<Function<String, String>> filters) {
        String uri = object.toString();
        if (!uri.startsWith("<") && uri.contains("http")) {
            uri = "<" + uri + ">";
        }
        return String.format(SELECT_SINGLE, uri);
    }

    @Override
    public String getStart(List<Function<String, String>> filters, Collection<String> projections) {
        if (Options.ALLOW_LOADING_OF_DELETED_INSTANCES.isEnabled()) {
            return String.format(getMultipleStart(), "", "");
        }
        return String.format(getMultipleStart(), IS_DELETED, IS_DELETED);
    }

    public abstract String getMultipleStart();

    @Override
    public String getEnd() {
        return SELECT_MULTIPLE_END;
    }

    @Override
    public Set<String> paramNames() {
        return PARAM_NAMES;
    }

    @Override
    public String collectionParamName() {
        return Params.URIS;
    }

    @Override
    public String getSeparator() {
        return SPARQLQueryHelper.FILTER_OR;
    }
}
