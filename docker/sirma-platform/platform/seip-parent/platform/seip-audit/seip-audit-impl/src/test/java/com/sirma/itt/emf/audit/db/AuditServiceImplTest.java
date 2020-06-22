package com.sirma.itt.emf.audit.db;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.converter.AuditActivityConverter;
import com.sirma.itt.emf.audit.rest.AuditSearchRequest;
import com.sirma.itt.emf.audit.solr.query.ServiceResult;
import com.sirma.itt.emf.audit.solr.query.SolrResult;
import com.sirma.itt.emf.audit.solr.service.SolrService;
import com.sirma.itt.emf.audit.solr.service.SolrServiceException;
import com.sirma.itt.emf.solr.services.query.SolrSearchQueryBuilder;
import com.sirma.itt.seip.domain.search.tree.Condition;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.MultivaluedHashMap;
import java.io.IOException;
import java.util.Collections;

/**
 * Test class for {@link AuditServiceImpl}
 */
public class AuditServiceImplTest {

    @InjectMocks
    private AuditServiceImpl auditService;

    @Mock
    private SolrService solrService;

    @Mock
    private AuditDao auditDao;

    @Mock
    private AuditActivityConverter converter;

    @Mock
    private SolrSearchQueryBuilder solrSearchQueryBuilder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_be_empty_result() throws SolrServiceException {
        SolrResult solrResult = mock(SolrResult.class);
        when(solrService.getIDsFromQuery(any())).thenReturn(solrResult);
        ServiceResult serviceResult = mock(ServiceResult.class);
        when(serviceResult.getRecords()).thenReturn(Collections.emptyList());
        when(auditDao.getActivitiesByIDs(any())).thenReturn(serviceResult);
        SolrQuery solrQuery = mock(SolrQuery.class);
        Assert.assertEquals(serviceResult,auditService.getActivitiesBySolrQuery(solrQuery));
    }

    @Test
    public void should_return_single_result() throws SolrServiceException {
        SolrResult solrResult = mock(SolrResult.class);
        when(solrResult.getTotal()).thenReturn(Long.valueOf(1));
        when(solrService.getIDsFromQuery(any())).thenReturn(solrResult);
        ServiceResult serviceResult = mock(ServiceResult.class);
        AuditActivity auditActivity = mock(AuditActivity.class);
        when(serviceResult.getRecords()).thenReturn(Collections.singletonList(auditActivity));
        when(auditDao.getActivitiesByIDs(any())).thenReturn(serviceResult);
        SolrQuery solrQuery = mock(SolrQuery.class);
        Assert.assertEquals(serviceResult,auditService.getActivitiesBySolrQuery(solrQuery));
    }

    @Test
    public void test_getActivitiesWithSearchRequest() throws IOException, SolrServiceException {
        MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.put("pageSize", Collections.singletonList("1"));
        params.put("pageNumber", Collections.singletonList("1"));
        AuditSearchRequest auditSearchRequest = new AuditSearchRequest(params);
        when(solrSearchQueryBuilder.buildSolrQuery(any(Condition.class))).thenReturn("");

        SolrResult solrResult = mock(SolrResult.class);
        when(solrResult.getTotal()).thenReturn(Long.valueOf(1));
        when(solrService.getIDsFromQuery(any())).thenReturn(solrResult);
        ServiceResult serviceResult = mock(ServiceResult.class);
        AuditActivity auditActivity = mock(AuditActivity.class);
        when(serviceResult.getRecords()).thenReturn(Collections.singletonList(auditActivity));
        when(auditDao.getActivitiesByIDs(any())).thenReturn(serviceResult);
        Assert.assertEquals(serviceResult,auditService.getActivities(auditSearchRequest));
    }
}
