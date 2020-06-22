package com.sirma.itt.seip.definition;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.PrototypeDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;

/**
 * Public service for accessing definitions and it's properties. The service provided a read only access. If
 * modifications are needed use {@link MutableDefinitionService} via inject or the method {@link #getMutableInstance()}.
 *
 * @author BBonev
 * @see MutableDefinitionService
 */
public interface DefinitionService extends Serializable {

	/**
	 * Provides a {@link Stream} of highest revision of all definitions.
	 *
	 * @param <E>
	 *            the definition type
	 * @return the all definitions stream
	 */
	<E extends DefinitionModel> Stream<E> getAllDefinitions();

	/**
	 * Gets the all definitions that are applicable for the given instance type
	 *
	 * @param instanceType
	 *            the instance type to match the definitions for
	 * @return the all definitions
	 */
	Stream<DefinitionModel> getAllDefinitions(InstanceType instanceType);

	/**
	 * Gets the all definitions filtered by instance category type
	 *
	 * @param category
	 *            the instance category type
	 * @return the all definitions that match the given category
	 * @see InstanceType#getCategory()
	 */
	default Stream<DefinitionModel> getAllDefinitions(String category) {
		return getAllDefinitions().filter(model -> nullSafeEquals(model.getType(), category));
	}

	/**
	 * Fetches the highest revision of all definitions of the given type.
	 *
	 * @param <E>
	 *            the definition type
	 * @param ref
	 *            the reference class to fetch the definitions for
	 * @return the all definitions
	 */
	<E extends DefinitionModel> List<E> getAllDefinitions(Class<?> ref);

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
	 * @deprecated change the reference argument to a object
	 */
	@Deprecated
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
	 * Gets the property.
	 *
	 * @param currentQName
	 *            the current q name
	 * @param pathElement
	 *            the path element
	 * @return the property
	 */
	default PropertyDefinition getProperty(String currentQName, PathElement pathElement) {
		return getProperty(currentQName, null, pathElement);
	}

	/**
	 * Gets the prototype of property of the given name.
	 *
	 * @param currentQName
	 *            the current q name
	 * @param pathElement
	 *            the path element
	 * @return the prototype
	 */
	default PrototypeDefinition getPrototype(String currentQName, PathElement pathElement) {
		return getPrototype(currentQName, null, pathElement);
	}

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
	Long getPropertyId(String propertyName, Long revision, PathElement pathElement, Serializable value);

	/**
	 * Gets the property id.
	 *
	 * @param propertyName
	 *            the property name
	 * @param pathElement
	 *            the path element
	 * @param value
	 *            the value
	 * @return the property id
	 */
	default Long getPropertyId(String propertyName, PathElement pathElement, Serializable value) {
		return getPropertyId(propertyName, null, pathElement, value);
	}

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
	 * Gets a {@link DataTypeDefinition} by it's name or the {@link String} representation of his Java class name
	 * returned by {@link DataTypeDefinition#getJavaClassName()}.<br>
	 * <b>NOTE: </b> For optimization purposes the method will check if the given argument contains a dot character to
	 * determine if the given argument is class name, so the method will not find a class that is defined in the default
	 * package! <br>
	 * The method also accepts a full URI defined in the <code>uri</code> tag in the type definition.
	 *
	 * @param key
	 *            the key to search for data type definition. It could be: db id, short name, class, uri.
	 * @return the found definition or <code>null</code> if not such definition exists
	 */
	DataTypeDefinition getDataTypeDefinition(Object key);

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
	Map<String, Serializable> filterProperties(DefinitionModel model, Map<String, Serializable> properties,
			DisplayType displayType);

	/**
	 * Gets the definition for the given instance.
	 *
	 * @param <D>
	 *            the definition type
	 * @param instance
	 *            the instance
	 * @return the instance definition if found valid definition
	 */
	<D extends DefinitionModel> D getInstanceDefinition(Instance instance);

	/**
	 * Retrieves all object properties that are defined in {@link DefinitionModel} for given instance.
	 *
	 * @param instance
	 *            the instance for which should be returned object properties
	 * @return stream of all object properties defined for given instance
	 */
	Stream<PropertyDefinition> getInstanceObjectProperties(Instance instance);

	/**
	 * Detects the type of the given serializable object and based on the detected type the method should return a
	 * definition that could be used for property persistence. If argument is null or not supported then the method will
	 * return <code>null</code>.
	 * <p>
	 * <b>NOTE: </b> This method should not be used for common properties that have a definition. This is only for
	 * properties that does not have a definition or are not a part of one but need to be persisted anyway.
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
	MutableDefinitionService getMutableInstance();

	/**
	 * Retrieve a {@link DefinitionModel} by it's identifier.
	 *
	 * @param id
	 *            Definition identifier.
	 * @return The found {@link DefinitionModel} or {@code null} if no definition with such identifier exists.
	 */
	DefinitionModel find(String id);

	/**
	 * Returns unique identifier for definition, build from definition type and id. Primary used when the definitions
	 * are added to the semantic.
	 *
	 * @param model
	 *            the definition for which will be build identifier
	 * @return unique definition identifier or null if the model is null or its identifier or type are null
	 */
	String getDefinitionIdentifier(DefinitionModel model);

	/**
	 * Gets the default definition id for the given instance. For instance that have a definition identifier the method
	 * will just return that identifier.
	 *
	 * @param target
	 *            the target object used for determining the default definition for
	 * @return the default definition id or <code>null</code> if no such exists
	 */
	String getDefaultDefinitionId(Instance target);
}
