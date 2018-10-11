package com.sirma.itt.seip.definition;

import java.util.List;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Defines methods that will help the calculating the allowed children of a concrete instance type.
 *
 * @param <I>
 *            the concrete instance type
 * @author BBonev
 */
public interface AllowedChildrenProvider<I extends Instance> {

	/**
	 * Gets the definition model for the given instance.
	 *
	 * @param <T>
	 *            the definition type
	 * @param instance
	 *            the instance
	 * @return the definition
	 */
	<T extends DefinitionModel> T getDefinition(I instance);

	/**
	 * Gets the concrete definition class that is represented by the given type.
	 *
	 * @param <T>
	 *            the definition type
	 * @param type
	 *            the type of the child
	 * @return the definition class
	 */
	<T extends DefinitionModel> Class<T> getDefinition(String type);

	/**
	 * Calculate active instance based on the given type and the parent instance.
	 *
	 * @param instance
	 *            the instance
	 * @param type
	 *            the type of the child
	 * @return true, if calculation is needed
	 */
	boolean calculateActive(I instance, String type);

	/**
	 * Gets the active instance based on the parent instance and the concrete child type. The method is called only when
	 * {@link #calculateActive(Instance, String)} returns <code>true</code> for the same arguments.
	 *
	 * @param <A>
	 *            the child instance type
	 * @param instance
	 *            the parent instance
	 * @param type
	 *            the type of the child
	 * @return the list of active instances if any
	 */
	<A extends Instance> List<A> getActive(I instance, String type);

	/**
	 * Gets the all definitions that are applicable for the given type and target instance.
	 *
	 * @param <T>
	 *            the definition type
	 * @param instance
	 *            the instance
	 * @param type
	 *            the type
	 * @return the all definitions
	 */
	<T extends DefinitionModel> List<T> getAllDefinitions(I instance, String type);

}
