package com.sirma.itt.seip.rest.filters.metrics;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.monitor.Metric;
import com.sirma.itt.seip.monitor.Statistics;

@RunWith(MockitoJUnitRunner.class)
public class CommonRestMetricsFilterTest {

	@Mock
	Statistics statistics;

	@Mock
	ContainerRequestContext request;

	@Mock
	UriInfo uriInfo;

	@Mock
	MultivaluedMap<String, String> query;

	@InjectMocks
	CommonRestMetricsFilter filter;

	@Before
	public void init() {
		Mockito.when(request.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(query);
	}

	@Test
	public void testIgrnoredRequest() throws IOException {
		Mockito.when(query.containsKey(Mockito.anyString())).thenReturn(true);
		filter.filter(request);

		Mockito.verify(statistics, Mockito.never()).track(Mockito.any(Metric.class));
	}

	@Test
	public void testTrack() throws IOException {
		Mockito.when(query.containsKey(Mockito.anyString())).thenReturn(false);
		filter.filter(request);

		Mockito.verify(statistics, Mockito.times(2)).track(Mockito.any(Metric.class));
	}

	@Test
	public void testIgrnoredResponse() throws IOException {
		Mockito.when(query.containsKey(Mockito.anyString())).thenReturn(true);
		filter.filter(request, null);

		Mockito.verify(statistics, Mockito.never()).track(Mockito.any(Metric.class));
	}

	@Test
	public void testEnd() throws IOException {
		Mockito.when(query.containsKey(Mockito.anyString())).thenReturn(false);
		filter.filter(request, null);

		Mockito.verify(statistics, Mockito.times(2)).end(Mockito.any(Metric.class));
	}
}
