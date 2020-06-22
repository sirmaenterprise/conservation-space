package com.sirma.itt.seip.instance.util;

import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Comparator for {@link Instance}s by one of their properties. <br>
 * <b>Note:</b> The comparator does not handle non comparable values like collections.
 *
 * @author yasko
 * @author bbonev
 */
public class InstancePropertyComparator implements Comparator<Instance> {

	/**
	 * Comparator by instance title
	 */
	public static final InstancePropertyComparator BY_TITLE_COMPARATOR = InstancePropertyComparator.forProperty(
			DefaultProperties.TITLE).localeSensitive().build();

	private final String property;
	private final boolean ascending;
	private final Collator collator;

	/**
	 * Constructor.
	 *
	 * @param property property name to compare by.
	 * @param ascending if the comparing should be ascending or descending. The default is ascending
	 * @param collator the {@link Collator} instance to use for text fields if required. If null then no natural
	 * language sorting will be done
	 */
	private InstancePropertyComparator(String property, boolean ascending, Collator collator) {
		this.property = property;
		this.ascending = ascending;
		this.collator = collator;
	}

	/**
	 * Create a builder that can be used to configure and build instances of InstancePropertyComparator
	 *
	 * @param property a required property name to be used to fetch the instance property for comparison. if null no
	 * comparison will occur and all invocations will mean `equal`.
	 * @return new builder instance
	 */
	public static InstancePropertyComparator.Builder forProperty(String property) {
		return new Builder(property);
	}

	@Override
	@SuppressWarnings("unchecked")
	public int compare(Instance o1, Instance o2) {
		if (o1 == o2 || property == null) {
			return 0;
		}

		Serializable v1 = o1.get(property, "");
		Serializable v2 = o2.get(property, "");

		if (v1 instanceof String && v2 instanceof String && collator != null) {
			int compare = collator.compare(v1, v2);
			return ascending ? compare : -compare;
		}

		if (v1 instanceof Comparable && v2 instanceof Comparable) {
			int compare = ((Comparable<Serializable>) v1).compareTo(v2);
			return ascending ? compare : -compare;
		}
		throw new IllegalArgumentException(
				"Incompatible value types. Cannot compare values of types value1=" + v1.getClass().getName()
						+ " and value2=" + v2.getClass().getName());
	}

	/**
	 * Builder class for {@link InstancePropertyComparator}. The builder provides methods to fine tune the comparator
	 * behaviour like changing the comparator sort direction and language case sensitive ordering
	 *
	 * @author BBonev
	 */
	public static class Builder {
		private final String property;
		private boolean ascending = true;
		private Collator collator;

		private Builder(String property) {
			this.property = property;
		}

		/**
		 * Change the output direction to ascending: A -&gt; Z (default behaviour)
		 *
		 * @return the same builder for chaining
		 */
		public Builder ascending() {
			ascending = true;
			return this;
		}

		/**
		 * Change the output direction to descending: Z -&gt; A
		 *
		 * @return the same builder for chaining
		 */
		public Builder descending() {
			ascending = false;
			return this;
		}

		/**
		 * Configures the comparator to use default locale for natural language text comparison. This applies only when
		 * both properties are of type String.
		 *
		 * @return the same builder for chaining
		 */
		public Builder localeSensitive() {
			collator = Collator.getInstance();
			return this;
		}

		/**
		 * Configures the comparator to use the given locale for natural language text comparison. This applies only
		 * when both properties are of type String.
		 *
		 * @param locale the locale to use when performing string comparison
		 * @return the same builder for chaining
		 */
		public Builder localeSensitive(Locale locale) {
			collator = Collator.getInstance(locale);
			return this;
		}

		/**
		 * Configures the comparator to use the collator for natural language text comparison. This applies only
		 * when both properties are of type String.
		 *
		 * @param collator the custom collator to be used for string comparison
		 * @return the same builder for chaining
		 */
		public Builder localeSensitive(Collator collator) {
			this.collator = collator;
			return this;
		}

		/**
		 * Builds an instance using the specified configurations
		 *
		 * @return an InstancePropertyComparator using the specified configurations
		 */
		public InstancePropertyComparator build() {
			return new InstancePropertyComparator(property, ascending, collator);
		}

	}
}
