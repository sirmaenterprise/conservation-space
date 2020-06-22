package com.sirma.itt.emf.audit.rest.handlers.readers;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sirma.itt.emf.audit.rest.AuditSearchRequest;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

/**
 * Test class for {@link AuditSearchRequestReader}.
 *
 * @author Hristo Lungov
 */
public class AuditSearchRequestReaderTest {

    private static final String TEST_SEARCH_REQUEST = "audit-search-request.json";

    @InjectMocks
    public AuditSearchRequestReader auditSearchRequestReader;

    @Mock
    private RequestInfo requestInfo;

    @Spy
    private JsonToConditionConverter converter = new JsonToConditionConverter();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_be_readable() {
        Assert.assertTrue(auditSearchRequestReader.isReadable(AuditSearchRequest.class, null, null, null));
    }

    @Test
    public void should_not_be_readable() {
        Assert.assertFalse(auditSearchRequestReader.isReadable(SearchRequest.class, null, null, null));
    }

    @Test
    public void should_successfull_readFrom() throws IOException {
        InputStream in = AuditSearchRequestReaderTest.class.getClassLoader().getResourceAsStream(TEST_SEARCH_REQUEST);
        UriInfo uriInfo = mock(UriInfo.class);
        MultivaluedHashMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.put("test", Collections.singletonList("test"));
        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(requestInfo.getUriInfo()).thenReturn(uriInfo);
        AuditSearchRequest auditSearchRequest = auditSearchRequestReader.readFrom(null, null,
                null, null, new MultivaluedHashMap<>(), in);
        Assert.assertEquals(queryParameters,auditSearchRequest.getRequest());
        Assert.assertNotNull(auditSearchRequest.getSearchTree());
    }
}
