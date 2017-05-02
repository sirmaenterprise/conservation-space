
function evalExp(expression, node) {
	if (!expression) {
		return;
	}
	if (node) {
		return _eval_.evalOverNode(expression, node);
	}
	return _eval_.eval(expression);
}

/**
 * Get a property from the root instance
 * @param name the property name
 * @returns value found in the or empty string if root instance is not present
 */
function get(name) {
	if (root) {
		return root.get(name);
	}
	return null;
}

/**
 * Sets a value to property identified by the given name in the root instance if present
 * @param name the name of the property to set
 * @param value the value to set. Note that this should be a compatible Java object
 * 	because no conversion is done at this point
 */
function set(name, value) {
	if (root) {
		root.add(name, value);
	}
}

/**
 * Gets a property from parent context instance associated with the current root instance
 * @param name the name of the property to fetch
 * @returns the value found or empty string
 */
function getFromContext(name) {
	if (root) {
		var context = com.sirma.itt.seip.domain.util.InstanceUtil.getDirectParent(root.getTarget());
		if (context) {
			return context.get(name);
		}
	}
	return null;
}