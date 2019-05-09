package com.sirma.sep.model.management.operation;

import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.stream.Stream;

import com.sirma.sep.model.management.ModelAttribute;
import com.sirma.sep.model.management.Models;
import com.sirma.sep.model.management.exception.ChangeSetCollisionException;

/**
 * Model change set operation that can handle model attribute operations.<br>
 * The operation performs a verification if the previous value is the same as the one in the change set if not fail to
 * execute the action.<br>
 * If the new value is not set the implementation assumes the attribute for removed so that the parent value will be
 * used if any.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/07/2018
 */
public class ModifyAttributeChangeSetOperation implements ModelChangeSetOperation<ModelAttribute> {
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
		Object currentValue = getOrDefault(targetNode.getValue(), targetNode.getMetaInfo().getDefaultValue());
		Object expectedValue = changeSet.getOldValue();
		Object newValue = changeSet.getNewValue();
		if (targetNode.getValue() != null && !isValueChangePermitted(currentValue, expectedValue, newValue)) {
			String message = "Detected value collision for node " + changeSet.getPath().prettyPrint() + " for attribute "
					+ targetNode.getName() + " expected value=" + currentValue + " got value=" + expectedValue;
			throw new ChangeSetCollisionException(changeSet.getPath().tail().getValue(), message);
		}
	}

	private static boolean isValueChangePermitted(Object value, Object oldValue, Object newValue) {
		return nullSafeEquals(value, oldValue) || nullSafeEquals(value, newValue);
	}

	@Override
	public Stream<ModelChangeSetInfo> applyChange(Models models, ModelAttribute targetNode, ModelChangeSet changeSet) {
		if (changeSet.getNewValue() == null) {
			return Stream.of(RestoreModelAttributeChangeSetOperation.createChange(changeSet));
		}
		Object newValue = changeSet.getNewValue();
		targetNode.getContext().addAttribute(targetNode.getName(), newValue);
		return Stream.empty();
	}

	@Override
	public String getName() {
		return "modifyAttribute";
	}
}
