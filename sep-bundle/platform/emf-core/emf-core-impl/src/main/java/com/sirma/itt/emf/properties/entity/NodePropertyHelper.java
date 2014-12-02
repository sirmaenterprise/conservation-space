/*
 * Copyright (C) 2005-2010 Alfresco Software Limited. This file is part of
 * Alfresco Alfresco is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version. Alfresco is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with Alfresco. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.sirma.itt.emf.properties.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PrototypeDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.exceptions.DictionaryException;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.exceptions.TypeConversionException;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.properties.dao.PersistentPropertiesExtension;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.PathHelper;

/**
 * This class provides services for translating exploded properties (as
 * persisted in <b>alf_node_properties</b>) in the public form, which is a
 * <tt>Map</tt> of values keyed by their <tt>QName</tt>.
 *
 * @author Derek Hulley
 * @since 3.4
 */
@ApplicationScoped
public class NodePropertyHelper implements Serializable {

	private static final String PROPERTY = "   Property: ";

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2413380724451901432L;

	/** The Constant logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(NodePropertyHelper.class);

	/** The Constant debug. */
	private static final boolean DEBUG = LOGGER.isDebugEnabled();

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;

	/** The instance dao. */
	@Inject
	private ServiceRegister serviceRegister;

	/** The persistent properties. */
	@Inject
	@ExtensionPoint(value = PersistentPropertiesExtension.TARGET_NAME)
	private Iterable<PersistentPropertiesExtension> persistentProperties;

	/** Set of properties that are allowed to be saved without definition. */
	private Set<String> alwaysPersistProperties = null;

	/**
	 * Initialize configurations
	 */
	@PostConstruct
	public void initialize() {
		alwaysPersistProperties = new HashSet<String>(50);
		for (PersistentPropertiesExtension extension : persistentProperties) {
			alwaysPersistProperties.addAll(extension.getPersistentProperties());
		}
	}

	/**
	 * Convert to persistent properties.
	 *
	 * @param in
	 *            the in
	 * @param revision
	 *            is the object revision
	 * @param pathElement
	 *            the path element
	 * @return the map
	 */
	public Map<PropertyKey, PropertyValue> convertToPersistentProperties(
			Map<String, Serializable> in, Long revision, PathElement pathElement) {
		Map<PropertyKey, PropertyValue> propertyMap = new LinkedHashMap<PropertyKey, PropertyValue>(
				in.size() + 5);
		// check if the model has a defined definition at all
		boolean hasDefinition = StringUtils.isNotNullOrEmpty(PathHelper.getRootPath(pathElement));
		// if there is no definition and the option to save properties without definition is not set
		// then we does not persist the properties
		if (!hasDefinition
				&& !RuntimeConfiguration
						.isConfigurationSet(RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION)) {
			LOGGER.warn(
					"The model {} does not have a definition and the option for persisting properties without definition is not active, so his properties will not be saved!",
					pathElement.getClass());
			return Collections.emptyMap();
		}

		DefinitionModel definitionModel = null;
		if (pathElement instanceof Instance) {
			definitionModel = dictionaryService.getInstanceDefinition((Instance) pathElement);
		}

		for (Map.Entry<String, Serializable> entry : in.entrySet()) {
			Serializable value = entry.getValue();
			// does not persist null values
			if (value != null) {
				// Get the qname ID
				String propertyQName = entry.getKey();
				PrototypeDefinition propertyDef = loadPropertyDefinition(revision, pathElement,
						hasDefinition, definitionModel, entry, value, propertyQName);
				if (propertyDef == null) {
					LOGGER.warn("No property definition for ({}, {}, {}). Will not be saved",
							propertyQName, revision, PathHelper.getPath(pathElement));
				} else {
					// Add it to the map
					addValueToPersistedProperties(propertyMap, propertyDef,
							NodePropertyHelper.IDX_NO_COLLECTION, propertyDef.getId(), value);
				}
			}
		}
		// Done
		return propertyMap;
	}

