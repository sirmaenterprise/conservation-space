package com.sirma.itt.seip.instance.properties.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.convert.TypeConversionException;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PrototypeDefinition;
import com.sirma.itt.seip.domain.exceptions.DictionaryException;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.instance.properties.PropertiesStorageAccess;
import com.sirma.itt.seip.instance.properties.PropertyEntryKey;
import com.sirma.itt.seip.instance.properties.PropertyModelValue;

/**
 * This class provides services for translating exploded properties as persisted in the concrete store in the public
 * form, which is a <tt>Map</tt> of values keyed by their name.
 *
 * @author Derek Hulley
 * @author BBonev
 */
@ApplicationScoped
public class NodePropertyHelper implements Serializable {

	/**
	 * The collection index used to indicate that the value is not part of a collection. All values from zero up are
	 * used for real collection indexes.
	 */
	private static final int IDX_NO_COLLECTION = -1;

	private static final String PROPERTY = "   Property: ";

	private static final long serialVersionUID = -2413380724451901432L;
	private static final Logger LOGGER = LoggerFactory.getLogger(NodePropertyHelper.class);

	@Inject
	private DefinitionService definitionService;

	@Inject
	private ServiceRegistry serviceRegistry;

	@Inject
	private DatabaseIdManager idManager;

	/**
	 * Convert to persistent properties.
	 *
	 * @param in
	 *            the in
	 * @param pathElement
	 *            the path element
	 * @param access
	 *            the factory used to create the new property model value instances
	 * @return the map
	 */
	public Map<PropertyEntryKey, PropertyModelValue> convertToPersistentProperties(Map<String, Serializable> in,
			PathElement pathElement, PropertiesStorageAccess access) {
		Map<PropertyEntryKey, PropertyModelValue> propertyMap = CollectionUtils.createLinkedHashMap(in.size() + 5);
		// check if the model has a defined definition at all
		boolean hasDefinition = StringUtils.isNotBlank(PathHelper.getRootPath(pathElement));
		// if there is no definition and the option to save properties without definition is not set
		// then we does not persist the properties
		if (!hasDefinition && !Options.SAVE_PROPERTIES_WITHOUT_DEFINITION.isEnabled()) {
			LOGGER.warn(
					"The model {} does not have a definition and the option for persisting properties without definition is not active, so his properties will not be saved!",
					pathElement.getClass());
			return Collections.emptyMap();
		}

		DefinitionModel definitionModel = null;
		if (pathElement instanceof Instance) {
			definitionModel = definitionService.getInstanceDefinition((Instance) pathElement);
		} else if (pathElement instanceof DefinitionModel) {
			definitionModel = (DefinitionModel) pathElement;
		}

		for (Entry<String, Serializable> entry : in.entrySet()) {
			Serializable value = entry.getValue();
			// does not persist null values
			if (value != null) {
				// Get the qname ID
				String propertyQName = entry.getKey();
				PrototypeDefinition propertyDef = access.getPropertyPrototype(propertyQName, value, pathElement,
						definitionModel);
				if (propertyDef == null) {
					LOGGER.warn("No property definition for ({}, {}). Will not be saved", propertyQName,
							PathHelper.getPath(pathElement));
				} else {
					// Add it to the map
					addValueToPersistedProperties(propertyMap, propertyDef, NodePropertyHelper.IDX_NO_COLLECTION,
							propertyDef.getId(), value, access);
				}
			}
		}
		// Done
		return propertyMap;
	}

