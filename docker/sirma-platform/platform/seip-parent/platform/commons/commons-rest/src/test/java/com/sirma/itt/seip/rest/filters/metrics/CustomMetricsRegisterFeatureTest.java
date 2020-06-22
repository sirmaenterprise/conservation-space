package com.sirma.itt.seip.rest.filters.metrics;

import java.lang.reflect.Method;

import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.monitor.annotations.MetricConfig;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition;
import com.sirma.itt.seip.monitor.annotations.Monitored;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition.Type;


@RunWith(MockitoJUnitRunner.class)
public class CustomMetricsRegisterFeatureTest {

	@Mock
	ResourceInfo info;

	@Mock
	FeatureContext ctx;

	@Test
	public void testNoAnnotations() throws Exception {
		Method method = CustomMetricsRegisterFeatureTest.class.getDeclaredMethod("nonMonitoredMethod");
		Mockito.when(info.getResourceMethod()).thenReturn(method);

		new CustomMetricsRegisterFeature().configure(info, ctx);
		Mockito.verify(ctx, Mockito.never()).register(Mockito.any());
	}

	@Test
	public void testEmptyAnnotation() throws Exception {
		Method method = CustomMetricsRegisterFeatureTest.class.getDeclaredMethod("emptyMonitoredMethod");
		Mockito.when(info.getResourceMethod()).thenReturn(method);

		new CustomMetricsRegisterFeature().configure(info, ctx);
		Mockito.verify(ctx, Mockito.never()).register(Mockito.any());
	}

	@Test
	public void testMonitored() throws Exception {
		Method method = CustomMetricsRegisterFeatureTest.class.getDeclaredMethod("monitoredMethod");
		Mockito.when(info.getResourceMethod()).thenReturn(method);

		new CustomMetricsRegisterFeature().configure(info, ctx);
		Mockito.verify(ctx).register(Mockito.any(CustomMetricsFilter.class));
	}

	void nonMonitoredMethod() {
		// used for test data only
	}

	@Monitored
	void emptyMonitoredMethod() {
		// used for test data only
	}

	@Monitored({ @MetricDefinition(name = "test", type = Type.GAUGE, configs = {
			@MetricConfig(key = "help", value = "just a test") }) })
	void monitoredMethod() {
		// used for test data only
	}
}
