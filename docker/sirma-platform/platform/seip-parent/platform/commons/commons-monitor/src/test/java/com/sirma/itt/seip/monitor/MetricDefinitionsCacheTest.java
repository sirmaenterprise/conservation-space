package com.sirma.itt.seip.monitor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.ws.rs.Path;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.monitor.annotations.MetricDefinition;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition.Type;
import com.sirma.itt.seip.monitor.annotations.Monitored;

@RunWith(MockitoJUnitRunner.class)
public class MetricDefinitionsCacheTest {

	@Path("/test")
	public class RestService {

		@Monitored
		public void delete() {
			// just as test data
		}

		@Monitored
		@Path("/all")
		public void deleteAll() {
			// just as test data
		}
	}

	@Spy
	private Map<Method, List<Metric>> cache = new HashMap<>();

	@Mock
	@SuppressWarnings("rawtypes")
	AnnotatedType type;

	@Mock
	@SuppressWarnings("rawtypes")
	ProcessAnnotatedType processedType;

	@InjectMocks
	MetricDefinitionsCache definitions;

	public void notMonitoredButSomehowInTheList() {
		// just as test data
	}

	@Monitored
	public void monitoredButNoMetrics() {
		// just as test data
	}

	@Monitored(@MetricDefinition(name = "test_metric", type = Type.COUNTER))
	public void monitoredWithMetrics() {
		// just as test data
	}

	@Before
	@SuppressWarnings("unchecked")
	public void init() throws Exception {
		Mockito.when(processedType.getAnnotatedType()).thenReturn(type);

		Method rest1 = RestService.class.getMethod("delete");
		Method rest2 = RestService.class.getMethod("deleteAll");
		Method notMonitoredButSomehowInTheList = MetricDefinitionsCacheTest.class.getMethod("notMonitoredButSomehowInTheList");
		Method monitoredButNoMetrics = MetricDefinitionsCacheTest.class.getMethod("monitoredButNoMetrics");
		Method monitoredWithMetrics = MetricDefinitionsCacheTest.class.getMethod("monitoredWithMetrics");

		AnnotatedMethod<MetricDefinitionsCacheTest> first = Mockito.mock(AnnotatedMethod.class);
		Mockito.when(first.getJavaMember()).thenReturn(rest1);
		Mockito.when(first.isAnnotationPresent(Monitored.class)).thenReturn(rest1.isAnnotationPresent(Monitored.class));
		AnnotatedMethod<MetricDefinitionsCacheTest> second = Mockito.mock(AnnotatedMethod.class);
		Mockito.when(second.getJavaMember()).thenReturn(rest2);
		Mockito.when(second.isAnnotationPresent(Monitored.class)).thenReturn(rest2.isAnnotationPresent(Monitored.class));
		AnnotatedMethod<MetricDefinitionsCacheTest> third = Mockito.mock(AnnotatedMethod.class);
		Mockito.when(third.getJavaMember()).thenReturn(notMonitoredButSomehowInTheList);
		Mockito.when(third.isAnnotationPresent(Monitored.class)).thenReturn(notMonitoredButSomehowInTheList.isAnnotationPresent(Monitored.class));
		AnnotatedMethod<MetricDefinitionsCacheTest> fourth = Mockito.mock(AnnotatedMethod.class);
		Mockito.when(fourth.getJavaMember()).thenReturn(monitoredButNoMetrics);
		Mockito.when(fourth.isAnnotationPresent(Monitored.class)).thenReturn(monitoredButNoMetrics.isAnnotationPresent(Monitored.class));
		AnnotatedMethod<MetricDefinitionsCacheTest> fifth = Mockito.mock(AnnotatedMethod.class);
		Mockito.when(fifth.getJavaMember()).thenReturn(monitoredWithMetrics);
		Mockito.when(fifth.isAnnotationPresent(Monitored.class)).thenReturn(monitoredWithMetrics.isAnnotationPresent(Monitored.class));

		Mockito.when(type.getMethods()).thenReturn(new HashSet<>(Arrays.asList(first, second, third, fourth, fifth)));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testObserver() {
		definitions.processAnnotatedType(processedType);

		Assert.assertEquals(1, cache.size());
		List<Metric> metrics = cache.entrySet().iterator().next().getValue();
		Assert.assertEquals(1, metrics.size());

		Metric metric = metrics.get(0);
		Assert.assertEquals("test_metric", metric.name());
		Assert.assertEquals(Type.COUNTER, metric.type());
		Assert.assertEquals("test_metric", metric.description());
	}
}
