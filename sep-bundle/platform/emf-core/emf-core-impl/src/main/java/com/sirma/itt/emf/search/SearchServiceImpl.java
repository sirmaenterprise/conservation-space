package com.sirma.itt.emf.search;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.plugin.PluginUtil;
import com.sirma.itt.emf.rendition.RenditionService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.search.model.SearchFilterConfig;
import com.sirma.itt.emf.security.Secure;

/**
 * Default implementation for the search service.
 *
 * @author BBonev
 */
@ApplicationScoped
public class SearchServiceImpl implements SearchService, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 7435785571309548636L;

	/** The extension. */
	@Inject
	@ExtensionPoint(SearchServiceFilterExtension.TARGET_NAME)
	private Iterable<SearchServiceFilterExtension> extension;

	/** The engines. */
	@Inject
	@ExtensionPoint(SearchEngine.TARGET_NAME)
	private Iterable<SearchEngine> engines;

	/** The mapping. */
	private Map<Class<?>, SearchServiceFilterExtension> mapping;

	/** The rendition service. */
	@Inject
	private javax.enterprise.inject.Instance<RenditionService> renditionService;

	/**
	 * Initialize mappings.
	 */
	@PostConstruct
	public void initializeMappings() {
		mapping = PluginUtil.parseSupportedObjects(extension, true);
	}

	@Override
	public <E, S extends SearchArguments<E>> S getFilter(String filterName, Class<E> resultType,
			Context<String, Object> context) {
		return getExtension(resultType).buildSearchArguments(filterName, context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends SearchArguments<?>> S buildSearchArguments(SearchFilter filter,
			Class<?> resultType, Context<String, Object> context) {
		return getExtension(resultType).buildSearchArguments(filter, context);
	}

	@Override
	public <E> SearchFilterConfig getFilterConfiguration(String placeHolder, Class<E> resultType) {
		return getExtension(resultType).getFilterConfiguration(placeHolder);
	}

	@Secure
	@Override
	public <E extends Instance, S extends SearchArguments<E>> void search(Class<?> target,
			S arguments) {
		for (SearchEngine engine : engines) {
			if (engine.isSupported(target, arguments)) {
				engine.search(target, arguments);
				loadResultThumbnails(arguments.getResult());
				return;
			}
		}
		// REVIEW or throw an exception
	}

	/**
	 * Load thumbnails for the given result.
	 * 
	 * @param <E>
	 *            the element type
	 * @param result
	 *            the arguments
	 */
	private <E extends Instance> void loadResultThumbnails(Collection<E> result) {
		if ((result != null) && !result.isEmpty() && !renditionService.isUnsatisfied()) {
			renditionService.get().loadThumbnails(result);
		}
	}

	/**
	 * Gets the extension.
	 *
	 * @param target
	 *            the target
	 * @return the extension
	 */
	protected SearchServiceFilterExtension getExtension(Class<?> target) {
		SearchServiceFilterExtension serviceExtension = mapping.get(target);
		if (serviceExtension == null) {
			throw new EmfConfigurationException(
					"Could not found a search filter extension that can support class of type "
							+ target);
		}
		return serviceExtension;
	}

}
