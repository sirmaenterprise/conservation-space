package com.sirma.itt.seip.instance.properties;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PrototypeDefinition;
import com.sirma.itt.seip.domain.instance.PropertyModel;

/**
 * This class is used to provide access to concrete storage where the properties are persisted and means to create model
 * objects used for the concrete storage.
 *
 * @author BBonev
 */
public interface PropertiesStorageAccess {

	/**
	 * Creates empty properties entity.
	 *
	 * @return the property model entity
	 */
	PropertyModelEntity createPropertiesEntity();

	/**
	 * Creates the properties value object for the given type and actual value
	 *
	 * @param propertyType
	 *            the property type
	 * @param value
	 *            the value to store
	 * @return the property model value
	 */
	PropertyModelValue createPropertiesValue(String propertyType, Serializable value);

	/**
	 * Gets the cache used to access the properties associated with the storage. The method should not return
	 * <code>null</code> in case of no cache but an instance initialized without cache storage.
	 *
	 * @return the cache accessor and never <code>null</code>.
	 */
	EntityLookupCache<PropertyModelKey, Map<String, Serializable>, Serializable> getCache();

	/**
	 * Add the given properties into the database store for the given model instance identified by the model key.
	 *
	 * @param entityId
	 *            the entity id
	 * @param newPropsRaw
	 *            the new props raw
	 */
	void insertProperties(PropertyModelKey entityId, Map<PropertyEntryKey, PropertyModelValue> newPropsRaw);

	/**
	 * Delete properties the properties associated with the instance identified by the given model key. The given set of
	 * ids are the ids of property entries that needs to be deleted.
	 *
	 * @param entityId
	 *            the entity id
	 * @param propStringIdsToDelete
	 *            the prop string ids to delete
	 */
	void deleteProperties(PropertyModelKey entityId, Set<Long> propStringIdsToDelete);

	/**
	 * Batch load properties for all instances of the given type and identified by the given set of database identifiers
	 *
	 * @param
	 * 			<P>
	 *            the generic type
	 * @param beanType
	 *            the bean type
	 * @param beanIds
	 *            the bean ids
	 * @return the list
	 */
	<P extends PropertyModelEntity> List<P> batchLoadProperties(Integer beanType, Collection<String> beanIds);

	/**
	 * Filter out forbidden properties that are forbidden for the model
	 *
	 * @param properties
	 *            the properties
	 */
	void filterOutForbiddenProperties(Set<String> properties);

	/**
	 * Notify for changes in properties for instance.
	 *
	 * @param model
	 *            the model
	 * @param propsToDelete
	 *            the props to delete
	 * @param propsToAdd
	 *            the props to add
	 * @param actualProperties
	 *            the actual properties
	 */
	void notifyForChanges(PropertyModel model, Map<String, Serializable> propsToDelete,
			Map<String, Serializable> propsToAdd, Map<String, Serializable> actualProperties);

	/**
	 * Gets the property prototype. The prototype is used to store the property in the database.
	 *
	 * @param propertyName
	 *            the property name
	 * @param value
	 *            the current property value
	 * @param pathElement
	 *            the path element used to resolve property definition
	 * @param definitionModel
	 *            the definition model model by which the current property value is stored if any.
	 * @return the property prototype
	 */
	PrototypeDefinition getPropertyPrototype(String propertyName, Serializable value, PathElement pathElement,
			DefinitionModel definitionModel);

}
