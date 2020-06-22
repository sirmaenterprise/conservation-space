/**
 * Calls TypeConverter to convert the given value to the given Java class.
 * @param type the Java class to convert to.
 * @param value the value to convert
 * @returns converted value to the destination type or null if any of the arguments are missing
 */
function convert(type, value) {
	if (!type || !value) {
		return;
	}
	return converter["convert(java.lang.Class,java.lang.Object)"](type.class, value);
}

/**
 * Creates new instance reference from string type and/or id. If only the first argument is passed it should be in JSON format that will include the actual type and id of the needed instance/reference.
 * @param type the instance type or the JSON script representing the instance reference
 * @param id the instance id. Optional if first argument is JSON. If passed with JSON will override the passed id.
 * @returns InstanceReference object
 */
function toReference(type, id) {
	var ref = convert(com.sirma.itt.seip.domain.instance.InstanceReference, type);
	if (id && ref) {
		ref.id = id;
	}
	return ref;
}

/**
 * Convert the given instance to script node instance using the type converter.
 * @param instance to pass
 * @returns a ScriptNode instance
 */
function toNode(instance) {
	if (!instance) {
		return;
	}
	return convert(com.sirma.itt.seip.script.ScriptInstance, instance);
}

/**
 * Instantiate instance by type and id. The returned instance will be a script node or null if not found.
 * @param type instance type
 * @param id the instance id
 * @returns a script node instance or null
 */
function toInstance(type, id) {
	return toNode(toReference(type, id).toInstance());
}

/**
 * Convert the given parameter to URI object
 * @param uri to item to convert
 * @returns the converted instance
 */
function toUri(uri) { return convert(com.sirma.itt.seip.Uri, uri); }