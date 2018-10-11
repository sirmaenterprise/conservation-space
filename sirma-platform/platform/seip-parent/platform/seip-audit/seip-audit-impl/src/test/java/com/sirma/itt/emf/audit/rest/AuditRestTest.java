package com.sirma.itt.emf.audit.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sirma.itt.emf.audit.db.AuditService;
import com.sirma.itt.emf.audit.solr.service.SolrServiceException;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import javax.ws.rs.core.MultivaluedHashMap;
import java.io.IOException;
import java.util.Collections;

/**
 * Test class for {@link AuditRestService}.
 *
 * @author Hristo Lungov
 */
public class AuditRestTest {

    @InjectMocks
    public AuditRestService auditRestService;

    @Mock
    private AuditService auditService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_pass_audit_events() throws IOException, SolrServiceException {
        Condition tree = mock(Condition.class);
        MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.put("pageSize", Collections.singletonList("1"));
        params.put("pageNumber", Collections.singletonList("1"));
        AuditSearchRequest auditSearchRequest = new AuditSearchRequest(params);
        auditSearchRequest.setSearchTree(tree);
        auditRestService.searchEvents(auditSearchRequest);
        Mockito.verify(auditService).getActivities(Matchers.eq(auditSearchRequest));
    }

    @Test(expected = ResourceException.class)
    public void should_not_pass_audit_events() throws IOException, SolrServiceException {
        Condition tree = mock(Condition.class);
        MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.put("pageSize", Collections.singletonList("1"));
        params.put("pageNumber", Collections.singletonList("1"));
        AuditSearchRequest auditSearchRequest = new AuditSearchRequest(params);
        auditSearchRequest.setSearchTree(tree);
        when(auditService.getActivities(auditSearchRequest)).thenThrow(new SolrServiceException());
        auditRestService.searchEvents(auditSearchRequest);
    }
}
