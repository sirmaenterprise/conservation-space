package com.sirma.itt.emf.definition.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.definition.DefinitionIdentityUtil;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.DefinitionEntry;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.Transitional;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.GenericProxy;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.label.Displayable;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Abstract class to provide base methods for implementing concrete
 * {@link com.sirma.itt.emf.definition.dao.DefinitionAccessor}.
 *
 * @author BBonev
 */
public abstract class BaseDefinitionAccessor implements Serializable {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4724316961774209798L;

	/** The Constant LOGGER. */
	protected static final Logger LOGGER = LoggerFactory.getLogger(BaseDefinitionAccessor.class);

	/** The debug. */
	protected final boolean debug = LOGGER.isDebugEnabled();

	/** The trace. */
	protected final boolean trace = LOGGER.isTraceEnabled();
	/** The authentication service instance. */
	@Inject
	protected javax.enterprise.inject.Instance<AuthenticationService> authenticationServiceInstance;

	/** The db dao. */
	@Inject
	private DbDao dbDao;
	/** The default container. */
	@Inject
	@Config(name = EmfConfigurationProperties.DEFAULT_CONTAINER)
	protected String defaultContainer;
	/** The exclude definitions. */
	@Inject
	@Config(name = EmfConfigurationProperties.EXCLUDE_DEFINITIONS)
	protected Set<String> excludeDefinitions;
	/** The label provider. */
	@Inject
	protected LabelProvider labelProvider;
	/** The cache context. */
	@Inject
	protected EntityLookupCacheContext cacheContext;
	/** The dictionary service instance. */
	@Inject
	protected Instance<DictionaryService> dictionaryServiceInstance;

	/**
	 * Save definition.
	 *
	 * @param <E>
	 *            the element type
	 * @param definition
	 *            the definition
	 * @param accessor
	 *            the accessor
	 * @return the e
	 */
	public <E extends TopLevelDefinition> E saveDefinition(E definition, DefinitionAccessor accessor) {
		DefinitionEntry entity = createEntity(definition, accessor);
		getDbDao().saveOrUpdate(entity);
		// updates the standard typed both caches
		updateCache(definition, false, true);
		return definition;
	}

	/**
	 * Removes the definition.
	 *
	 * @param definition
	 *            the definition
	 * @param version
	 *            the version
	 * @return true, if successful
	 */
	public boolean removeDefinition(String definition, long version) {
		return false;
	}

	/**
	 * Gets the definition from cache.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param <VK>
	 *            the generic type
	 * @param definitionId
	 *            the definition id
	 * @param cache
	 *            the cache
	 * @return the definition from cache
	 */
	protected <K extends Serializable, V, VK extends Serializable> V getDefinitionFromCache(
			K definitionId, EntityLookupCache<K, V, VK> cache) {
		Pair<K, V> pair = cache.getByKey(definitionId);
		// clone the result against modifications in the cache
		return getCacheValue(pair);
	}

	/**
	 * Creates a definition entry for the given top level definition.
	 *
	 * @param topLevelDefinition
	 *            the top level definition
	 * @param accessor
	 *            the accessor
	 * @return the definition entry
	 */
	protected DefinitionEntry createEntity(TopLevelDefinition topLevelDefinition,
			DefinitionAccessor accessor) {
		DefinitionEntry entry = new DefinitionEntry();
		entry.setAbstract(topLevelDefinition.isAbstract());
		entry.setContainer(topLevelDefinition.getContainer());
		entry.setDmsId(topLevelDefinition.getDmsId());
		entry.setHash(accessor.computeHash(topLevelDefinition));
		entry.setIdentifier(topLevelDefinition.getIdentifier());
		entry.setRevision(topLevelDefinition.getRevision());
		DataTypeDefinition targetType = detectDataTypeDefinition(topLevelDefinition);
		entry.setTargetType(targetType);
		entry.setTarget(topLevelDefinition);
		return entry;
	}

	/**
	 * Detect data type definition.
	 *
	 * @param topLevelDefinition
	 *            the top level definition
	 * @return the data type definition
	 */
	protected abstract DataTypeDefinition detectDataTypeDefinition(
			DefinitionModel topLevelDefinition);