	/**
	 * Load property definition.
	 *
	 * @param revision
	 *            the revision
	 * @param pathElement
	 *            the path element
	 * @param hasDefinition
	 *            the has definition
	 * @param definitionModel
	 *            the definition model
	 * @param entry
	 *            the entry
	 * @param value
	 *            the value
	 * @param propertyQName
	 *            the property q name
	 * @return the prototype definition
	 */
	private PrototypeDefinition loadPropertyDefinition(Long revision, PathElement pathElement,
			boolean hasDefinition, DefinitionModel definitionModel,
			Map.Entry<String, Serializable> entry, Serializable value, String propertyQName) {
		PrototypeDefinition propertyDef = null;
		// added optimization not to fetch multiple types the definition
		// try to fetch the property from the preloaded definition
		if (definitionModel != null) {
			propertyDef = PathHelper.findProperty(definitionModel, pathElement, entry.getKey());
		}
		// if not found we execute the old algorithm for retrieving property definition
		if (propertyDef == null) {
			if (alwaysPersistProperties.contains(propertyQName) || !hasDefinition) {
				propertyDef = dictionaryService.getDefinitionByValue(propertyQName, value);
			} else {
				// Get the property definition, if available
				propertyDef = dictionaryService.getPrototype(propertyQName, revision,
						pathElement);
			}
			if (propertyDef == null) {
				// if we have enabled custom saving then we can try to found an proper field to
				// save it into
				if (RuntimeConfiguration
						.isConfigurationSet(RuntimeConfigurationProperties.SAVE_PROPERTIES_WITHOUT_DEFINITION)) {
					propertyDef = dictionaryService.getDefinitionByValue(propertyQName, value);
					if (propertyDef == null) {
						LOGGER.debug(
								"Enabled property for saving fields with no definitions but the property value is not supported: {}",
								entry);
					}
				}
			}
		}
		return propertyDef;
	}

	/**
	 * The collection index used to indicate that the value is not part of a
	 * collection. All values from zero up are used for real collection indexes.
	 */
	private static final int IDX_NO_COLLECTION = -1;

