package com.sirma.itt.seip.definition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.GenericProxy;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Quad;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.model.BaseDefinition;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.PrototypeDefinitionImpl;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.PrototypeDefinition;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.sep.definition.db.DefinitionEntry;

/**
 * ~ Default service implementation for working with definitions.
 *
 * @author BBonev
 */
@ApplicationScoped
public class MutableDefinitionServiceImpl extends BaseDefinitionService implements MutableDefinitionService {

	private static final long serialVersionUID = 8755426012305496783L;

	private static final String BASE_DEFINITION_SELECTOR = "$DEFAULT_DEFINITION$";

	private static final Logger LOGGER = LoggerFactory.getLogger(MutableDefinitionServiceImpl.class);

	private static final String NO_CONTAINER = "$NO_CONTAINER$";

	@Inject
	private HashCalculator hashCalculator;

	@Inject
	private DefinitionService definitionService;

	@Override
	@SuppressWarnings("unchecked")
	public PropertyDefinition savePropertyIfChanged(PropertyDefinition newProperty, PropertyDefinition oldProperty) {
		// if we have old property we compare the two of them and copy the id if needed
		if (oldProperty != null && oldProperty.getPrototypeId() != null) {
			WritablePropertyDefinition newProp = (WritablePropertyDefinition) newProperty;
			WritablePropertyDefinition oldProp = (WritablePropertyDefinition) oldProperty;
			if (newProperty instanceof GenericProxy) {
				newProp = ((GenericProxy<WritablePropertyDefinition>) newProperty).getTarget();
			}
			if (oldProperty instanceof GenericProxy) {
				oldProp = ((GenericProxy<WritablePropertyDefinition>) oldProperty).getTarget();
			}

			if (hashCalculator.computeHash(newProp).equals(hashCalculator.computeHash(oldProp))) {
				newProp.setPrototypeId(oldProp.getPrototypeId());
				// could call refresh..
				return newProperty;
			}
		}
		if (newProperty instanceof GenericProxy) {
			// fill default container
			if (StringUtils.isBlank(newProperty.getContainer())) {
				newProperty.setContainer(NO_CONTAINER);
			}
			Object target = ((GenericProxy<?>) newProperty).getTarget();
			if (target instanceof WritablePropertyDefinition) {
				WritablePropertyDefinition definition = (WritablePropertyDefinition) target;
				// if the field does not have computed hash then we will compute it and set it
				if (definition.getHash() == null) {
					definition.setHash(hashCalculator.computeHash(definition));
				}
				// fill prototype Id
				createOrUpdatePrototypeDefinition(definition);
			}
		}
		// if not a proxy then save it
		if (!(newProperty instanceof PropertyDefinitionProxy) && newProperty instanceof WritablePropertyDefinition
				&& newProperty.getPrototypeId() == null) {
			createOrUpdatePrototypeDefinition((WritablePropertyDefinition) newProperty);
			return newProperty;
		}
		return newProperty;
	}

	private void createOrUpdatePrototypeDefinition(WritablePropertyDefinition propertyDefinition) {
		if (propertyDefinition.getPrototypeId() != null) {
			return;
		}
		EntityLookupCache<Long, PrototypeDefinition, Quad<String, String, Boolean, Long>> prototypeCache = getPrototypeCache();

		PrototypeDefinition prototype = new PrototypeDefinitionImpl();
		prototype.setIdentifier(propertyDefinition.getName());
		prototype.setContainer(propertyDefinition.getContainer());
		prototype.setDataType(propertyDefinition.getDataType());
		prototype.setMultiValued(propertyDefinition.isMultiValued());
		Pair<Long, PrototypeDefinition> pair = prototypeCache.getOrCreateByValue(prototype);
		if (pair != null) {
			propertyDefinition.setPrototypeId(pair.getFirst());
		} else {
			LOGGER.error("Failed to create prototype definition.");
		}
	}

