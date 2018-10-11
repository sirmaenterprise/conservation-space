package com.sirma.itt.seip.wildfly;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;
import static org.jboss.as.controller.client.helpers.ClientConstants.READ_RESOURCE_OPERATION;

import java.util.Optional;
import java.util.function.Function;

import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;

/**
 * Utility class for common Wildfly CLI operations
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/10/2017
 */
class WildflyCLIUtil {

	private final WildflyControllerService controller;

	/**
	 * Instantiate the utility class for the given controller
	 *
	 * @param controller the controller to set
	 */
	WildflyCLIUtil(WildflyControllerService controller) {
		this.controller = controller;
	}

	/**
	 * Add write attribute operation to the given node for the given name and value
	 *
	 * @param node the target node
	 * @param name the name of the attribute to set
	 * @param value the value to set
	 */
	static void writeAttribute(ModelNode node, String name, String value) {
		node.get(ClientConstants.OP).set(ClientConstants.WRITE_ATTRIBUTE_OPERATION);
		node.get(ClientConstants.NAME).set(name);
		node.get(ClientConstants.VALUE).set(value);
	}

	/**
	 * Add remove operation for the given attribute name
	 *
	 * @param node the target node
	 * @param name the property name to set for removal
	 */
	static void removeAttribute(ModelNode node, String name) {
		node.get(ClientConstants.OP).set(ClientConstants.REMOVE_OPERATION);
		node.get(ClientConstants.NAME).set(name);
	}

	/**
	 * Check if the given mode represents a success operation
	 *
	 * @param result the node from a {@link WildflyControllerService#execute(ModelNode)}
	 * @return true if successful
	 */
	static boolean isSuccessful(ModelNode result) {
		return ClientConstants.SUCCESS.equals(result.get(ClientConstants.OUTCOME).asString());
	}

	/**
	 * Read a resource at the given address
	 *
	 * @param address the address to read
	 * @return the the node that points at the given address if found
	 */
	Optional<ModelNode> readResource(ModelNode address) {
		ModelNode root = new ModelNode();
		root.get(OP).set(READ_RESOURCE_OPERATION);
		root.get(OP_ADDR).set(address);
		ModelNode result;
		try {
			result = controller.execute(root);
		} catch (RollbackedException e) {
			throw new RollbackedRuntimeException(e);
		}
		if (!isSuccessful(result)) {
			return Optional.empty();
		}
		return Optional.of(result);
	}

	/**
	 * Reads the attribute value located at the given address
	 *
	 * @param address the base address where the property should be located
	 * @param propertyName the property name to read
	 * @return the node representing the found value. If value is undefined the method will return empty optional
	 */
	Optional<ModelNode> readResourceAttribute(ModelNode address, String propertyName) {
		ModelNode root = new ModelNode();
		root.get(OP).set(ClientConstants.READ_ATTRIBUTE_OPERATION);
		root.get(OP_ADDR).set(address);
		root.get(ClientConstants.NAME).set(propertyName);
		ModelNode result;
		try {
			result = controller.execute(root);
		} catch (RollbackedException e) {
			throw new RollbackedRuntimeException(e);
		}
		if (!isSuccessful(result)) {
			return Optional.empty();
		}
		ModelNode resultValue = result.get(ClientConstants.RESULT);
		if (resultValue.isDefined()) {
			return Optional.of(resultValue);
		}
		return Optional.empty();
	}

	/**
	 * Checks if there is something bound to the given resource location and returns true if there is NONE
	 *
	 * @param address the address to check
	 * @return true if there is no resource located at the given address
	 */
	boolean isAddressUndefined(ModelNode address) {
		return !readResource(address).isPresent();
	}

	/**
	 * Add the given step to the composite operation if the step address points to not bound resource
	 *
	 * @param steps the composite operation
	 * @param step the step operation to add if the step address is not bound
	 */
	void addIfNotAlreadyDefined(ModelNode steps, ModelNode step) {
		ModelNode address = step.get(OP_ADDR);
		if (isAddressUndefined(address)) {
			steps.add(step);
		}
	}

	/**
	 * Checks if the property value is present at the given address and generates a change in order to add/update or
	 * remove the current value depending on the given value parameter
	 *
	 * @param propertyName the property name to check for changes
	 * @param propertyValue the property value that should be set
	 * @param addressBuilder the builder function for the property address
	 * @return the model node that if executed will change the current value to the one given to the method.
	 */
	Optional<ModelNode> checkOrUpdateValue(String propertyName, String propertyValue,
			Function<ModelNode, ModelNode> addressBuilder) {
		ModelNode modelNode = new ModelNode();
		ModelNode address = addressBuilder.apply(modelNode);

		Optional<ModelNode> node = readResourceAttribute(address, propertyName);

		if (node.isPresent()) {
			if (!nullSafeEquals(propertyValue, node.get().asString())) {
				if (propertyValue == null) {
					//removeProperty
					removeAttribute(modelNode, propertyName);
					return Optional.of(modelNode);
				} else {
					// update value
					writeAttribute(modelNode, propertyName, propertyValue);
					return Optional.of(modelNode);
				}
			}
		} else if (propertyValue != null) {
			writeAttribute(modelNode, propertyName, propertyValue);
			return Optional.of(modelNode);
		}
		return Optional.empty();
	}
}
