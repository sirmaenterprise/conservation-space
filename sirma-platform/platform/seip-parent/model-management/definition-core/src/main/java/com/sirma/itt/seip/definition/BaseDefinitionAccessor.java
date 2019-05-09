package com.sirma.itt.seip.definition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.GenericProxy;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefinitionAccessor.DefinitionDeleteMode;
import com.sirma.itt.seip.domain.BidirectionalMapping;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.Displayable;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.sep.definition.db.DefinitionEntry;

/**
 * Abstract class to provide base methods for implementing concrete
 * {@link com.sirma.itt.seip.definition.DefinitionAccessor}.
 *
 * @author BBonev
 */
public abstract class BaseDefinitionAccessor {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "exclude.definitions", type = Set.class, label = "A list of definitions to be excluded from the list of allowed definitions for creation. This is valid for all definitions no matter the type. If more then one value they should be separeted by comma. Ex: def1, def2, ...")
	protected ConfigurationProperty<Set<String>> excludeDefinitions;

	@Inject
	private DbDao dbDao;
	@Inject
	protected LabelProvider labelProvider;
	@Inject
	protected EntityLookupCacheContext cacheContext;
	@Inject
	private SecurityContext securityContext;

	@Inject
	protected DefinitionService definitionServiceInstance;

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
	 * @param mode
	 *            delete mode
	 * @return true, if successful
	 */
	public Collection<DeletedDefinitionInfo> removeDefinition(String definition, long version,
			DefinitionDeleteMode mode) {
		return Collections.emptyList();
	}

	/**
	 * Gets the definition from cache.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the value type
	 * @param <S>
	 *            the generic type
	 * @param definitionId
	 *            the definition id
	 * @param cache
	 *            the cache
	 * @return the definition from cache
	 */
	protected <K extends Serializable, V, S extends Serializable> V getDefinitionFromCache(K definitionId,
			EntityLookupCache<K, V, S> cache) {
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
	protected DefinitionEntry createEntity(TopLevelDefinition topLevelDefinition, DefinitionAccessor accessor) {
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
		entry.setModifiedBy(getCurrentUser());
		entry.setModifiedOn(new Date());
		return entry;
	}

	private String getCurrentUser() {
		if (securityContext.isActive()) {
			return securityContext.getEffectiveAuthentication().getIdentityId();
		}
		return null;
	}

	/**
	 * Detect data type definition.
	 *
	 * @param topLevelDefinition
	 *            the top level definition
	 * @return the data type definition
	 */
	protected abstract DataTypeDefinition detectDataTypeDefinition(DefinitionModel topLevelDefinition);

	/**
	 * Gets the all case definitions internal.
	 *
	 * @param type
	 *            the type
	 * @param <D>
	 *            the generic type
	 * @return the all case definitions internal
	 */
	protected <D extends DefinitionModel> List<D> getAllDefinitionsInternal(DataTypeDefinition type) {
		return getDefinitionsInternal(DefinitionEntry.QUERY_MAX_REVISION_OF_DEFINITIONS_BY_FILTER_KEY, null, null, type,
				Boolean.FALSE, true, true, true);
	}

	/**
	 * Gets the definitions for the given query and parameters. The method converts the results to working definition
	 * objects and does not filter the results.
	 *
	 * @param <E>
	 *            the element type
	 * @param query
	 *            the query
	 * @param identifier
	 *            the identifier
	 * @param revision
	 *            the revision
	 * @param type
	 *            the type
	 * @param isAbstract
	 *            the is abstract
	 * @param updateCache
	 *            if cache update is needed with the returned results
	 * @return the definitions internal
	 */
	protected <E extends DefinitionModel> List<E> getDefinitionsInternal(String query, String identifier, Long revision,
			DataTypeDefinition type, Boolean isAbstract, boolean updateCache) {
		return getDefinitionsInternal(query, identifier, revision, type, isAbstract, updateCache, true, false);
	}

	/**
	 * Gets the definitions for the given query and parameters. The method could update the internal caches after
	 * executing the query. Could convert the results to definitions or return the raw {@link DefinitionEntry}. Could
	 * also filter the excluded definitions from the result.
	 *
	 * @param <E>
	 *            the element type
	 * @param query
	 *            the query
	 * @param identifier
	 *            the identifier
	 * @param revision
	 *            the revision
	 * @param type
	 *            the type
	 * @param isAbstract
	 *            the is abstract
	 * @param updateCache
	 *            if cache update is needed with the returned results
	 * @param convert
	 *            whether to convert or not the results. If <code>false</code> the raw definition object will be
	 *            returned in the form of the {@link DefinitionEntry} object.
	 * @param filter
	 *            to filter or not the excluded definitions.
	 * @return the fetched definitions
	 */
	@SuppressWarnings("unchecked")
	protected <E extends DefinitionModel> List<E> getDefinitionsInternal(String query, String identifier, Long revision,
			DataTypeDefinition type, Boolean isAbstract, boolean updateCache, boolean convert, boolean filter) {
		// if no container is specified we cannot list or create a instance
		if (StringUtils.isBlank(query)) {
			return CollectionUtils.emptyList();
		}

		List<Pair<String, Object>> args = new ArrayList<>(5);
		if (StringUtils.isNotBlank(identifier)) {
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
		if (list == null) {
			return CollectionUtils.emptyList();
		}
		Stream<TopLevelDefinition> filteredDefinitions = list.stream();
		if (filter) {
			filteredDefinitions = filterOutExcludedDefinitions(filteredDefinitions);
		}
		if (convert) {
			List<E> definitions = convertDefinitions(filteredDefinitions);
			if (updateCache) {
				// update the internal cache
				// BB: optimize somehow - well with the new implementation this step is required
				updateCache(definitions, revision == null);
			}
			return definitions;
		}
		return (List<E>) filteredDefinitions.collect(Collectors.toCollection(LinkedList::new));
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
	protected static <E> E getCacheValue(Pair<?, E> pair) {
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
	protected static <E extends DefinitionModel> List<E> convertDefinitions(Stream<TopLevelDefinition> list) {
		// convert the definition entry to concrete definition
		return (List<E>) list
				.filter(GenericProxy.class::isInstance)
				.map(GenericProxy.class::cast)
				.map(GenericProxy::getTarget)
				.filter(TopLevelDefinition.class::isInstance)
				.map(TopLevelDefinition.class::cast)
				.peek((TopLevelDefinition tld) -> {
					if (tld instanceof BidirectionalMapping) {
						((BidirectionalMapping) tld).initBidirection();
					}
					Sealable.seal(tld);
				})
				.collect(Collectors.toCollection(LinkedList::new));
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
	protected <E extends TopLevelDefinition> Stream<E> filterOutExcludedDefinitions(Stream<E> list) {
		Set<String> excluded = excludeDefinitions.computeIfNotSet(Collections::emptySet);
		return list.filter(d -> !d.isAbstract()).filter(d -> !excluded.contains(d.getIdentifier()));
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
	protected <E extends DefinitionModel> void updateCache(List<E> definitions, boolean isMaxRevision) {
		if (definitions == null || definitions.isEmpty()) {
			return;
		}

		for (E definition : definitions) {
			if (definition instanceof BidirectionalMapping) {
				((BidirectionalMapping) definition).initBidirection();
			}
			updateCache(definition, false, isMaxRevision);
		}
	}

	/**
	 * Inject label provider to the given object tree. The method supports the types: {@link Displayable},
	 * {@link DefinitionModel}, {@link RegionDefinitionModel}, {@link RegionDefinition}, {@link PropertyDefinition},
	 * {@link Transitional} and {@link TransitionDefinition}.
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
			displayable.setLabelProvider(labelProvider);
		}
		if (model instanceof DefinitionModel) {
			injectLabelProvider(((DefinitionModel) model).getFields());
		}
		if (model instanceof RegionDefinitionModel) {
			injectLabelProvider(((RegionDefinitionModel) model).getRegions());
		}
		if (model instanceof Transitional) {
			injectLabelProvider(((Transitional) model).getTransitions());
			injectLabelProvider(((Transitional) model).getTransitionGroups());
		}
		if (model instanceof TransitionDefinition) {
			injectLabelProvider(((TransitionDefinition) model).getFields());
		}
		// handle region model
		injectLabelProviderToRegionDefinition(model);
		return model;
	}

	/**
	 * Inject label provider to region definition.
	 *
	 * @param <E>
	 *            the element type
	 * @param model
	 *            the model
	 */
	private <E> void injectLabelProviderToRegionDefinition(E model) {
		if (model instanceof RegionDefinition) {
			RegionDefinition regionDefinition = (RegionDefinition) model;
			injectLabelProvider(regionDefinition.getFields());
			ControlDefinition controlDefinition = regionDefinition.getControlDefinition();
			if (controlDefinition != null) {
				injectLabelProvider(controlDefinition.getFields());
			}
		} else if (model instanceof PropertyDefinition) {
			// handle specific property
			ControlDefinition controlDefinition = ((PropertyDefinition) model).getControlDefinition();
			if (controlDefinition != null) {
				injectLabelProvider(controlDefinition.getFields());
			}
		}
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
	 * Gets the definition service.
	 *
	 * @return the definition service
	 */
	protected DefinitionService getDefinitionService() {
		return definitionServiceInstance;
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
		return getDefinitionService().getDataTypeDefinition(name);
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
	protected abstract void updateCache(DefinitionModel definition, boolean propertiesOnly, boolean isMaxRevision);

	/**
	 * Getter method for dbDao.
	 *
	 * @return the dbDao
	 */
	protected DbDao getDbDao() {
		return dbDao;
	}
}