	/**
	 * A method that adds properties to the given map. It copes with collections.
	 *
	 * @param propertyMap
	 *            the property map
	 * @param propertyDef
	 *            the property definition (<tt>null</tt> is allowed)
	 * @param collectionIndex
	 *            the index of the property in the collection or <tt>-1</tt> if we are not yet processing a collection
	 * @param propertyQNameId
	 *            the property q name id
	 * @param value
	 *            the value
	 * @param access
	 *            the access object used to create property value instances
	 */
	private void addValueToPersistedProperties(Map<PropertyEntryKey, PropertyModelValue> propertyMap,
			PrototypeDefinition propertyDef, int collectionIndex, Long propertyQNameId, Serializable value,
			PropertiesStorageAccess access) {
		int localCollectionIndex = collectionIndex;
		if (value == null) {
			// The property is null. Null is null and cannot be massaged any
			// other way.
			PropertyModelValue npValue = makeNodePropertyValue(propertyDef, null, access);
			PropertyKey npKey = new PropertyKey();
			npKey.setListIndex(localCollectionIndex);
			npKey.setPropertyId(propertyQNameId);
			// Add it to the map
			propertyMap.put(npKey, npValue);
			// Done
			return;
		}

		// Get or spoof the property datatype
		String propertyTypeQName;
		// property not recognised
		if (propertyDef == null) {
			// allow it for now - persisting excess properties can be useful
			// sometimes
			propertyTypeQName = DataTypeDefinition.ANY;
		} else {
			propertyTypeQName = propertyDef.getDataType().getName();
		}

		// A property may appear to be multi-valued if the model definition is
		// loose and
		// an unexploded collection is passed in. Otherwise, use the
		// model-defined behaviour
		// strictly.
		boolean isMultiValued = false;
		if (propertyTypeQName.equals(DataTypeDefinition.ANY)) {
			// It is multi-valued if required (we are not in a collection and
			// the property is a new collection)
			isMultiValued = value instanceof Collection<?> && localCollectionIndex == IDX_NO_COLLECTION;
		} else if (propertyDef != null) {
			isMultiValued = propertyDef.isMultiValued().booleanValue();
		}

		// Handle different scenarios.
		// - Do we need to explode a collection?
		// - Does the property allow collections?
		if (localCollectionIndex == IDX_NO_COLLECTION && isMultiValued && !(value instanceof Collection<?>)) {
			// We are not (yet) processing a collection but the property should
			// be part of a collection
			addValueToPersistedProperties(propertyMap, propertyDef, 0, propertyQNameId, value, access);
		} else if (localCollectionIndex == IDX_NO_COLLECTION && value instanceof Collection<?>) {
			// We are not (yet) processing a collection and the property is a
			// collection i.e. needs exploding
			// Check that multi-valued properties are supported if the property
			// is a collection
			if (!isMultiValued) {
				throw new DictionaryException(
						"A single-valued property of this type may not be a collection: \n" + PROPERTY + propertyDef
								+ "\n" + "   Type: " + propertyTypeQName + "\n" + "   Value: " + value);
			}
			// We have an allowable collection.
			@SuppressWarnings("unchecked")
			Collection<Object> collectionValues = (Collection<Object>) value;
			// Persist empty collections directly. This is handled by the
			// NodePropertyValue.
			if (collectionValues.isEmpty()) {
				PropertyModelValue npValue = makeNodePropertyValue(null, (Serializable) collectionValues, access);
				PropertyKey npKey = new PropertyKey();
				npKey.setListIndex(NodePropertyHelper.IDX_NO_COLLECTION);
				npKey.setPropertyId(propertyQNameId);
				// Add it to the map
				propertyMap.put(npKey, npValue);
			}
			// Break it up and recurse to persist the values.
			localCollectionIndex = -1;
			for (Object collectionValueObj : collectionValues) {
				localCollectionIndex++;
				if (collectionValueObj != null && !(collectionValueObj instanceof Serializable)) {
					throw new IllegalArgumentException("Node properties must be fully serializable, "
							+ "including values contained in collections. \n" + PROPERTY + propertyDef + "\n"
							+ "   Index:    " + localCollectionIndex + "\n" + "   Value:    " + collectionValueObj);
				}
				Serializable collectionValue = (Serializable) collectionValueObj;
				try {
					addValueToPersistedProperties(propertyMap, propertyDef, localCollectionIndex, propertyQNameId,
							collectionValue, access);
				} catch (Exception e) {
					throw new EmfRuntimeException("Failed to persist collection entry: \n" + PROPERTY + propertyDef
							+ "\n" + "   Index:    " + localCollectionIndex + "\n" + "   Value:    " + collectionValue,
							e);
				}
			}
		} else {
			if (value instanceof Instance) {
				Instance instance = (Instance) value;
				persistValueOfTypeInstance(instance);
			}
			// We are either processing collection elements OR the property is
			// not a collection
			// Collections of collections are only supported by type d:any
			if (value instanceof Collection<?> && !propertyTypeQName.equals(DataTypeDefinition.ANY)) {
				throw new DictionaryException(
						"Collections of collections (Serializable) are only supported by type 'd:any': \n" + PROPERTY
								+ propertyDef + "\n" + "   Type: " + propertyTypeQName + "\n" + "   Value: " + value);
			}
			PropertyModelValue npValue = makeNodePropertyValue(propertyDef, value, access);
			PropertyKey npKey = new PropertyKey();
			npKey.setListIndex(localCollectionIndex);
			npKey.setPropertyId(propertyQNameId);
			// Add it to the map
			propertyMap.put(npKey, npValue);
		}
	}

