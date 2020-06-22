package com.sirma.itt.seip.instance.script;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.script.GlobalBindingsExtension;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension which provides functionality for copying properties from source instance to destination
 * instance.
 *
 * @author A. Kunchev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 210)
public class OutjectPropertiesScriptProvider implements GlobalBindingsExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String OUTJECT_PROPERTIES_JS = "outject-properties.js";

	private static final String OUTJECT = "outject";

	private static final String OUTJECT_NOT_EMPTY = "outjectNotEmpty";

	@Inject
	private InstanceService instanceService;

	@Inject
	private DefinitionService definitionService;

	@Override
	public Map<String, Object> getBindings() {
		return Collections.singletonMap(OUTJECT, this);
	}

	@Override
	public Collection<String> getScripts() {
		return ResourceLoadUtil.loadResources(getClass(), OUTJECT_PROPERTIES_JS);
	}

	/**
	 * Copies properties from given source instance to given destination instance or source owning instance if the
	 * destination is <b>null</b>. There are options to copy properties from the source instance, no matter if they are
	 * empty or null, and option where the source properties are copied only, if there are not empty or null. This
	 * options could be combined. The properties that should be copied are passed as string in JSON format. After the
	 * properties are copied to the destination, the instance is updated.
	 * <p>
	 * Properties object example:
	 * <p>
	 * <pre>
	 * {
	 *  outject         : ["property1", "property2", ..., "propertyN"],
	 *  outjectNotEmpty : ["property1", "property2", ..., "propertyN"]
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param source the source ScriptNode object, which contains the source instance from which the properties will be
	 * copied
	 * @param destination the destination ScriptNode object, which contains the destination instance to which the properties
	 * will be copied. Can be <b>null</b>, then for the destination instance will be get the owning instance
	 * of the source (source instance direct parent)
	 * @param propertiesObject the properties, which should be copied as string in JSON format (the example above)
	 * @return <code><b>false</b> - </code> when source is <code>null</code>, when propertiesObject is null or empty,
	 * when there is no instance to where the properties should be outjected or there is no instance from which
	 * the properties should be outjected. <br />
	 * <code><b>true</b> - </code> when the operation is successful
	 */
	public boolean outjectProperties(ScriptNode source, ScriptNode destination, String propertiesObject) {
		if (source == null || StringUtils.isBlank(propertiesObject)) {
			LOGGER.warn("Missing source or properties. Check your input arguments!");
			return false;
		}

		Instance current = source.getTarget();
		Instance parent = destination != null ? destination.getTarget() : InstanceUtil.getDirectParent(current);

		if (parent == null || current == null) {
			LOGGER.warn("Missing instance from/to which the properties should be outjected!");
			return false;
		}

		JsonObject propertiesJson = JSON.readObject(propertiesObject, Function.identity());

		List<String> outject = getListOfStrings(propertiesJson, OUTJECT);
		List<String> outjectNotEmpty = getListOfStrings(propertiesJson, OUTJECT_NOT_EMPTY);
		DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(current);

		if (!outject.isEmpty()) {
			Map<String, Serializable> currentInstanceProperties = outject.stream()
					.filter(current::isValueNotNull)
					.filter(propertyName -> !outjectNotEmpty.contains(propertyName))
					.filter(propertyName -> !isUniqueProperty(instanceDefinition, propertyName))
					.map(Pair.from(Function.identity(), current::get))
					.collect(Pair.toMap());
			parent.addAllProperties(currentInstanceProperties);
		}
		outjectNotEmptyProperties(current, parent, outjectNotEmpty);
		instanceService.save(parent, new Operation(ActionTypeConstants.EDIT_DETAILS));
		return true;
	}

	/**
	 * Checks if a property with name <code>propertyName</code> is defined as unique.
	 *
	 * @param instanceDefinition - definition where property is defined.
	 * @param propertyName - the name of property which have to be checked.
	 * @return true if property with name <code>propertyName</code> is defined as unique.
	 */
	private static boolean isUniqueProperty(DefinitionModel instanceDefinition, String propertyName) {
		Optional<PropertyDefinition> propertyDefinition = instanceDefinition.getField(propertyName);
		if (propertyDefinition.filter(PropertyDefinition.isUniqueProperty()).isPresent()) {
			LOGGER.warn("Property with name: \"{}\" is registered as unique field and will be skipped of outjection!",
					propertyName);
			return true;
		}
		return false;
	}

	/**
	 * Fetch {@link JsonArray} with key <code>arraysKey</code> from <code>json</code> and convert it to
	 * list with strings. If {@link JsonArray} with key <code>arraysKey</code> not exist a empty list will be returned.
	 *
	 * @return fetched {@link JsonArray} as list of strings.
	 */
	private static List<String> getListOfStrings(JsonObject json, String arraysKey) {
		JsonArray array = JSON.getArray(arraysKey, json);
		if (array == null || array.isEmpty()) {
			return Collections.emptyList();
		}

		return array.getValuesAs(JsonString.class)
				.stream()
				.filter(Objects::nonNull)
				.filter(jsonValue -> !StringUtils.isBlank(jsonValue.getString()))
				.map(JsonString::getString)
				.collect(Collectors.toList());
	}

	/**
	 * Copies only not null, not empty properties from the current instance to parent instance. Which properties should
	 * be outjected is shown in the array outjectNotEmpty argument. The results for every property is logged.
	 *
	 * @param current the instance from which the properties values are get
	 * @param parent the parent instance to which the properties are add
	 * @param outjectNotEmpty array with the properties which should be transfered, if they are not null or empty
	 */
	private static void outjectNotEmptyProperties(Instance current, Instance parent, List<String> outjectNotEmpty) {
		if (outjectNotEmpty.isEmpty()) {
			return;
		}

		for (String property : outjectNotEmpty) {
			boolean copied;
			Serializable value = current.get(property);
			if (value instanceof String) {
				copied = parent.addIfNotNullOrEmpty(property, (String) value);
			} else {
				copied = parent.addIfNotNull(property, value);
			}

			if (!copied) {
				LOGGER.warn("[{}] was not copied! The property value is null or empty,"
						+ " or the parent doesn't have this property.", property);
			}
		}
	}
}