	/**
	 * A method that adds properties to the given map. It copes with collections.
	 *
	 * @param propertyMap
	 *            the property map
	 * @param propertyDef
	 *            the property definition (<tt>null</tt> is allowed)
	 * @param collectionIndex
	 *            the index of the property in the collection or <tt>-1</tt> if we are not yet
	 *            processing a collection
	 * @param propertyQNameId
	 *            the property q name id
	 * @param value
	 *            the value
	 */
	private void addValueToPersistedProperties(Map<PropertyKey, PropertyValue> propertyMap,
			PrototypeDefinition propertyDef, int collectionIndex, Long propertyQNameId,
			Serializable value) {
		int localCollectionIndex = collectionIndex;
		if (value == null) {
			// The property is null. Null is null and cannot be massaged any
			// other way.
			PropertyValue npValue = makeNodePropertyValue(propertyDef, null);
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
		boolean isMultiValued;
		if (propertyTypeQName.equals(DataTypeDefinition.ANY)) {
			// It is multi-valued if required (we are not in a collection and
			// the property is a new collection)
			isMultiValued = (value instanceof Collection<?>)
					&& (localCollectionIndex == IDX_NO_COLLECTION);
		} else {
			isMultiValued = propertyDef.isMultiValued();
		}

		// Handle different scenarios.
		// - Do we need to explode a collection?
		// - Does the property allow collections?
		if ((localCollectionIndex == IDX_NO_COLLECTION) && isMultiValued
				&& !(value instanceof Collection<?>)) {
			// We are not (yet) processing a collection but the property should
			// be part of a collection
			addValueToPersistedProperties(propertyMap, propertyDef, 0, propertyQNameId, value);
		} else if ((localCollectionIndex == IDX_NO_COLLECTION) && (value instanceof Collection<?>)) {
			// We are not (yet) processing a collection and the property is a
			// collection i.e. needs exploding
			// Check that multi-valued properties are supported if the property
			// is a collection
			if (!isMultiValued) {
				throw new DictionaryException(
						"A single-valued property of this type may not be a collection: \n"
								+ PROPERTY + propertyDef + "\n" + "   Type: "
								+ propertyTypeQName + "\n" + "   Value: " + value);
			}
			// We have an allowable collection.
			@SuppressWarnings("unchecked")
			Collection<Object> collectionValues = (Collection<Object>) value;
			// Persist empty collections directly. This is handled by the
			// NodePropertyValue.
			if (collectionValues.isEmpty()) {
				PropertyValue npValue = makeNodePropertyValue(null, (Serializable) collectionValues);
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
				if ((collectionValueObj != null) && !(collectionValueObj instanceof Serializable)) {
					throw new IllegalArgumentException(
							"Node properties must be fully serializable, "
									+ "including values contained in collections. \n"
									+ PROPERTY + propertyDef + "\n" + "   Index:    "
									+ localCollectionIndex + "\n" + "   Value:    "
									+ collectionValueObj);
				}
				Serializable collectionValue = (Serializable) collectionValueObj;
				try {
					addValueToPersistedProperties(propertyMap, propertyDef, localCollectionIndex,
							propertyQNameId, collectionValue);
				} catch (Exception e) {
					throw new EmfRuntimeException("Failed to persist collection entry: \n"
							+ PROPERTY + propertyDef + "\n" + "   Index:    "
							+ localCollectionIndex + "\n" + "   Value:    " + collectionValue, e);
				}
			}
		} else {
			if (value instanceof Instance) {
				Instance instance = (Instance) value;
				if (!SequenceEntityGenerator.isPersisted(instance)) {
					InstanceDao<Instance> instanceDao = serviceRegister.getInstanceDao(instance);
					if (instanceDao != null) {
						instanceDao.persistChanges(instance);
						if (DEBUG) {
							LOGGER.debug("Saved unsaved Instance before properties persist!");
						}
					}
				}
			}
			// We are either processing collection elements OR the property is
			// not a collection
			// Collections of collections are only supported by type d:any
			if ((value instanceof Collection<?>) && !propertyTypeQName.equals(DataTypeDefinition.ANY)) {
				throw new DictionaryException(
						"Collections of collections (Serializable) are only supported by type 'd:any': \n"
								+ PROPERTY + propertyDef + "\n" + "   Type: "
								+ propertyTypeQName + "\n" + "   Value: " + value);
			}
			PropertyValue npValue = makeNodePropertyValue(propertyDef, value);
			PropertyKey npKey = new PropertyKey();
			npKey.setListIndex(localCollectionIndex);
			npKey.setPropertyId(propertyQNameId);
			// Add it to the map
			propertyMap.put(npKey, npValue);
		}
	}

	/**
	 * Helper method to convert the <code>Serializable</code> value into a full,
	 * persistable {@link PropertyValue}.
	 * <p>
	 * Where the property definition is null, the value will take on the
	 * {@link DataTypeDefinition#ANY generic ANY} value.
	 * <p>
	 * Collections are NOT supported. These must be split up by the calling code
	 * before calling this method. Map instances are supported as plain
	 * serializable instances.
	 *
	 * @param propertyDef
	 *            the property dictionary definition, may be null
	 * @param value
	 *            the value, which will be converted according to the definition
	 *            - may be null
	 * @return Returns the persistable property value
	 */
	public PropertyValue makeNodePropertyValue(PrototypeDefinition propertyDef, Serializable value) {
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
			PropertyValue propertyValue = new PropertyValue(propertyTypeQName, value);
			// done
			return propertyValue;
		} catch (TypeConversionException e) {
			throw new TypeConversionException(
					"The property value is not compatible with the type defined for the property: \n"
							+ "   property: " + (propertyDef == null ? "unknown" : propertyDef)
							+ "\n" + "   value: " + value + "\n" + "   value type: "
							+ value.getClass(), e);
		}
	}

