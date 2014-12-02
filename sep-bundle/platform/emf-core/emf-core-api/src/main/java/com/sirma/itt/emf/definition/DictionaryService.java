package com.sirma.itt.emf.definition;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.PrototypeDefinition;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * The Interface DictionaryService.
 * 
 * @author BBonev
 */
public interface DictionaryService extends Serializable {

	/**
	 * Fetches the highest revision of all definitions of the given type.
	 * 
	 * @param <E>
	 *            the definition type
	 * @param ref
	 *            the reference class to fetch the definitions for
	 * @return the all definitions
	 */
	<E extends DefinitionModel> List<E> getAllDefinitions(Class<E> ref);

	/**
	 * Gets the highest revision for the given type and definition id.
	 * 
	 * @param <E>
	 *            the definition type
	 * @param ref
	 *            the reference class to fetch the definitions for
	 * @param defId
	 *            the def id
	 * @return the definition
	 */
	<E extends DefinitionModel> E getDefinition(Class<E> ref, String defId);

	/**
	 * Gets the definition by ID and revision for the given type.
	 * 
	 * @param <E>
	 *            the definition type
	 * @param ref
	 *            the reference class to fetch the definitions for
	 * @param defId
	 *            the def id
	 * @param version
	 *            the version
	 * @return the definition
	 */
	<E extends DefinitionModel> E getDefinition(Class<E> ref, String defId, Long version);

	/**
	 * Gets the definition versions for the given type.
	 * 
	 * @param <E>
	 *            the definition type
	 * @param ref
	 *            the reference class to fetch the definitions for
	 * @param defId
	 *            the def id
	 * @return the case definition version
	 */
	<E extends DefinitionModel> List<E> getDefinitionVersions(Class<E> ref, String defId);

	/**
	 * Gets the property.
	 * 
	 * @param currentQName
	 *            the current q name
	 * @param revision
	 *            is the property revision
	 * @param pathElement
	 *            the path element
	 * @return the property
	 */
	PropertyDefinition getProperty(String currentQName, Long revision, PathElement pathElement);

	/**
	 * Gets the prototype of property of the given name.
	 * 
	 * @param currentQName
	 *            the current q name
	 * @param revision
	 *            the revision
	 * @param pathElement
	 *            the path element
	 * @return the prototype
	 */
	PrototypeDefinition getPrototype(String currentQName, Long revision, PathElement pathElement);

	/**
	 * Gets the property id.
	 * 
	 * @param propertyName
	 *            the property name
	 * @param revision
	 *            is the property revision
	 * @param pathElement
	 *            the path element
	 * @param value
	 *            the value
	 * @return the property id
	 */
	Long getPropertyId(String propertyName, Long revision, PathElement pathElement,
			Serializable value);

	/**
	 * Gets the property by id.
	 * 
	 * @param propertyId
	 *            the property id
	 * @return the property by id
	 */
	String getPropertyById(Long propertyId);

	/**
	 * Gets the property definition by id.
	 * 
	 * @param propertyId
	 *            the property id
	 * @return the property definition
	 */
	PrototypeDefinition getProperty(Long propertyId);

	/**
	 * Gets a {@link DataTypeDefinition} by it's name or the {@link String} representation of his
	 * Java class name returned by {@link DataTypeDefinition#getJavaClassName()}.<br>
	 * <b>NOTE: </b> For optimization purposes the method will check if the given argument contains
	 * a dot character to determine if the given argument is class name, so the method will not find
	 * a class that is defined in the default package!
	 * 
	 * @param name
	 *            the name of the definition
	 * @return the found definition or <code>null</code> if not such definition exists
	 */
	DataTypeDefinition getDataTypeDefinition(String name);

	/**
	 * Filter properties.
	 * 
	 * @param model
	 *            the model
	 * @param properties
	 *            the properties
	 * @param displayType
	 *            the display type
	 * @return the map
	 */
	Map<String, Serializable> filterProperties(DefinitionModel model,
			Map<String, Serializable> properties, DisplayType displayType);

	/**
	 * Gets the definition for the given instance.
	 * 
	 * @param instance
	 *            the instance
	 * @return the instance definition if found valid definition
	 */
	DefinitionModel getInstanceDefinition(Instance instance);

	/**
	 * Detects the type of the given serializable object and based on the detected type the method
	 * should return a definition that could be used for property persistence. If argument is null
	 * or not supported then the method will return <code>null</code>.
	 * <p>
	 * <b>NOTE: </b> This method should not be used for common properties that have a definition.
	 * This is only for properties that does not have a definition or are not a part of one but need
	 * to be persisted anyway.
	 * 
	 * @param propertyName
	 *            the property name to search for
	 * @param serializable
	 *            the serializable to detect the type
	 * @return the property definition or <code>null</code> if not supported
	 */
	PrototypeDefinition getDefinitionByValue(String propertyName, Serializable serializable);

	/**
	 * Gets the mutable dictionary service instance.
	 * 
	 * @return the mutable instance
	 */
	MutableDictionaryService getMutableInstance();

}
