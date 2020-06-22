package com.sirma.itt.seip.instance.actions;

/**
 * Collection of default properties used to implement operations.
 *
 * @author BBonev
 */
public interface InstanceOperationProperties {

	/**
	 * Represents the target instance on which operation is executed. Should be of type
	 * {@link com.sirma.itt.seip.domain.instance.Instance}.
	 */
	String INSTANCE = "instance";

	/**
	 * Represents the source instance on which operation is executed. Should be of type
	 * {@link com.sirma.itt.seip.domain.instance.Instance}.
	 */
	String SOURCE_INSTANCE = "source_instance";

	/**
	 * Represents the operation object used to trigger the operation execution. Should be of type
	 * {@link com.sirma.itt.seip.instance.state.Operation}.
	 */
	String OPERATION = "operation";

	/** Represents an list of instances contained in an {@link java.lang.reflect.Array} */
	String INSTANCE_ARRAY = "instance_array";

	/**
	 * Defines a property that represent an user executed operation. Some actions could support to execute an operation
	 * and the actual user operation is passed via this parameter. Should be of type
	 * {@link com.sirma.itt.seip.instance.state.Operation}. If operation fires some events they should be fired using
	 * the operation object provided from this property if available.
	 */
	String ACTUAL_USER_OPERATION = "actualOperation";
}
