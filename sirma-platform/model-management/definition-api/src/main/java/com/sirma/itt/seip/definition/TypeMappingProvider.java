package com.sirma.itt.seip.definition;

import java.util.List;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Service that provides means to retrieve a concrete type of definition, instance or. {@link DataTypeDefinition} based
 * on the given {@link com.sirma.itt.seip.domain.definition.AllowedChildDefinition#getType()}. The provider must collects
 * all information from the extension {@link AllowedChildTypeMappingExtension} and provides common access to it. <br>
 *
 * @author BBonev
 */
public interface TypeMappingProvider {

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

	/**
	 * Filters out the allowed definitions for user onto given list of {@link DefinitionModel} bbanchev: may find better
	 * place for this method
	 * 
	 * @param <D>
	 *            the definition models
	 * @param model
	 *            is the list ot models to filter
	 * @return the filtered list (might be the same)
	 */
	<D extends DefinitionModel> List<D> filterAllowedDefinitions(List<D> model);
}
