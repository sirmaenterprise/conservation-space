/**
 * Creates an instance of the given type from the given definition. If the
 * instance is create successfully a content is set using the template id
 * provided. If parent instance is passed the instance is created in the content
 * of the given parent. Note that the instance is not persisted and in order to
 * do that a save should be called on the instance.
 *
 * @param type
 *            the instance type to created. Example: document, object...
 * @param definition
 *            the definition id to use to initialize the created instance
 * @param operation
 *            the operation to execute
 * @param modifiable
 *            a boolean value that if <code>true</code> the instance should be
 *            not modifiable. This is achieved by making the instance a
 *            revision.
 * @param parent
 *            if the optional parent to assign to the instance
 * @returns a script node of the created instance.
 */
function create(type, definition, operation, parent) {
	var created;
	if (parent) {
		created = instance.createWithParent(type, definition, operation, parent);
	} else {
		created = instance.create(type, definition, operation);
	}
	return created;
}

/**
 * Copy the properties from the given source instance to the destination
 * instance. The property keys should be an array of property names to copy. The
 * method does not copy null values.
 *
 * @param source
 *            the source instance to copy the properties from
 * @param destination
 *            the destination instance to copy the properties to
 * @param keys
 *            the property names to copy. Single value could be passed as is. Multiple keys could be passed if array
 */
function copyProperties(source, destination, keys) {
	if (!source || !destination || !keys) {
		log.warn('Did not copy any properties because some of the arguments are not set: source['
						+ (!source ? 'NOT' : '')
						+ ' present], destination['
						+ (!destination ? 'NOT' : '')
						+ ' present], keys[' + (!keys ? 'NOT' : '') + ' present]');
		return;
	}
	if (typeof keys == 'string') {
		var value = source.getProperties().get(keys);
		if (value) {
			destination.getProperties().put(keys, value);
		}
	} else {
		for (var index in keys) {
			var property = keys[index];
			var value = source.getProperties().get(property);
			if (value) {
				destination.getProperties().put(property, value);
			}
		}
	}
}