package com.sirma.itt.seip.monitor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Singleton;
import javax.ws.rs.Path;

import com.sirma.itt.seip.monitor.Metric.Builder;
import com.sirma.itt.seip.monitor.annotations.MetricConfig;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition;
import com.sirma.itt.seip.monitor.annotations.Monitored;

/**
 * Scans for methods annotated with {@link Monitored} and builds and caches the
 * metric definitions.
 *
 * @author yasko
 */
@Singleton
public class MetricDefinitionsCache implements Extension {
	private Map<Method, List<Metric>> cache = new HashMap<>(128);

	/**
	 * Retrieves metrics defined for the provided method.
	 *
	 * @param m
	 *            Method for which to look for metrics.
	 * @return Defined metrics for the method or {@link Collections#emptyList()}
	 *         if no metrics are defined for the method.
	 */
	public List<Metric> getMetrics(Method m) {
		return cache.getOrDefault(m, Collections.emptyList());
	}

	<T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> type) {
		type.getAnnotatedType().getMethods().stream()
			.filter(m -> m.isAnnotationPresent(Monitored.class))
			.map(AnnotatedMethod::getJavaMember)
			.forEach(this::addToCache);
	}

	private void addToCache(Method m) {
		// rest methods are processed by a JAR-RS request/response filter
		if (m.getAnnotation(Path.class) != null || m.getDeclaringClass().getAnnotation(Path.class) != null) {
			return;
		}

		Monitored monitored = m.getAnnotation(Monitored.class);
		if (monitored == null || monitored.value().length == 0) {
			return;
		}

		List<Metric> metrics = new LinkedList<>();
		for (MetricDefinition def : monitored.value()) {
			Builder builder = Builder.newInstance(def.name(), def.type(), def.descr());
			for (MetricConfig config : def.configs()) {
				builder.config(config.key(), config.value());
			}

			metrics.add(builder.build());
		}

		if (!metrics.isEmpty()) {
			cache.put(m, metrics);
		}
	}
}