	private void persistValueOfTypeInstance(Instance instance) {
		if (idManager.isPersisted(instance)) {
			return;
		}

		InstanceDao instanceDao = serviceRegistry.getInstanceDao(instance);
		if (instanceDao != null) {
			instanceDao.persistChanges(instance);
			LOGGER.debug("Saved unsaved Instance before properties persist!");
		}
	}

	/**
	 * Helper method to convert the <code>Serializable</code> value into a full, persistable {@link PropertyModelValue}.
	 * <p>
	 * Where the property definition is null, the value will take on the {@link DataTypeDefinition#ANY generic ANY}
	 * value.
	 * <p>
	 * Collections are NOT supported. These must be split up by the calling code before calling this method. Map
	 * instances are supported as plain serializable instances.
	 *
	 * @param propertyDef
	 *            the property dictionary definition, may be null
	 * @param value
	 *            the value, which will be converted according to the definition - may be null
	 * @param access
	 *            the callback used to create the value instance
	 * @return Returns the persistable property value
	 */
	public PropertyModelValue makeNodePropertyValue(PrototypeDefinition propertyDef, Serializable value,
			PropertiesStorageAccess access) {
		// get property attributes
		final String propertyTypeQName;
		// property not recognized
		if (propertyDef == null) {
			// allow it for now - persisting excess properties can be useful
			// sometimes
			propertyTypeQName = DataTypeDefinition.ANY;
		} else {
			propertyTypeQName = propertyDef.getDataType().getName();
		}
		try {
			return access.createPropertiesValue(propertyTypeQName, value);
		} catch (TypeConversionException e) {
			throw new TypeConversionException(
					"The property value is not compatible with the type defined for the property: \n" + "   property: "
							+ (propertyDef == null ? "unknown" : propertyDef) + "\n" + "   value: " + value + "\n"
							+ "   value type: " + value.getClass(),
					e);
		}
	}

	/**
	 * Gets the public property.
	 *
	 * @param propertyValues
	 *            the property values
	 * @param propertyQName
	 *            the property q name
	 * @param revision
	 *            the property revision
	 * @param pathElement
	 *            the path element
	 * @return the public property
	 */
	public Serializable getPublicProperty(Map<PropertyEntryKey, PropertyModelValue> propertyValues,
			String propertyQName, Long revision, PathElement pathElement) {
		// Get the qname ID
		PrototypeDefinition propertyDefinition = definitionService.getPrototype(propertyQName, revision, pathElement);
		if (propertyDefinition == null) {
			// There is no persisted property with that QName, so we can't match
			// anything
			return null;
		}
		Long qnameId = propertyDefinition.getId();
		// Now loop over the properties and extract those with the given qname
		// ID
		SortedMap<PropertyEntryKey, PropertyModelValue> scratch = new TreeMap<>();
		for (Map.Entry<PropertyEntryKey, PropertyModelValue> entry : propertyValues.entrySet()) {
			PropertyEntryKey propertyKey = entry.getKey();
			if (propertyKey.getPropertyId().equals(qnameId)) {
				scratch.put(propertyKey, entry.getValue());
			}
		}
		// If we found anything, then collapse the properties to a Serializable
		if (!scratch.isEmpty()) {
			return collapsePropertiesWithSameQName(propertyDefinition, scratch);
		}
		return null;
	}

