package com.sirma.itt.seip.monitor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.monitor.annotations.MetricDefinition.Type;

/**
 * Defines a platform metric.
 *
 * @author yasko
 */
public class Metric {

	/**
	 * Helper for building metric definitions.
	 *
	 * @author yasko
	 */
	public static class Builder {
		private Metric metric;

		public Builder() {
			this.metric = new Metric();
		}

		public Builder name(String name) {
			metric.name = name;
			return this;
		}

		public Builder type(Type type) {
			metric.type = type;
			return this;
		}

		public Builder description(String descr) {
			metric.descr = descr;
			return this;
		}

		public Builder config(String key, String value) {
			if (metric.configs == null) {
				metric.configs = new HashMap<>();
			}
			metric.configs.put(key, value);
			return this;
		}

		/**
		 * Builds the final metric definition.
		 *
		 * @return {@link Metric} definition.
		 */
		public Metric build() {
			if (StringUtils.isBlank(metric.descr)) {
				metric.descr = metric.name;
			}
			return metric;
		}

		/**
		 * Creates an instance of {@link Builder} to be used to build a new
		 * {@link Metric} definition.
		 *
		 * @param name
		 *            Name of the metric.
		 * @param type
		 *            Type of the metric.
		 * @param descr
		 *            Short description of the metric. If not set the name of
		 *            the metric will be used instead.
		 * @return A new instance of {@link Builder} initialized with the
		 *         provided metric name and type.
		 */
		public static Builder newInstance(String name, Type type, String descr) {
			return new Builder().name(name).type(type).description(descr);
		}

		/**
		 * Creates a new metric builder initialized with {@link Type#HISTOGRAM}
		 * type.
		 *
		 * @param name
		 *            Name of the metric.
		 * @param descr
		 *            Optional description.
		 * @return A new instance of {@link Builder} initialized with histogram
		 *         type.
		 */
		public static Builder histogram(String name, String descr) {
			return Builder.newInstance(name, Type.HISTOGRAM, descr);
		}

		/**
		 * Creates a new metric builder initialized with
		 * {@link Type#TIMER} type.
		 *
		 * @param name
		 *            Name of the metric.
		 * @param descr
		 *            Optional description.
		 * @return A new instance of {@link Builder} initialized with histogram
		 *         timer type.
		 */
		public static Builder timer(String name, String descr) {
			return Builder.newInstance(name, Type.TIMER, descr);
		}

		/**
		 * Creates a new metric builder initialized with {@link Type#COUNTER}
		 * type.
		 *
		 * @param name
		 *            Name of the metric.
		 * @param descr
		 *            Optional description.
		 * @return A new instance of {@link Builder} initialized with counter
		 *         type.
		 */
		public static Builder counter(String name, String descr) {
			return Builder.newInstance(name, Type.COUNTER, descr);
		}

		/**
		 * Creates a new metric builder initialized with {@link Type#GAUGE}
		 * type.
		 *
		 * @param name
		 *            Name of the metric.
		 * @param descr
		 *            Optional description.
		 * @return A new instance of {@link Builder} initialized with gauge
		 *         type.
		 */
		public static Builder gauge(String name, String descr) {
			return Builder.newInstance(name, Type.GAUGE, descr);
		}
	}

	private String name;
	private Type type;
	private String descr;
	private Map<String, String> configs;

	private Metric() {
		// only thru the builder
	}

	/**
	 * Getter for the metric name.
	 *
	 * @return Metric name.
	 */
	public String name() {
		return name;
	}

	/**
	 * Getter for the metric type.
	 *
	 * @return Metric type.
	 */
	public Type type() {
		return type;
	}

	/**
	 * Getter for the metric description.
	 *
	 * @return Metric description.
	 */
	public String description() {
		if (StringUtils.isBlank(descr)) {
			return name;
		}
		return descr;
	}

	/**
	 * Getter for a metric configuration.
	 *
	 * @param key
	 *            Name of the metric configuration.
	 * @return
	 */
	public String config(String key) {
		return configs.get(key);
	}
}