	/**
	 * Gets the public property.
	 *
	 * @param propertyValues the property values
	 * @param propertyQName the property q name
	 * @param revision the property revision
	 * @param pathElement the path element
	 * @return the public property
	 */
	public Serializable getPublicProperty(Map<PropertyKey, PropertyValue> propertyValues,
			String propertyQName, Long revision, PathElement pathElement) {
		// Get the qname ID
		PrototypeDefinition propertyDefinition = dictionaryService.getPrototype(propertyQName,
				revision, pathElement);
		if (propertyDefinition == null) {
			// There is no persisted property with that QName, so we can't match
			// anything
			return null;
		}
		Long qnameId = propertyDefinition.getId();
		// Now loop over the properties and extract those with the given qname
		// ID
		SortedMap<PropertyKey, PropertyValue> scratch = new TreeMap<PropertyKey, PropertyValue>();
		for (Map.Entry<PropertyKey, PropertyValue> entry : propertyValues.entrySet()) {
			PropertyKey propertyKey = entry.getKey();
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
	 * @param revision
	 *            the property revision
	 * @param pathElement
	 *            the path element
	 * @return the map
	 */
	public Map<String, Serializable> convertToPublicProperties(
			Map<PropertyKey, PropertyValue> propertyValues, Long revision, PathElement pathElement) {
		Map<String, Serializable> propertyMap = CollectionUtils.createLinkedHashMap(propertyValues.size());
		// Shortcut
		if (propertyValues.isEmpty()) {
			return propertyMap;
		}
		// We need to process the properties in order
		SortedMap<PropertyKey, PropertyValue> sortedPropertyValues = new TreeMap<PropertyKey, PropertyValue>(
				propertyValues);
		// A working map. Ordering is important.
		SortedMap<PropertyKey, PropertyValue> scratch = new TreeMap<PropertyKey, PropertyValue>();
		// Iterate (sorted) over the map entries and extract values with the
		// same qname
		Long currentQNameId = Long.MIN_VALUE;
		Iterator<Map.Entry<PropertyKey, PropertyValue>> iterator = sortedPropertyValues.entrySet()
				.iterator();
		while (true) {
			Long nextQNameId = null;
			PropertyKey nextPropertyKey = null;
			PropertyValue nextPropertyValue = null;
			// Record the next entry's values
			if (iterator.hasNext()) {
				Map.Entry<PropertyKey, PropertyValue> entry = iterator.next();
				nextPropertyKey = entry.getKey();
				nextPropertyValue = entry.getValue();
				nextQNameId = nextPropertyKey.getPropertyId();
			}
			// If the QName is going to change, and we have some entries to
			// process, then process them.
			if (!scratch.isEmpty()
					&& ((nextQNameId == null) || !nextQNameId.equals(currentQNameId))) {
				PrototypeDefinition currentPropertyDef = dictionaryService.getProperty(currentQNameId);
				// We have added something to the scratch properties but the
				// qname has just changed
				Serializable collapsedValue = null;
				// We can shortcut if there is only one value
				if (scratch.size() == 1) {
					// There is no need to collapse list indexes
					collapsedValue = collapsePropertiesWithSameQNameAndListIndex(
							currentPropertyDef, scratch);
				} else {
					// There is more than one value so the list indexes need to
					// be collapsed
					collapsedValue = collapsePropertiesWithSameQName(currentPropertyDef, scratch);
				}
				boolean forceCollection = false;
				// If the property is multi-valued then the output property must
				// be a collection
				if ((currentPropertyDef != null) && currentPropertyDef.isMultiValued()) {
					forceCollection = true;
				} else if ((scratch.size() == 1) && (scratch.firstKey().getListIndex().intValue() > -1)) {
					// This is to handle cases of collections where the property
					// is d:any but not
					// declared as multiple.
					forceCollection = true;
				}
				if (forceCollection && (collapsedValue != null)
						&& !(collapsedValue instanceof Collection<?>)) {
					// Can't use Collections.singletonList: ETHREEOH-1172
					List<Serializable> collection = new ArrayList<Serializable>(1);
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
			SortedMap<PropertyKey, PropertyValue> sortedPropertyValues) {
		Serializable result = null;
		Collection<Serializable> collectionResult = null;
		// A working map. Ordering is not important for this map.
		Map<PropertyKey, PropertyValue> scratch = new LinkedHashMap<PropertyKey, PropertyValue>(3);
		// Iterate (sorted) over the map entries and extract values with the
		// same list index
		Integer currentListIndex = Integer.MIN_VALUE;
		Iterator<Map.Entry<PropertyKey, PropertyValue>> iterator = sortedPropertyValues.entrySet()
				.iterator();
		while (true) {
			Integer nextListIndex = null;
			PropertyKey nextPropertyKey = null;
			PropertyValue nextPropertyValue = null;
			// Record the next entry's values
			if (iterator.hasNext()) {
				Map.Entry<PropertyKey, PropertyValue> entry = iterator.next();
				nextPropertyKey = entry.getKey();
				nextPropertyValue = entry.getValue();
				nextListIndex = nextPropertyKey.getListIndex();
			}
			// If the list index is going to change, and we have some entries to
			// process, then process them.
			if (!scratch.isEmpty()
					&& ((nextListIndex == null) || !nextListIndex.equals(currentListIndex))) {
				// We have added something to the scratch properties but the
				// index has just changed
				Serializable collapsedValue = collapsePropertiesWithSameQNameAndListIndex(
						currentPropertyDef, scratch);
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
					collectionResult = new ArrayList<Serializable>(20);
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
		if ((currentPropertyDef != null) && currentPropertyDef.isMultiValued() && (result != null)
				&& !(result instanceof Collection<?>)) {
			// Can't use Collections.singletonList: ETHREEOH-1172
			List<Serializable> collection = new ArrayList<Serializable>(1);
			collection.add(result);
			result = (Serializable) collection;
		}
		// Done
		return result;
	}

	/**
	 * At this level, the properties have the same qname and list index. They can only be separated
	 * by locale. Typically, MLText will fall into this category as only.
	 * <p>
	 * If there are multiple values then they can only be separated by locale. If they are separated
	 * by locale, then they have to be text-based. This means that the only way to store them is via
	 * MLText. Any other multi-locale properties cannot be deserialized.
	 *
	 * @param currentPropertyDef
	 *            the property def
	 * @param propertyValues
	 *            the property values
	 * @return the serializable
	 */
	private Serializable collapsePropertiesWithSameQNameAndListIndex(
			PrototypeDefinition currentPropertyDef, Map<PropertyKey, PropertyValue> propertyValues) {
		int propertyValuesSize = propertyValues.size();
		Serializable value = null;
		if (propertyValues.isEmpty()) {
			// Nothing to do
			return value;
		}

		Integer listIndex = null;
		for (Map.Entry<PropertyKey, PropertyValue> entry : propertyValues.entrySet()) {
			PropertyKey propertyKey = entry.getKey();
			PropertyValue propertyValue = entry.getValue();

			// Check that the client code has gathered the values together
			// correctly
			listIndex = propertyKey.getListIndex();
			if ((listIndex != null) && !listIndex.equals(propertyKey.getListIndex())) {
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
	 * @return Returns the value of the property in the format dictated by the property definition,
	 *         or null if the property value is null
	 */
	public Serializable makeSerializableValue(PrototypeDefinition currentPropertyDef,
			PropertyValue propertyValue) {
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
					"The property value is not compatible with the type defined for the property: \n"
							+ "   property: "
							+ (currentPropertyDef == null ? "unknown" : currentPropertyDef)
							+ "\n" + "   property value: " + propertyValue, e);
		}
	}
}
