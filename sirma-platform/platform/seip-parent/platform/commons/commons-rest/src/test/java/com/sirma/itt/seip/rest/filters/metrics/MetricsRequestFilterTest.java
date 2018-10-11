package com.sirma.itt.seip.rest.filters.metrics;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Test for {@link MetricsRequestFilter}
 *
 * @author BBonev
 */
public class MetricsRequestFilterTest {

	private MetricsRequestFilter filter;

	@Spy
	private Statistics statistics = Statistics.NO_OP;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private ContainerRequestContext requestContext;
	@Mock
	private ContainerResponseContext responseContext;
	@Mock
	private UriInfo info;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(securityContext.getCurrentTenantId()).thenReturn("test-tenant.com");
		when(requestContext.getUriInfo()).thenReturn(info);
		when(requestContext.getMethod()).thenReturn("GET");
		when(info.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());

		filter = new MetricsRequestFilter("path", statistics, securityContext);
	}

	@Test
	public void should_doNothing_inTestMode() throws Exception {
		info.getQueryParameters().putSingle(RequestParams.KEY_DISABLE_METRICS, "");

		// should force fail marker
		when(responseContext.getStatus()).thenReturn(400);

		filter.filter(requestContext);
		filter.filter(requestContext, responseContext);
		verify(statistics, never()).updateMeter(any(), anyString());
		verify(statistics, never()).createTimeStatistics(any(), anyString());
		verify(statistics, never()).getCounter(any(), anyString());
	}

	@Test
	public void should_trackMetrics() throws Exception {
		filter.filter(requestContext);
		filter.filter(requestContext, responseContext);
		verify(statistics).updateMeter(any(), eq("test_tenant_com_rest_get_path_rate"));
		verify(statistics).createTimeStatistics(any(), eq("test_tenant_com_rest_get_path_time"));
		verify(statistics, never()).updateMeter(any(), eq("system_tenant_rest_get_path_fail"));
	}

	@Test
	public void should_trackMetrics_onFailedRequests() throws Exception {
		when(responseContext.getStatus()).thenReturn(400);

		filter.filter(requestContext);
		filter.filter(requestContext, responseContext);
		verify(statistics).updateMeter(any(), eq("test_tenant_com_rest_get_path_rate"));
		verify(statistics).createTimeStatistics(any(), eq("test_tenant_com_rest_get_path_time"));
		verify(statistics).updateMeter(any(), eq("system_tenant_rest_get_path_fail"));
	}
}