	/**
	 * Gets the all case definitions internal.
	 *
	 * @param <D>
	 *            the generic type
	 * @param currentContainer
	 *            the current container
	 * @param type
	 *            the type
	 * @return the all case definitions internal
	 */
	protected <D extends DefinitionModel> List<D> getAllDefinitionsInternal(
			String currentContainer,
			DataTypeDefinition type) {
		// if no container is specified we cannot list or create a instance
		if (StringUtils.isNullOrEmpty(currentContainer)) {
			return CollectionUtils.emptyList();
		}
		return getDefinitionsInternal(EmfQueries.QUERY_ALL_DEFINITIONS_FILTERED_KEY, null,
				null, currentContainer, type, Boolean.FALSE, true, true, true);
	}

	/**
	 * Gets the definitions for the given query and parameters. The method converts the results to
	 * working definition objects and does not filter the results.
	 * 
	 * @param <E>
	 *            the element type
	 * @param query
	 *            the query
	 * @param identifier
	 *            the identifier
	 * @param revision
	 *            the revision
	 * @param container
	 *            the container
	 * @param type
	 *            the type
	 * @param isAbstract
	 *            the is abstract
	 * @param updateCache
	 *            if cache update is needed with the returned results
	 * @return the definitions internal
	 */
	protected <E extends DefinitionModel> List<E> getDefinitionsInternal(String query,
			String identifier, Long revision, String container, DataTypeDefinition type,
			Boolean isAbstract, boolean updateCache) {
		return getDefinitionsInternal(query, identifier, revision, container, type, isAbstract,
				updateCache, true, false);
	}

	/**
	 * Gets the definitions for the given query and parameters. The method could update the internal
	 * caches after executing the query. Could convert the results to definitions or return the raw
	 * {@link DefinitionEntry}. Could also filter the excluded definitions from the result.
	 * 
	 * @param <E>
	 *            the element type
	 * @param query
	 *            the query
	 * @param identifier
	 *            the identifier
	 * @param revision
	 *            the revision
	 * @param container
	 *            the container
	 * @param type
	 *            the type
	 * @param isAbstract
	 *            the is abstract
	 * @param updateCache
	 *            if cache update is needed with the returned results
	 * @param convert
	 *            whether to convert or not the results. If <code>false</code> the raw definition
	 *            object will be returned in the form of the {@link DefinitionEntry} object.
	 * @param filter
	 *            to filter or not the excluded definitions.
	 * @return the fetched definitions
	 */
	@SuppressWarnings("unchecked")
	protected <E extends DefinitionModel> List<E> getDefinitionsInternal(String query,
			String identifier, Long revision, String container, DataTypeDefinition type,
			Boolean isAbstract, boolean updateCache, boolean convert, boolean filter) {
		// if no container is specified we cannot list or create a instance
		if (StringUtils.isNullOrEmpty(query) || StringUtils.isNullOrEmpty(container)) {
			return CollectionUtils.emptyList();
		}
		TimeTracker tracker = new TimeTracker().begin().begin();

		List<Pair<String, Object>> args = new ArrayList<Pair<String, Object>>(5);
		args.add(new Pair<String, Object>("container", container));
		if (StringUtils.isNotNullOrEmpty(identifier)) {
			args.add(new Pair<String, Object>("identifier", identifier));
		}
		if (revision != null) {
			args.add(new Pair<String, Object>("revision", revision));
		}
		if (type != null) {
			args.add(new Pair<String, Object>("type", type.getId()));
		}
		if (isAbstract != null) {
			args.add(new Pair<String, Object>("Abstract", isAbstract));
		}
		List<TopLevelDefinition> list = getDbDao().fetchWithNamed(query, args);
		if (debug) {
			String typeName = type != null ? type.getTitle() : "all";
			LOGGER.debug("Db search for {} definitions took {} s", typeName,
					tracker.stopInSeconds());
			tracker.begin();
		}
		if (list == null) {
			return CollectionUtils.emptyList();
		}
		List<TopLevelDefinition> filteredDefinitions = list;
		if (filter) {
			filteredDefinitions = filterOutExcludedDefinitions(list);
		}
		if (convert) {
			List<E> definitions = convertDefinitions(filteredDefinitions);
			if (debug) {
				LOGGER.debug("Definitions filterring took {} s", tracker.stopInSeconds());
			}
			if (updateCache) {
				// update the internal cache
				// BB: optimize somehow - well with the new implementation this step is required
				updateCache(definitions, revision == null);
			}

			if (debug) {
				String typeName = type != null ? type.getTitle() : "all";
				LOGGER.debug("Fetched {} {} definitions in {} s", filteredDefinitions.size(),
						typeName, tracker.stopInSeconds());
			}
			return definitions;
		}
		return (List<E>) filteredDefinitions;
	}

