package com.sirma.itt.emf.audit.rest.handlers.readers;

import com.sirma.itt.emf.audit.rest.AuditSearchRequest;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Converts a JSON object to {@link AuditSearchRequest}.
 *
 * @author Hristo Lungov
 */
@Provider
@Consumes(Versions.V2_JSON)
public class AuditSearchRequestReader implements MessageBodyReader<AuditSearchRequest> {

    @BeanParam
    private RequestInfo requestInfo;

    @Inject
    private JsonToConditionConverter converter;

    @Override
    public boolean isReadable(Class<?> type, Type generic, Annotation[] annotations, MediaType media) {
        return AuditSearchRequest.class.isAssignableFrom(type);
    }

    @Override
    public AuditSearchRequest readFrom(Class<AuditSearchRequest> type, Type generic, Annotation[] annotations,
                                       MediaType media, MultivaluedMap<String, String> headers, InputStream in)
            throws IOException {
        Condition tree = JSON.readObject(in, converter::parseCondition);

        MultivaluedMap<String, String> queryParams = requestInfo.getUriInfo().getQueryParameters();
        AuditSearchRequest searchRequest = new AuditSearchRequest(CollectionUtils.createHashMap(headers.size() + queryParams.size()));
        searchRequest.setSearchTree(tree);
        searchRequest.getRequest().putAll(headers);
        searchRequest.getRequest().putAll(queryParams);
        searchRequest.setDialect(SearchDialects.SOLR);
        return searchRequest;
    }
}
