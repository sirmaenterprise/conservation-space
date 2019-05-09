package com.sirma.itt.seip.rest.filters.metrics;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.monitor.Metric;
import com.sirma.itt.seip.monitor.Metric.Builder;
import com.sirma.itt.seip.monitor.Statistics;

@RunWith(MockitoJUnitRunner.class)
public class CustomMetricsFilterTest {

	@Mock
	private Statistics stats;

	private Metric m1 = Builder.counter("m1", "counter").build();
	private Metric m2 = Builder.gauge("m2", null).build();

	@Spy
	private List<Metric> metrics = new LinkedList<>(Arrays.asList(m1, m2));

	@InjectMocks
	private CustomMetricsFilter filter;

	@Test
	public void testFilterRequest() throws IOException {
		filter.filter(null);

		Mockito.verify(stats, Mockito.times(2)).track(Mockito.any());
		InOrder inOrder = Mockito.inOrder(stats);
		inOrder.verify(stats).track(m1);
		inOrder.verify(stats).track(m2);
	}

	@Test
	public void testFilterResponse() throws IOException {
		filter.filter(null, null);

		Mockito.verify(stats, Mockito.times(2)).end(Mockito.any());
		InOrder inOrder = Mockito.inOrder(stats);
		inOrder.verify(stats).end(m1);
		inOrder.verify(stats).end(m2);
	}
}