	/**
	 * Gets the definition pair based on the given definition. If the definition id contains a
	 * container id then it's extracted from if and separated. if not present and there is a
	 * container id from the current
	 *
	 * @param container
	 *            the container
	 * @param defId
	 *            the def id
	 * @return the definition pair {@link AuthenticationService} that id is returned
	 */
	protected Pair<String, String> getDefinitionPair(String container, String defId) {
		Pair<String, String> parsed = DefinitionIdentityUtil.parseDefinitionId(defId);
		if (parsed == null) {
			return null;
		}
		// if the container was passed via the parameter we assume it's correct one
		if (parsed.getSecond() != null) {
			return parsed;
		}
		String currentContainer = container;
		if (StringUtils.isNullOrEmpty(currentContainer)) {
			// check if they differ
			currentContainer = getCurrentContainer();
		}
		if (!EqualsHelper.nullSafeEquals(parsed.getSecond(), currentContainer, true)) {
			// if container not passed via definition Id then get it from
			// service
			String string = MergeHelper.replaceIfNull(parsed.getSecond(), currentContainer);
			parsed.setSecond(string);
		}
		return parsed;
	}

	/**
	 * Gets the cache value.
	 *
	 * @param <E>
	 *            the element type
	 * @param pair
	 *            the pair
	 * @return the cache value
	 */
	protected <E> E getCacheValue(Pair<?, E> pair) {
		if (pair == null) {
			return null;
		}
		return pair.getSecond();
	}

	/**
	 * Convert definitions.
	 *
	 * @param <E>
	 *            the element type
	 * @param list
	 *            the list
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	protected <E extends DefinitionModel> List<E> convertDefinitions(List<TopLevelDefinition> list) {
		List<E> definitions = new ArrayList<E>(list.size());
		// convert the definition entry to concrete definition
		for (TopLevelDefinition topLevelDefinition : list) {
			if (topLevelDefinition instanceof GenericProxy) {
				Object target = ((GenericProxy<?>) topLevelDefinition).getTarget();
				if (target instanceof TopLevelDefinition) {
					TopLevelDefinition definition = (TopLevelDefinition) target;
					if (definition instanceof BidirectionalMapping) {
						((BidirectionalMapping) definition).initBidirection();
					}

					definitions.add((E) definition);
				}
			}
		}
		return definitions;
	}

	/**
	 * Gets the current container.
	 *
	 * @return the current container
	 */
	protected String getCurrentContainer() {
		return SecurityContextManager.getCurrentContainer(authenticationServiceInstance);
	}

	/**
	 * Filter out excluded definitions.
	 *
	 * @param <E>
	 *            the element type
	 * @param list
	 *            the list
	 * @return the list
	 */
	protected <E extends TopLevelDefinition> List<E> filterOutExcludedDefinitions(List<E> list) {
		List<E> result = new LinkedList<E>();
		String container = getCurrentContainer();
		for (E definition : list) {
			if (!excludeDefinitions.contains(definition.getIdentifier())
					&& !definition.isAbstract()
					// check the container affinity
					&& (StringUtils.isNullOrEmpty(definition.getContainer()) || EqualsHelper
							.nullSafeEquals(container, definition.getContainer(), true))) {
				result.add(definition);
			}
		}
		return result;
	}

