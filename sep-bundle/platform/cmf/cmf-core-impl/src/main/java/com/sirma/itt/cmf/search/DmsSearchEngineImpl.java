package com.sirma.itt.cmf.search;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.plugin.PluginUtil;
import com.sirma.itt.emf.search.SearchDialects;
import com.sirma.itt.emf.search.SearchEngine;
import com.sirma.itt.emf.search.model.SearchArguments;

/**
 * Search engine implementation using DMS adapters to perform the actual search.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = SearchEngine.TARGET_NAME, order = 10)
public class DmsSearchEngineImpl implements DmsSearchEngine {

	/** The extensions. */
	@Inject
	@ExtensionPoint(DmsSearchEngineExtension.TARGET_NAME)
	private Iterable<DmsSearchEngineExtension> extensions;

	/** The mappings. */
	private Map<Class<?>, DmsSearchEngineExtension> mappings;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		mappings = PluginUtil.parseSupportedObjects(extensions, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends Instance, S extends SearchArguments<E>> boolean isSupported(Class<?> target,
			S arguments) {
		// this should be more complex
		return SearchDialects.DMS_SOLR.equals(arguments.getDialect())
				|| mappings.containsKey(target);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends Instance, S extends SearchArguments<E>> void search(Class<?> target,
			S arguments) {
		DmsSearchEngineExtension extension = mappings.get(target);
		if (extension == null) {
			// TODO: probably throw an exception
			return;
		}
		extension.search(arguments);
	}

}
