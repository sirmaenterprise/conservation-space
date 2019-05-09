package com.sirmaenterprise.sep.monitor.prometheus;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.monitor.Metric;
import com.sirma.itt.seip.monitor.Metric.Builder;
import com.sirma.itt.seip.security.context.SecurityContext;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Histogram.Child;
import io.prometheus.client.Histogram.Timer;

@RunWith(MockitoJUnitRunner.class)
public class PrometheusStatisticsTest {

	/**
	 * Mock thread local.
	 *
	 * @author yasko
	 */
	public class ThreadLocalWithInitial extends ThreadLocal<Map<String, Timer>> {
		@Override
		protected Map<String, Timer> initialValue() {
			return new HashMap<>();
		}
	}

	@Spy
	Map<String, Histogram> histograms = new HashMap<>();

	@Spy
	Map<String, Gauge> gauges = new HashMap<>();

	@Spy
	Map<String, Counter> counters = new HashMap<>();

	@Spy
	ThreadLocal<Map<String, Timer>> timers = new ThreadLocalWithInitial();

	@Mock
	SecurityContext securityContext;

	@InjectMocks
	PrometheusStatistics stats;

	@Before
	public void init() {
		Mockito.when(securityContext.getCurrentTenantId()).thenReturn("test_tenant");
	}

	@Test
	public void testCreateCounter() {
		Metric metric = Builder.counter("my_test_counter", "Test counter").config("key1", "v1").config("key2", "v2")
				.build();
		stats.track(metric);

		Assert.assertTrue(counters.containsKey(metric.name()));
	}

	@Test
	public void testCreateGauge() {
		Metric metric = Builder.gauge("my_test_gauge", "Test gauge").build();
		stats.track(metric);

		Assert.assertTrue(gauges.containsKey(metric.name()));
	}

	@Test
	public void testHistogramTimer() {
		Metric m = Builder.timer("histogram_timer", "Test histogram timer.").build();

		stats.track(m);
		Timer timer = timers.get().get(m.name());
		Assert.assertNotNull(timer);

		timer = Mockito.mock(Timer.class);
		timers.get().put(m.name(), timer);
		stats.end(m);
		Mockito.verify(timer).observeDuration();
	}

	@Test
	public void testHistogram() {
		Child child = Mockito.mock(Child.class);
		Histogram histogram = Mockito.mock(Histogram.class);
		Mockito.when(histogram.labels(Mockito.anyVararg())).thenReturn(child);
		histograms.put("histogram", histogram);
		Metric m = Builder.histogram("histogram", "Test histogram.").build();

		double amt = 10;
		stats.value(m.name(), amt);
		stats.end(m);
		Mockito.verify(child).observe(amt);
	}

	@Test
	public void testGauge() {
		Gauge gauge = Mockito.mock(Gauge.class);
		io.prometheus.client.Gauge.Child child = Mockito.mock(io.prometheus.client.Gauge.Child.class);
		Mockito.when(gauge.labels(Mockito.anyVararg())).thenReturn(child);
		gauges.put("test", gauge);

		Metric m = Builder.gauge("test", "gauge").build();
		stats.track(m);
		Mockito.verify(child).inc();

		stats.end(m);
		Mockito.verify(child).dec();
	}

	@Test
	public void testCounter() {
		Counter counter = Mockito.mock(Counter.class);
		io.prometheus.client.Counter.Child child = Mockito.mock(io.prometheus.client.Counter.Child.class);
		Mockito.when(counter.labels(Mockito.anyVararg())).thenReturn(child);
		counters.put("test", counter);

		Metric m = Builder.counter("test", "").build();
		stats.track(m);
		stats.end(m);
		Mockito.verify(child).inc();
	}
}