	/**
	 * Update cache.
	 *
	 * @param <E>
	 *            the element type
	 * @param definitions
	 *            the definitions
	 * @param isMaxRevision
	 *            the is max revision
	 */
	protected <E extends DefinitionModel> void updateCache(List<E> definitions,
			boolean isMaxRevision) {
		TimeTracker tracker = new TimeTracker().begin();
		if ((definitions == null) || definitions.isEmpty()) {
			return;
		}

		StringBuilder traceMsg = null;
		String defName = null;
		if (debug) {
			defName = definitions.get(0).getClass().getSimpleName();
			if (trace) {
				traceMsg = new StringBuilder("Refreshing " + defName + " definitions cache:\n");
			}
		}
		for (E definition : definitions) {
			if (trace) {
				tracker.begin();
			}
			if (definition instanceof BidirectionalMapping) {
				((BidirectionalMapping) definition).initBidirection();
			}
			updateCache(definition, false, isMaxRevision);
			if (trace) {
				traceMsg.append(definition.getIdentifier()).append(" refresh took ")
				.append(tracker.stopInSeconds()).append(" s\n");
			}
		}
		if (debug) {
			if (trace) {
				LOGGER.trace(traceMsg.toString());
			}
			LOGGER.debug("Cache refresh for {} {}s definitions completed in {} s",
					definitions.size(), defName, tracker.stopInSeconds());
		}
	}

	/**
	 * Inject label provider to the given object tree. The method supports the types:
	 * {@link Displayable}, {@link DefinitionModel}, {@link RegionDefinitionModel},
	 * {@link RegionDefinition}, {@link PropertyDefinition}, {@link Transitional} and
	 * {@link TransitionDefinition}.
	 * 
	 * @param <E>
	 *            the element type
	 * @param model
	 *            the definition tree to inject the label provider.
	 * @return the updated instance tree
	 */
	protected <E> E injectLabelProvider(E model) {
		if (model instanceof Displayable) {
			Displayable displayable = (Displayable) model;
			if ((displayable.getLabelId() != null) || (displayable.getTooltipId() != null)) {
				displayable.setLabelProvider(labelProvider);
			}
		}
		if (model instanceof DefinitionModel) {
			injectLabelProvider(((DefinitionModel) model).getFields());
		}
		if (model instanceof RegionDefinitionModel) {
			injectLabelProvider(((RegionDefinitionModel) model).getRegions());
		}
		if (model instanceof Transitional) {
			injectLabelProvider(((Transitional) model).getTransitions());
		}
		if (model instanceof TransitionDefinition) {
			injectLabelProvider(((TransitionDefinition) model).getFields());
		}
		// handle region model
		if (model instanceof RegionDefinition) {
			RegionDefinition regionDefinition = (RegionDefinition) model;
			injectLabelProvider(regionDefinition.getFields());
			ControlDefinition controlDefinition = regionDefinition.getControlDefinition();
			if (controlDefinition != null) {
				injectLabelProvider(controlDefinition.getFields());
			}
		} else
			// handle specific property
			if (model instanceof PropertyDefinition) {
				ControlDefinition controlDefinition = ((PropertyDefinition) model)
						.getControlDefinition();
				if (controlDefinition != null) {
					injectLabelProvider(controlDefinition.getFields());
				}
			}
		return model;
	}

	/**
	 * Inject label provider to the given list of objects. Each object is handled by the method
	 * {@link #injectLabelProvider(Object)}
	 *
	 * @param <E>
	 *            the element type
	 * @param list
	 *            the list
	 */
	protected <E> void injectLabelProvider(List<E> list) {
		for (E e : list) {
			injectLabelProvider(e);
		}
	}

	/**
	 * Gets the dictionary service.
	 *
	 * @return the dictionary service
	 */
	protected DictionaryService getDictionaryService() {
		return dictionaryServiceInstance.get();
	}

	/**
	 * Gets the data type definition for the given class.
	 *
	 * @param clazz
	 *            the class to get the data type definition
	 * @return the data type definition
	 */
	protected DataTypeDefinition getDataTypeDefinition(Class<?> clazz) {
		if (clazz == null) {
			throw new EmfRuntimeException("Cannot fetch type for null class");
		}
		String name = clazz.getName();
		return getDictionaryService().getDataTypeDefinition(name);
	}

	/**
	 * Update the internal cache and update the property custom injections.
	 *
	 * @param definition
	 *            the definition
	 * @param propertiesOnly
	 *            the properties only
	 * @param isMaxRevision
	 *            the is max revision
	 */
	protected abstract void updateCache(DefinitionModel definition, boolean propertiesOnly,
			boolean isMaxRevision);

	/**
	 * Getter method for dbDao.
	 *
	 * @return the dbDao
	 */
	protected DbDao getDbDao() {
		return dbDao;
	}
}