	/**
	 * Convert to public properties.
	 *
	 * @param propertyValues
	 *            the property values
	 * @return the map
	 */
	public Map<String, Serializable> convertToPublicProperties(
			Map<PropertyEntryKey, PropertyModelValue> propertyValues) {
		Map<String, Serializable> propertyMap = CollectionUtils.createLinkedHashMap(propertyValues.size());
		// Shortcut
		if (propertyValues.isEmpty()) {
			return propertyMap;
		}
		// We need to process the properties in order
		SortedMap<PropertyEntryKey, PropertyModelValue> sortedPropertyValues = new TreeMap<>(propertyValues);
		// A working map. Ordering is important.
		SortedMap<PropertyEntryKey, PropertyModelValue> scratch = new TreeMap<>();
		// Iterate (sorted) over the map entries and extract values with the
		// same qname
		Long currentQNameId = Long.MIN_VALUE;
		Iterator<Map.Entry<PropertyEntryKey, PropertyModelValue>> iterator = sortedPropertyValues.entrySet().iterator();
		while (true) {
			Long nextQNameId = null;
			PropertyEntryKey nextPropertyKey = null;
			PropertyModelValue nextPropertyValue = null;
			// Record the next entry's values
			if (iterator.hasNext()) {
				Map.Entry<PropertyEntryKey, PropertyModelValue> entry = iterator.next();
				nextPropertyKey = entry.getKey();
				nextPropertyValue = entry.getValue();
				nextQNameId = nextPropertyKey.getPropertyId();
			}
			// If the QName is going to change, and we have some entries to
			// process, then process them.
			if (!scratch.isEmpty() && (nextQNameId == null || !nextQNameId.equals(currentQNameId))) {
				PrototypeDefinition currentPropertyDef = definitionService.getProperty(currentQNameId);
				// We have added something to the scratch properties but the
				// qname has just changed
				Serializable collapsedValue = null;
				// We can shortcut if there is only one value
				if (scratch.size() == 1) {
					// There is no need to collapse list indexes
					collapsedValue = collapsePropertiesWithSameQNameAndListIndex(currentPropertyDef, scratch);
				} else {
					// There is more than one value so the list indexes need to
					// be collapsed
					collapsedValue = collapsePropertiesWithSameQName(currentPropertyDef, scratch);
				}
				boolean forceCollection = false;
				// If the property is multi-valued then the output property must
				// be a collection
				if (currentPropertyDef != null && currentPropertyDef.isMultiValued()) {
					forceCollection = true;
				} else if (scratch.size() == 1 && scratch.firstKey().getListIndex().intValue() > -1) {
					// This is to handle cases of collections where the property
					// is d:any but not
					// declared as multiple.
					forceCollection = true;
				}
				if (forceCollection && collapsedValue != null && !(collapsedValue instanceof Collection<?>)) {
					// Can't use Collections.singletonList: ETHREEOH-1172
					List<Serializable> collection = new ArrayList<>(1);
					collection.add(collapsedValue);
					collapsedValue = (Serializable) collection;
				}
				if (currentPropertyDef != null) {
					// Store the value
					propertyMap.put(currentPropertyDef.getIdentifier(), collapsedValue);
				}
				// Reset
				scratch.clear();
			}
			if (nextQNameId != null) {
				// Add to the current entries
				scratch.put(nextPropertyKey, nextPropertyValue);
				currentQNameId = nextQNameId;
			} else {
				// There is no next value to process
				break;
			}
		}
		// Done
		return propertyMap;
	}

	/**
	 * Collapse properties with same q name.
	 *
	 * @param currentPropertyDef
	 *            the property def
	 * @param sortedPropertyValues
	 *            the sorted property values
	 * @return the serializable
	 */
	private Serializable collapsePropertiesWithSameQName(PrototypeDefinition currentPropertyDef,
			SortedMap<PropertyEntryKey, PropertyModelValue> sortedPropertyValues) {
		Serializable result = null;
		Collection<Serializable> collectionResult = null;
		// A working map. Ordering is not important for this map.
		Map<PropertyEntryKey, PropertyModelValue> scratch = new LinkedHashMap<>(3);
		// Iterate (sorted) over the map entries and extract values with the
		// same list index
		Integer currentListIndex = Integer.MIN_VALUE;
		Iterator<Map.Entry<PropertyEntryKey, PropertyModelValue>> iterator = sortedPropertyValues.entrySet().iterator();
		while (true) {
			Integer nextListIndex = null;
			PropertyEntryKey nextPropertyKey = null;
			PropertyModelValue nextPropertyValue = null;
			// Record the next entry's values
			if (iterator.hasNext()) {
				Map.Entry<PropertyEntryKey, PropertyModelValue> entry = iterator.next();
				nextPropertyKey = entry.getKey();
				nextPropertyValue = entry.getValue();
				nextListIndex = nextPropertyKey.getListIndex();
			}
			// If the list index is going to change, and we have some entries to
			// process, then process them.
			if (!scratch.isEmpty() && (nextListIndex == null || !nextListIndex.equals(currentListIndex))) {
				// We have added something to the scratch properties but the
				// index has just changed
				Serializable collapsedValue = collapsePropertiesWithSameQNameAndListIndex(currentPropertyDef, scratch);
				// Store. If there is a value already, then we must build a
				// collection.
				if (result == null) {
					result = collapsedValue;
				} else if (collectionResult != null) {
					// We have started a collection, so just add the value to
					// it.
					collectionResult.add(collapsedValue);
				} else {
					// We already had a result, and now have another. A
					// collection has not been
					// started. We start a collection and explicitly keep track
					// of it so that
					// we don't get mixed up with collections of collections
					// (ETHREEOH-2064).
					collectionResult = new ArrayList<>(20);
					// Add the first result
					collectionResult.add(result);
					// Add the new value
					collectionResult.add(collapsedValue);
					result = (Serializable) collectionResult;
				}
				// Reset
				scratch.clear();
			}
			if (nextListIndex != null) {
				// Add to the current entries
				scratch.put(nextPropertyKey, nextPropertyValue);
				currentListIndex = nextListIndex;
			} else {
				// There is no next value to process
				break;
			}
		}
		// Make sure that multi-valued properties are returned as a collection
		if (currentPropertyDef != null && currentPropertyDef.isMultiValued() && result != null
				&& !(result instanceof Collection<?>)) {
			// Can't use Collections.singletonList: ETHREEOH-1172
			List<Serializable> collection = new ArrayList<>(1);
			collection.add(result);
			result = (Serializable) collection;
		}
		// Done
		return result;
	}

