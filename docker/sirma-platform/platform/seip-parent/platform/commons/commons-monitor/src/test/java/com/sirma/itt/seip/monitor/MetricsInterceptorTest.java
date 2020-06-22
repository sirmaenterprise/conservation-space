package com.sirma.itt.seip.monitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.interceptor.InvocationContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.monitor.Metric.Builder;

@RunWith(MockitoJUnitRunner.class)
public class MetricsInterceptorTest {

	@Mock
	Statistics stats;

	@Mock
	MetricDefinitionsCache cache;

	@Mock
	InvocationContext ctx;

	@InjectMocks
	MetricsInterceptor intercepor;

	@Test
	public void testTrackMetrics() throws Exception {
		Metric counter = Builder.counter("my_counter", "test counter").build();
		List<Metric> metrics = Arrays.asList(counter);

		Mockito.when(cache.getMetrics(Mockito.any())).thenReturn(metrics);

		intercepor.trackMetrics(ctx);

		Mockito.verify(stats).track(counter);
		Mockito.verify(ctx).proceed();
		Mockito.verify(stats).end(counter);
	}

	@Test
	public void testNoMetrics() throws Exception {
		Mockito.when(cache.getMetrics(Mockito.any())).thenReturn(Collections.emptyList());

		intercepor.trackMetrics(ctx);

		Mockito.verify(stats, Mockito.never()).track(Mockito.any());
		Mockito.verify(ctx).proceed();
		Mockito.verify(stats, Mockito.never()).end(Mockito.any());
	}
}
