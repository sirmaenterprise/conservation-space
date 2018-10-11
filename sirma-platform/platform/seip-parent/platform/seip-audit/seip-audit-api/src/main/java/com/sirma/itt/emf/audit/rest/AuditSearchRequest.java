package com.sirma.itt.emf.audit.rest;

import com.sirma.itt.seip.domain.search.SearchRequest;

import java.util.List;
import java.util.Map;

/**
 * Object that represents an audit search request used to build proper solr search query.
 *
 * @author Hristo Lungov
 */
public class AuditSearchRequest extends SearchRequest {

    /**
     * Instantiates a new search request.
     *
     * @param request the request
     */
    public AuditSearchRequest(Map<String, List<String>> request) {
        super(request);
    }

}
