package com.sirma.itt.seip.definition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Triplet;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAO;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.exceptions.DictionaryException;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.sep.definition.db.DefinitionContent;
import com.sirma.sep.definition.db.DefinitionEntry;

/**
 * Base abstract class that implements common functions among top level definitions.
 *
 * @param <T>
 *            the generic type
 * @author BBonev
 */
public abstract class CommonDefinitionAccessor<T extends TopLevelDefinition> extends BaseDefinitionAccessor
implements DefinitionAccessor {

	private static final String TYPE = "type";
	private static final String IDENTIFIER = "identifier";
	private static final String REVISION = "revision";

	@Inject
	protected HashCalculator hashCalculator;

	/**
	 * Initialize the accessor cache.
	 */
	@PostConstruct
	public void initinializeCache() {
		if (!cacheContext.containsCache(getBaseCacheName()) || cacheContext.getCache(getBaseCacheName()) == null) {
			cacheContext.createCache(getBaseCacheName(), getBaseCacheCallback());
		}
		if (getMaxRevisionCacheName() != null && (!cacheContext.containsCache(getMaxRevisionCacheName())
				|| cacheContext.getCache(getMaxRevisionCacheName()) == null)) {
			cacheContext.createCache(getMaxRevisionCacheName(), getMaxRevisionCacheCallback());
		}
	}

	@Override
	public <E extends DefinitionModel> List<E> getAllDefinitions() {
		DataTypeDefinition typeDefinition = getDataTypeDefinition(getTargetDefinition());
		if (typeDefinition == null) {
			// no template definitions probably clean server/tenant initialization.
			return Collections.emptyList();
		}
		return getAllDefinitionsInternal(typeDefinition);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends DefinitionModel> E getDefinition(String defId) {
		if (defId == null) {
			return null;
		}
		// ensure max revision selection
		Pair<String, T> pair = getMaxRevisionCache().getByKey(defId);
		return (E) getCacheValue(pair);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends DefinitionModel> E getDefinition(String defId, Long version) {
		if (defId == null) {
			return null;
		}
		if (version == null) {
			// return the max version definition
			return getDefinition(defId);
		}
		EntityLookupCache<Pair<String, Long>, T, Serializable> lookupCache = getDefinitionCache();
		Pair<Pair<String, Long>, T> pair = lookupCache.getByKey(new Pair<>(defId, version));
		return (E) getCacheValue(pair);
	}

	@Override
	public int computeHash(DefinitionModel model) {
		return hashCalculator.computeHash(model).intValue();
	}

	@Override
	public Collection<DeletedDefinitionInfo> removeDefinition(String definition, long version,
			DefinitionDeleteMode mode) {
		if (StringUtils.isBlank(definition) || mode == null) {
			return Collections.emptyList();
		}
		Collection<DeletedDefinitionInfo> deleted = new LinkedList<>();

		switch (mode) {
			case SINGLE_REVISION:
				deleteSingleRevision(definition, version, deleted);
				break;
			case LAST_REVISION:
				deleteLastRevision(definition, deleted);
				break;
			case OLD_REVISIONS:
				deleteOldRevisions(definition, deleted);
				break;
			case ALL:
				deleteAllDefinitions(definition, deleted);
				break;
			default:
				throw new UnsupportedOperationException("Cannot handle delete mode " + mode);
		}

		return deleted;
	}

	private void deleteAllDefinitions(String definition, Collection<DeletedDefinitionInfo> deleted) {
		DataTypeDefinition type = getDataTypeDefinition(getTargetDefinition());

		List<Pair<String, Object>> args = new ArrayList<>(4);
		args.add(new Pair<String, Object>(IDENTIFIER, definition));
		args.add(new Pair<String, Object>(REVISION, -1L));
		args.add(new Pair<String, Object>(TYPE, type.getId()));

		List<Long> revisions = getDbDao().fetchWithNamed(DefinitionEntry.QUERY_DEFINITION_BY_ID_EXCLUDE_REVISION_KEY,
				args);

		for (Long revision : revisions) {
			deleteSingleRevision(definition, revision, deleted);
		}
	}

	private void deleteOldRevisions(String definition, Collection<DeletedDefinitionInfo> deleted) {
		DefinitionModel model = getDefinition(definition);
		if (model == null || model.getRevision().longValue() > 0L) {
			return;
		}
		DataTypeDefinition type = getDataTypeDefinition(getTargetDefinition());

		List<Pair<String, Object>> args = new ArrayList<>(4);
		args.add(new Pair<String, Object>(IDENTIFIER, definition));
		args.add(new Pair<String, Object>(REVISION, model.getRevision()));
		args.add(new Pair<String, Object>(TYPE, type.getId()));

		List<Long> revisions = getDbDao().fetchWithNamed(DefinitionEntry.QUERY_DEFINITION_BY_ID_EXCLUDE_REVISION_KEY,
				args);

		for (Long revision : revisions) {
			deleteSingleRevision(definition, revision, deleted);
		}
	}

	private void deleteLastRevision(String definition, Collection<DeletedDefinitionInfo> deleted) {
		DefinitionModel model = getDefinition(definition);
		if (model != null) {
			deleteSingleRevision(model.getIdentifier(), model.getRevision(), deleted);
		}
	}

	private void deleteSingleRevision(String definition, Long version, Collection<DeletedDefinitionInfo> deleted) {
		if (definition == null) {
			return;
		}

		EntityLookupCache<Pair<String, Long>, T, Serializable> definitionCache = getDefinitionCache();
		int count = definitionCache.deleteByKey(new Pair<>(definition, version));
		if (count > 0) {

			EntityLookupCache<String, T, Serializable> maxRevisionCache = getMaxRevisionCache();
			T value = maxRevisionCache.getValue(definition);
			if (value != null && value.getRevision().longValue() == version.longValue()) {
				maxRevisionCache.removeByKey(definition);
			}
			deleted.add(new DeletedDefinitionInfo(getTargetDefinition(), definition, version));
		}
	}

	@Override
	protected DataTypeDefinition detectDataTypeDefinition(DefinitionModel topLevelDefinition) {
		if (getTargetDefinition().isInstance(topLevelDefinition)) {
			return getDataTypeDefinition(getTargetDefinition());
		}
		throw new EmfRuntimeException("Not supported object instance: " + topLevelDefinition.getClass());
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void updateCache(DefinitionModel definition, boolean propertiesOnly, boolean isMaxRevision) {
		// check the definition type
		if (!getTargetDefinition().isInstance(definition)) {
			return;
		}
		T targetDefinition = (T) definition;
		// if we want to update only the definition properties
		if (!propertiesOnly) {
			if (isMaxRevision) {
				// overrides the current max revision of the case
				getMaxRevisionCache().setValue(targetDefinition.getIdentifier(), targetDefinition);
			}
			Triplet<String, Long, String> key = new Triplet<>(targetDefinition.getIdentifier(),
					targetDefinition.getRevision(), targetDefinition.getContainer());
			getDefinitionCache().setValue(key, targetDefinition);
		}

		// populate the label provider in all definitions
		injectLabelProvider(targetDefinition);
	}

	/**
	 * Gets the max revision cache.
	 *
	 * @return the caseDefinitionCache
	 */
	protected EntityLookupCache<String, T, Serializable> getMaxRevisionCache() {
		return cacheContext.getCache(getMaxRevisionCacheName());
	}

	/**
	 * Gets the definition cache.
	 *
	 * @return the caseDefinitionCache
	 */
	protected EntityLookupCache<Pair<String, Long>, T, Serializable> getDefinitionCache() {
		return cacheContext.getCache(getBaseCacheName());
	}

	/**
	 * Gets the target definition.
	 *
	 * @return the target definition
	 */
	protected abstract Class<T> getTargetDefinition();

	/**
	 * Gets the base cache name.
	 *
	 * @return the base cache name
	 */
	protected abstract String getBaseCacheName();

	/**
	 * Gets the max revision cache name.
	 *
	 * @return the max revision cache name
	 */
	protected abstract String getMaxRevisionCacheName();

	/**
	 * Gets the base cache callback.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the generic type
	 * @return the base cache callback
	 */
	@SuppressWarnings("unchecked")
	protected <K extends Serializable, V extends Serializable> EntityLookupCallbackDAO<K, T, V> getBaseCacheCallback() {
		return (EntityLookupCallbackDAO<K, T, V>) new DefinitionLookup();
	}

	/**
	 * Gets the max revision cache callback.
	 *
	 * @param <K>
	 *            the key type
	 * @param <V>
	 *            the generic type
	 * @return the max revision cache callback
	 */
	@SuppressWarnings("unchecked")
	protected <K extends Serializable, V extends Serializable> EntityLookupCallbackDAO<K, T, V> getMaxRevisionCacheCallback() {
		return (EntityLookupCallbackDAO<K, T, V>) new DefinitionMaxRevisionLookup();
	}

	/**
	 * Default lookup DAO class for fetching a definition by definition id, revision and container
	 *
	 * @author BBonev
	 */
	protected class DefinitionLookup extends EntityLookupCallbackDAOAdaptor<Pair<String, Long>, T, Serializable> {

		@Override
		public Pair<Pair<String, Long>, T> findByKey(Pair<String, Long> key) {
			DataTypeDefinition type = getDataTypeDefinition(getTargetDefinition());

			List<T> resultList = getDefinitionsInternal(DefinitionEntry.QUERY_DEFINITION_BY_ID_REVISION_KEY,
					key.getFirst(), key.getSecond(), type, null, false);
			if (resultList.isEmpty()) {
				return null;
			}
			if (resultList.size() > 1) {
				// this should not happen or someone broke the DB on purpose
				throw new DictionaryException("More then one case definition and revision found for " + key);
			}
			T result = resultList.get(0);
			// load properties into the cache
			updateCache(result, true, false);
			return new Pair<>(key, result);
		}

		@Override
		public Pair<Pair<String, Long>, T> createValue(T value) {
			throw new UnsupportedOperationException(getTargetDefinition().getName() + " has external creation");
		}

		@Override
		public int deleteByKey(Pair<String, Long> key) {
			DataTypeDefinition type = getDataTypeDefinition(getTargetDefinition());

			// also delete corresponding definition content
			List<Pair<String, Object>> args = new ArrayList<>(1);
			args.add(new Pair<String, Object>("definitionId", key.getFirst()));
			getDbDao().executeUpdate(DefinitionContent.DELETE_DEFINITION_CONTENT_BY_DEFINITION_ID_REVISION_KEY, args);

			args = new ArrayList<>(4);
			args.add(new Pair<String, Object>(IDENTIFIER, key.getFirst()));
			args.add(new Pair<String, Object>(REVISION, key.getSecond()));
			args.add(new Pair<String, Object>(TYPE, type.getId()));

			return getDbDao().executeUpdate(DefinitionEntry.DELETE_DEFINITION_BY_ID_REVISION_KEY, args);
		}

	}

	/**
	 * Default lookup DAO class for fetching a definition by definition id, container and his maximum revision.
	 *
	 * @author BBonev
	 */
	protected class DefinitionMaxRevisionLookup extends EntityLookupCallbackDAOAdaptor<String, T, Serializable> {

		@Override
		public Pair<String, T> findByKey(String key) {
			DataTypeDefinition type = getDataTypeDefinition(getTargetDefinition());
			if (type == null) {
				throw new EmfConfigurationException("No type definition found for: " + getTargetDefinition());
			}
			List<T> resultList = getDefinitionsInternal(DefinitionEntry.QUERY_MAX_DEFINITION_BY_ID_AND_TYPE_KEY, key, null, type,
					null, false);
			if (resultList.isEmpty()) {
				return null;
			}
			if (resultList.size() > 1) {
				// this should not happen or someone broke the DB on purpose
				throw new DictionaryException(
						"More then one " + getTargetDefinition().getName() + " with max revision found for " + key);
			}
			T result = resultList.get(0);

			// load properties into the cache
			updateCache(result, true, true);
			return new Pair<>(key, result);
		}

		@Override
		public Pair<String, T> createValue(T value) {
			throw new UnsupportedOperationException(getTargetDefinition().getName() + " has external creation");
		}

	}
}
