package com.sirma.itt.seip.domain.definition.label;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import com.sirma.itt.seip.Resettable;

/**
 * Resolver for statically or dynamically defined labels. A particular resolver instance should resolve labels for a
 * single language.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 30/10/2018
 */
public interface LabelResolver {

	/**
	 * Check if the current resolver has label for the given identifier
	 *
	 * @param labelId the label id to check
	 * @return {@code true} if the current resolver has label with the given id.
	 */
	boolean containsLabel(String labelId);

	/**
	 * Fetches a label with the given identifier or null is such label is undefined
	 *
	 * @param labelId the label id to resolve
	 * @return the resolved label or null
	 */
	String getLabel(String labelId);

	/**
	 * Creates a static {@link LabelResolver} that wraps a {@link ResourceBundle}
	 *
	 * @param resourceBundle the resource bundle to wrap
	 * @return a label resolver instance that uses the provided bundle to serve labels
	 */
	static LabelResolver wrap(ResourceBundle resourceBundle) {
		return new ResourceBundleToLabelProviderAdapter(resourceBundle);
	}

	/**
	 * Wraps a lazy initialized supplier for a map that will be used as source to power the return label resolver.
	 *
	 * @param labels the supplier for the data to use to wrap in the return resolver
	 * @return a label resolver using a lazy resolved map
	 */
	static LabelResolver wrap(Supplier<Map<String, String>> labels) {
		return new LazyMapToLabelProviderAdapter(labels);
	}

	/**
	 * Wraps a lazy initialized supplier for a map that will be used as source to power the return label resolver.
	 *
	 * @param labels the supplier for the data to use to wrap in the return resolver
	 * @return a label resolver using a lazy resolved map
	 */
	static LabelResolver wrap(Map<String, String> labels) {
		return new MapToLabelProviderAdapter(labels);
	}

	/**
	 * Label resolver implementation that wraps {@link ResourceBundle}
	 *
	 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
	 * @since 30/10/2018
	 */
	class ResourceBundleToLabelProviderAdapter implements LabelResolver {

		private final ResourceBundle resourceBundle;

		ResourceBundleToLabelProviderAdapter(ResourceBundle resourceBundle) {this.resourceBundle = resourceBundle;}

		@Override
		public boolean containsLabel(String labelId) {
			return resourceBundle.containsKey(labelId);
		}

		@Override
		public String getLabel(String labelId) {
			return resourceBundle.getString(labelId);
		}
	}

	/**
	 * Label resolver implementation that wraps lazy resolved {@link Map}
	 *
	 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
	 * @since 30/10/2018
	 */
	class LazyMapToLabelProviderAdapter implements LabelResolver, Resettable {

		private final Supplier<Map<String, String>> labels;

		LazyMapToLabelProviderAdapter(Supplier<Map<String, String>> labels) {this.labels = labels;}

		@Override
		public boolean containsLabel(String labelId) {
			return labels.get().containsKey(labelId);
		}

		@Override
		public String getLabel(String labelId) {
			return labels.get().get(labelId);
		}

		@Override
		public void reset() {
			Resettable.reset(labels);
		}
	}

	/**
	 * Label resolver implementation that wraps {@link Map}
	 *
	 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
	 * @since 30/10/2018
	 */
	class MapToLabelProviderAdapter implements LabelResolver {

		private final Map<String, String> labels;

		MapToLabelProviderAdapter(Map<String, String> labels) {this.labels = labels;}

		@Override
		public boolean containsLabel(String labelId) {
			return labels.containsKey(labelId);
		}

		@Override
		public String getLabel(String labelId) {
			return labels.get(labelId);
		}
	}
}
