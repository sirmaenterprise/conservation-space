/**
 *
 */
package com.sirma.itt.emf.definition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.definition.model.BaseDefinition;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.DefinitionEntry;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.emf.definition.model.PrototypeDefinition;
import com.sirma.itt.emf.definition.model.PrototypeDefinitionImpl;
import com.sirma.itt.emf.definition.model.WritablePropertyDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.Quad;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.GenericProxy;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.exceptions.CmfDatabaseException;
import com.sirma.itt.emf.hash.HashCalculator;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Default service implementation for working with definitions.
 *
 * @author BBonev
 */
@Stateless
public class MutableDictionaryServiceImpl extends BaseDefinitionService implements MutableDictionaryService {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 8755426012305496783L;

	/** The Constant NO_DEFINITION_SELECTOR. */
	private static final String BASE_DEFINITION_SELECTOR = "$DEFAULT_DEFINITION$";

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MutableDictionaryServiceImpl.class);

	@Inject
	private HashCalculator hashCalculator;

	@Inject
	private DictionaryService dictionaryService;

	/** The trace. */
	private boolean trace;

	@Override
	@PostConstruct
	public void init() {
		trace = LOGGER.isTraceEnabled();

		super.init();

	}

	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public PropertyDefinition savePropertyIfChanged(PropertyDefinition newProperty,
			PropertyDefinition oldProperty) {
		// if we have old property we compare the two of them and copy the id if needed
		if ((oldProperty != null) && (oldProperty.getPrototypeId() != null)) {
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
			if (StringUtils.isNullOrEmpty(newProperty.getContainer())) {
				((WritablePropertyDefinition) newProperty)
						.setContainer(SecurityContextManager.NO_CONTAINER);
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
		if (!(newProperty instanceof PropertyDefinitionProxy)
				&& (newProperty instanceof WritablePropertyDefinition)
				&& (newProperty.getPrototypeId() == null)) {
			createOrUpdatePrototypeDefinition((WritablePropertyDefinition) newProperty);
			return newProperty;
		}
		return newProperty;
	}

	/**
	 * Creates the or update prototype definition.
	 *
	 * @param propertyDefinition
	 *            the property definition
	 */
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
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void initializeBasePropertyDefinitions() {
		EntityLookupCache<Pair<Class, String>, List<DefinitionModel>, Serializable> maxRevisionsCache = getMaxRevisionsCache();
		Pair<Class, String> key = new Pair<Class, String>(BaseDefinition.class,
				SecurityContextManager.NO_CONTAINER);
		Pair<Pair<Class, String>, List<DefinitionModel>> pair = maxRevisionsCache.getByKey(key);
		// definition exists we a done
		if ((pair != null) && (getCacheValue(pair) != null) && !getCacheValue(pair).isEmpty()) {
			if (trace) {
				LOGGER.trace("Base definition was initialized before. Nothing to do.");
			}
			return;
		}
		// create new empty definition
		BaseDefinition<BaseDefinition<?>> definition = new BaseDefinition<>();
		definition.setIdentifier(BASE_DEFINITION_SELECTOR);

		DefinitionEntry entry = new DefinitionEntry();
		entry.setContainer(SecurityContextManager.NO_CONTAINER);
		entry.setAbstract(Boolean.FALSE);
		entry.setDmsId(null);
		entry.setHash(-1);
		entry.setRevision(0L);
		entry.setIdentifier(BASE_DEFINITION_SELECTOR);
		entry.setTargetType(dictionaryService.getDataTypeDefinition(BaseDefinition.class.getName()));
		entry.setTarget(definition);

		DefinitionEntry baseDefinition = dbDao.saveOrUpdate(entry);

		maxRevisionsCache.setValue(key,
				new ArrayList<DefinitionModel>(Arrays.asList(baseDefinition)));
	}


	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public DataTypeDefinition saveDataTypeDefinition(DataTypeDefinition typeDefinition) {
		EntityLookupCache<String, DataTypeDefinition, String> typeCache = getTypeDefinitionCache();
		// clear the cache entry to force fetching of new entry !!!
		typeCache.removeByKey(typeDefinition.getName());
		typeCache.removeByValue(typeDefinition);

		Pair<String, DataTypeDefinition> pair = typeCache.getOrCreateByValue(typeDefinition);
		if (pair == null) {
			throw new CmfDatabaseException("Failed to persist type definition: " + typeDefinition);
		}
		DataTypeDefinition definition = pair.getSecond();
		// check for changes
		if (!hashCalculator.equalsByHash(definition, typeDefinition)) {
			typeDefinition.setId(definition.getId());
			definition = dbDao.saveOrUpdate(typeDefinition);
			typeCache.setValue(definition.getName(), typeDefinition);
		}
		// update the second cache
		for (String string : definition.getUries()) {
			getTypeDefinitionUriCache().setValue(string, definition.getName());
		}
		return pair.getSecond();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends DefinitionModel> boolean isDefinitionEquals(E definition1, E definition2) {
		TimeTracker tracker = null;
		if (trace) {
			hashCalculator.setStatisticsEnabled(true);
			tracker = new TimeTracker().begin();
		}
		int hashCode1 = getDefinitionAccessor(definition1.getClass(), true).computeHash(definition1);

		List<Pair<String, String>> statistics1 = hashCalculator.getStatistics(true);
		int hashCode2 = getDefinitionAccessor(definition2.getClass(), true).computeHash(definition2);
		List<Pair<String, String>> statistics2 = hashCalculator.getStatistics(true);
		boolean result = hashCode1 == hashCode2;
		if (trace) {
			hashCalculator.setStatisticsEnabled(false);
			LOGGER.trace("Compared definitions " + definition1.getIdentifier() + " vs "
					+ definition2.getIdentifier() + " with result " + result + ", compare took "
					+ tracker.stopInSeconds() + " s");

			if (!result) {
				StringBuilder builder = new StringBuilder(1000);
				builder.append(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				List<String> diff = EqualsHelper.diffLists(statistics1, statistics2);
				for (String string : diff) {
					builder.append('\n').append(string);
				}
				builder.append("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				LOGGER.trace("Definitions not equal! Report: \n" + builder);
			}
		}
		return result;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <E extends TopLevelDefinition> E saveDefinition(E definition) {
		DefinitionAccessor accessor = getDefinitionAccessor(definition.getClass(), true);
		// clear all max revision so they later can reinitialize
		getMaxRevisionsCache().clear();
		// updates the standard typed both caches
		return accessor.saveDefinition(definition);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <E extends TopLevelDefinition> E saveTemplateDefinition(E definition) {
		DefinitionAccessor accessor = getDefinitionAccessor(definition.getClass(), true);
		// updates the standard typed both caches
		return accessor.saveDefinition(definition);
	}

	@Override
	public List<Pair<String, String>> removeDefinitionsWithoutInstances(
			Set<String> definitionsToCheck) {
		List<Pair<String, String>> removed = new LinkedList<>();
		Set<String> allActiveDefinitions = new LinkedHashSet<>(200);
		for (DefinitionAccessor accessor : accessors) {
			Set<String> activeDefinitions = accessor.getActiveDefinitions();
			if (!activeDefinitions.isEmpty()) {
				Set<String> notActive = new HashSet<>(definitionsToCheck);
				notActive.removeAll(allActiveDefinitions);
			}
			allActiveDefinitions.addAll(activeDefinitions);
			for (String definitionId : activeDefinitions) {
				if (accessor.removeDefinition(definitionId, -1)) {
					removed.add(DefinitionIdentityUtil.parseDefinitionId(definitionId));
				}
			}
		}
		return removed;
	}


}