	/**
	 * Initialize base property definitions.
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void initializeBasePropertyDefinitions() {
		EntityLookupCache<Class, List<DefinitionModel>, Serializable> maxRevisionsCache = getMaxRevisionsCache();
		Class<BaseDefinition> key = BaseDefinition.class;
		Pair<Class, List<DefinitionModel>> pair = maxRevisionsCache.getByKey(key);
		// definition exists we a done
		if (pair != null && getCacheValue(pair) != null && !getCacheValue(pair).isEmpty()) {
			LOGGER.trace("Base definition was initialized before. Nothing to do.");
			return;
		}
		// create new empty definition
		BaseDefinition<BaseDefinition<?>> definition = new BaseDefinition<>();
		definition.setIdentifier(BASE_DEFINITION_SELECTOR);

		DefinitionEntry entry = new DefinitionEntry();
		entry.setContainer(NO_CONTAINER);
		entry.setAbstract(Boolean.FALSE);
		entry.setDmsId(null);
		entry.setHash(-1);
		entry.setRevision(0L);
		entry.setIdentifier(BASE_DEFINITION_SELECTOR);
		entry.setTargetType(definitionService.getDataTypeDefinition(BaseDefinition.class.getName()));
		entry.setTarget(definition);

		DefinitionEntry baseDefinition = dbDao.saveOrUpdate(entry);

		maxRevisionsCache.setValue(key, new ArrayList<DefinitionModel>(Collections.singletonList(baseDefinition)));
	}

	@Override
	public DataTypeDefinition saveDataTypeDefinition(DataTypeDefinition typeDefinition) {
		EntityLookupCache<String, DataTypeDefinition, String> typeCache = getTypeDefinitionCache();
		// clear the cache entry to force fetching of new entry !!!
		typeCache.removeByKey(typeDefinition.getName());
		typeCache.removeByValue(typeDefinition);

		Pair<String, DataTypeDefinition> byKey = typeCache.getByKey(typeDefinition.getName());
		DataTypeDefinition updated;
		if (byKey == null) {
			updated = dbDao.saveOrUpdate(typeDefinition);
		} else {
			DataTypeDefinition definition = byKey.getSecond();
			typeDefinition.setId(definition.getId());
			updated = dbDao.saveOrUpdate(typeDefinition);

		}
		typeCache.setValue(updated.getName(), updated);
		// update the second cache
		for (String string : updated.getUries()) {
			getTypeDefinitionUriCache().setValue(string, updated.getName());
		}
		return updated;
	}

	@Override
	public <E extends DefinitionModel> boolean isDefinitionEquals(E definition1, E definition2) {
		TimeTracker tracker = new TimeTracker();
		boolean traceEnabled = LOGGER.isTraceEnabled();
		if (traceEnabled) {
			hashCalculator.setStatisticsEnabled(true);
			tracker.begin();
		}
		int hashCode1 = getDefinitionAccessor(definition1.getClass(), true).computeHash(definition1);
		List<String> statistics1 = hashCalculator.getStatistics();

		int hashCode2 = getDefinitionAccessor(definition2.getClass(), true).computeHash(definition2);
		List<String> statistics2 = hashCalculator.getStatistics();

		boolean result = hashCode1 == hashCode2;

		if (traceEnabled) {
			hashCalculator.setStatisticsEnabled(false);
			LOGGER.trace("Compared definitions {} vs {} with result {}, compare took {} s", definition1.getIdentifier(),
					definition2.getIdentifier(), result, tracker.stopInSeconds());

			if (!result) {
				StringBuilder builder = new StringBuilder(1000);
				builder.append(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				List<String> diff = EqualsHelper.diffLists(statistics1, statistics2);
				for (String string : diff) {
					builder.append('\n').append(string);
				}
				builder.append("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				diff.clear();
				LOGGER.trace("Definitions not equal! Report: \n{}", builder);
			}
		}

		statistics1.clear();
		statistics2.clear();

		return result;
	}

	@Override
	public <E extends TopLevelDefinition> E saveDefinition(E definition) {
		DefinitionAccessor accessor = getDefinitionAccessor(definition.getClass(), true);
		// clear all max revision so they later can reinitialize
		getMaxRevisionsCache().clear();
		// updates the standard typed both caches
		return accessor.saveDefinition(definition);
	}

	@Override
	public DeletedDefinitionInfo deleteDefinition(DefinitionModel model) {
		if (model == null) {
			return DeletedDefinitionInfo.EMPTY_INFO;
		}
		return deleteDefinition(model.getClass(), model.getIdentifier(), model.getRevision());
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public DeletedDefinitionInfo deleteLastDefinition(Class type, String definition) {
		Collection<DeletedDefinitionInfo> internal = deleteInternal(type, definition, -1L,
				DefinitionAccessor.DefinitionDeleteMode.LAST_REVISION);
		if (CollectionUtils.isEmpty(internal)) {
			return DeletedDefinitionInfo.EMPTY_INFO;
		}
		return internal.iterator().next();
	}

	@Override
	public Collection<DeletedDefinitionInfo> deleteAllDefinitionRevisions(Class<?> type, String definition) {
		return deleteInternal(type, definition, -1L, DefinitionAccessor.DefinitionDeleteMode.ALL);
	}

	@Override
	public Collection<DeletedDefinitionInfo> deleteOldDefinitionRevisions(Class<?> type, String definition) {
		return deleteInternal(type, definition, -1L, DefinitionAccessor.DefinitionDeleteMode.OLD_REVISIONS);
	}

	@Override
	public DeletedDefinitionInfo deleteDefinition(Class<?> type, String definition, Long revision) {
		Collection<DeletedDefinitionInfo> internal = deleteInternal(type, definition, revision,
				DefinitionAccessor.DefinitionDeleteMode.SINGLE_REVISION);
		if (CollectionUtils.isEmpty(internal)) {
			return DeletedDefinitionInfo.EMPTY_INFO;
		}
		return internal.iterator().next();
	}

	private Collection<DeletedDefinitionInfo> deleteInternal(Class<?> type, String definition, Long revision,
			DefinitionAccessor.DefinitionDeleteMode mode) {
		if (type == null) {
			return Collections.emptyList();
		}
		DefinitionAccessor accessor = getDefinitionAccessor(type, true);
		Collection<DeletedDefinitionInfo> removeDefinitions = accessor.removeDefinition(definition, revision, mode);
		if (!removeDefinitions.isEmpty()) {
			getMaxRevisionsCache().removeByKey(type);
		}
		return removeDefinitions;
	}
}
