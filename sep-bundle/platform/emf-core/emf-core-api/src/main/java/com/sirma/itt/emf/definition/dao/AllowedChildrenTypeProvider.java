package com.sirma.itt.emf.definition.dao;

import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Service that provides means to retrieve a concrete type of definition, instance or.
 * {@link DataTypeDefinition} based on the given
 * {@link com.sirma.itt.emf.definition.model.AllowedChildDefinition#getType()}. The provider must
 * collects all information from the extension {@link AllowedChildTypeMappingExtension} and provides
 * common access to it. <br>
 * REVIEW: name of interface is not consistent with the functionality
 * 
 * @author BBonev
 */
public interface AllowedChildrenTypeProvider {

	/**
	 * Gets the definition class that corresponds to the given type.
	 * 
	 * @param type
	 *            the type
	 * @return the definition
	 */
	Class<? extends DefinitionModel> getDefinitionClass(String type);

	/**
	 * Gets the instance class that corresponds to the given type.
	 * 
	 * @param type
	 *            the type
	 * @return the instance class
	 */
	Class<? extends Instance> getInstanceClass(String type);

	/**
	 * Gets the data type name that corresponds to the given type.
	 * 
	 * @param type
	 *            the type
	 * @return the data type name
	 */
	String getDataTypeName(String type);

	/**
	 * Gets the data type definition that corresponds to the given type.
	 * 
	 * @param type
	 *            the type
	 * @return the data type
	 */
	DataTypeDefinition getDataType(String type);

	/**
	 * Gets the child type by instance class.
	 * 
	 * @param clazz
	 *            the class to get the type for
	 * @return the type by instance
	 */
	String getTypeByInstance(Class<? extends Instance> clazz);
}