	/**
	 * At this level, the properties have the same qname and list index. They can only be separated by locale.
	 * Typically, MLText will fall into this category as only.
	 * <p>
	 * If there are multiple values then they can only be separated by locale. If they are separated by locale, then
	 * they have to be text-based. This means that the only way to store them is via MLText. Any other multi-locale
	 * properties cannot be deserialized.
	 *
	 * @param currentPropertyDef
	 *            the property def
	 * @param propertyValues
	 *            the property values
	 * @return the serializable
	 */
	private Serializable collapsePropertiesWithSameQNameAndListIndex(PrototypeDefinition currentPropertyDef,
			Map<PropertyEntryKey, PropertyModelValue> propertyValues) {
		int propertyValuesSize = propertyValues.size();
		Serializable value = null;
		if (propertyValues.isEmpty()) {
			// Nothing to do
			return value;
		}

		Integer listIndex = null;
		for (Map.Entry<PropertyEntryKey, PropertyModelValue> entry : propertyValues.entrySet()) {
			PropertyEntryKey propertyKey = entry.getKey();
			PropertyModelValue propertyValue = entry.getValue();

			// Check that the client code has gathered the values together
			// correctly
			listIndex = propertyKey.getListIndex();
			if (listIndex != null && !listIndex.equals(propertyKey.getListIndex())) {
				throw new IllegalStateException(
						"Expecting to collapse properties with same list index: " + propertyValues);
			}

			// Get the local entry value
			Serializable entryValue = makeSerializableValue(currentPropertyDef, propertyValue);

			// Check and warn if there are other values
			if (propertyValuesSize > 1) {
				LOGGER.warn(
						"Found localized properties along with a 'null' value in the default locale. \n   The localized values will be ignored; 'null' will be returned: \n   Property:          {}\n   Values:            {}",
						currentPropertyDef, propertyValues);
			}
			// The entry could be null or whatever value came out
			value = entryValue;
			break;
		}
		// Done
		return value;
	}

	/**
	 * Extracts the externally-visible property from the persistable value.
	 *
	 * @param currentPropertyDef
	 *            the model property definition - may be <tt>null</tt>
	 * @param propertyValue
	 *            the persisted property
	 * @return Returns the value of the property in the format dictated by the property definition, or null if the
	 *         property value is null
	 */
	public Serializable makeSerializableValue(PrototypeDefinition currentPropertyDef,
			PropertyModelValue propertyValue) {
		if (propertyValue == null) {
			return null;
		}
		// get property attributes
		final String propertyTypeQName;
		if (currentPropertyDef == null) {
			// allow this for now
			propertyTypeQName = DataTypeDefinition.ANY;
		} else {
			propertyTypeQName = currentPropertyDef.getDataType().getName();
		}
		try {
			Serializable value = propertyValue.getValue(propertyTypeQName);
			// Handle conversions to and from ContentData
			return value;
		} catch (TypeConversionException e) {
			throw new TypeConversionException(
					"The property value is not compatible with the type defined for the property: \n" + "   property: "
							+ (currentPropertyDef == null ? "unknown" : currentPropertyDef) + "\n"
							+ "   property value: " + propertyValue,
					e);
		}
	}
}
