package com.sirma.sep.model.management.operation;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;
import static com.sirma.sep.model.management.converter.ModelConverterUtilities.normalizeLabelsMap;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.ModelAttribute;
import com.sirma.sep.model.management.ModelAttributeType;
import com.sirma.sep.model.management.Models;
import com.sirma.sep.model.management.exception.ChangeSetCollisionException;

/**
 * Model change operation that handles map attributes. It supports checking single map key-value pairs for collisions
 * without affecting non changed map entries.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 10/08/2018
 */
public class ModifyMapAttributeChangeSetOperation implements ModelChangeSetOperation<ModelAttribute> {
	@Override
	public boolean isAccepted(Object target) {
		return target instanceof ModelAttribute;
	}

	@Override
	public boolean validate(Models models, ModelAttribute targetNode, ModelChangeSet changeSet) {
		ModelChangeSetOperation.super.validate(models, targetNode, changeSet);
		// add other validations needed for this change operation
		checkForValueCollisions(targetNode, changeSet);
		return !nullSafeEquals(targetNode.getValue(), changeSet.getNewValue());
	}

	private void checkForValueCollisions(ModelAttribute targetNode, ModelChangeSet changeSet) {
		Map<String, String> currentValue = getAsMap(targetNode, targetNode.getValue());
		Map<String, String> oldValue = getAsMap(targetNode, changeSet.getOldValue());
		Map<String, String> newValue = getAsMap(targetNode, changeSet.getNewValue());

		String errors = currentValue.entrySet().stream().map(entry -> {
			String key = entry.getKey();
			String currentData = entry.getValue();
			String oldData = oldValue.get(key);
			String newData = newValue.get(key);
			if ((oldData != null || newData != null) && !isValueChangePermitted(currentData, oldData, newData)) {
				return " Expected value for key '" + key + "' '" + currentData + "' got '" + oldData + "'";
			}
			return "";
		}).filter(StringUtils::isNotBlank).collect(Collectors.joining("; "));

		if (!errors.isEmpty()) {
			throw new ChangeSetCollisionException(changeSet.getPath().tail().getValue(), errors);
		}
	}

	private static boolean isValueChangePermitted(Object value, Object oldValue, Object newValue) {
		return nullSafeEquals(value, oldValue) || nullSafeEquals(value, newValue);
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> getAsMap(ModelAttribute targetNode, Object value) {
		if (value == null) {
			return new HashMap<>();
		} else if (value instanceof Map) {
			if (ModelAttributeType.LABEL_ATTRIBUTES.contains(targetNode.getType())) {
				return normalizeLabelsMap((Map) value);
			}
			return (Map<String, String>) value;
		} else if (value instanceof String) {
			Map<String, String> map = new HashMap<>();
			map.put("en", (String) value);
			return map;
		}
		throw new ChangeSetValidationFailed(
				"Invalid value type. Expected java.util.Map but got " + value.getClass().getName() + " for value "
						+ value);
	}

	private Map<String, String> getAsNewMap(ModelAttribute targetNode, Object value) {
		return new HashMap(getAsMap(targetNode, value));
	}

	@Override
	public Stream<ModelChangeSetInfo> applyChange(Models models, ModelAttribute targetNode, ModelChangeSet changeSet) {
		Map<String, String> currentValue = getCurrentValue(targetNode);
		Map<String, String> oldValue = getAsMap(targetNode, changeSet.getOldValue());
		Map<String, String> newValue = getAsMap(targetNode, changeSet.getNewValue());

		oldValue.keySet().forEach(currentValue::remove);
		currentValue.putAll(newValue);

		targetNode.setValue(currentValue);
		if (currentValue.isEmpty()) {
			return Stream.of(RestoreModelAttributeChangeSetOperation.createChange(targetNode.getPath()));
		}
		return Stream.empty();
	}

	private Map<String, String> getCurrentValue(ModelAttribute targetNode) {
		Map<String, String> currentValue = getAsMap(targetNode, targetNode.getValue());
		if (currentValue.isEmpty()) {
			// Means the attribute is probably overriding -> use inherited value as current before merging (if such exists in the hierarchy)
			ModelNode owningNode = targetNode.getContext();
			if (owningNode.hasParent()) {
				// Hierarchy resolve, current node could override across models
				return owningNode.getParentReference()
						.findAttribute(targetNode.getName())
						.map(inherited -> getAsNewMap(targetNode, inherited.getValue()))
						.orElse(currentValue);
			}
		}
		return currentValue;
	}

	@Override
	public String getName() {
		return "modifyMapAttribute";
	}
}
