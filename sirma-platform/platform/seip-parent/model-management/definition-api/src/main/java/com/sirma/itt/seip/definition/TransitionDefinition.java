package com.sirma.itt.seip.definition;

import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.Purposable;
import com.sirma.itt.seip.domain.definition.Conditional;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.label.Displayable;
import com.sirma.itt.seip.domain.security.Action;

/**
 * Defines a definition of a transition into the workflow, where to go after completing the current task.
 *
 * @author BBonev
 */
public interface TransitionDefinition
		extends PathElement, Displayable, Conditional, DefinitionModel, Action, Ordinal, Purposable {

	/**
	 * Gets the value of the eventId property.
	 *
	 * @return possible object is {@link String }
	 */
	String getEventId();

	/**
	 * Gets the task definition.
	 *
	 * @return the task definition
	 */
	DefinitionModel getOwningDefinition();

	/**
	 * Gets the next secondary state.
	 *
	 * @return the next secondary state
	 */
	String getNextSecondaryState();

	/**
	 * Gets the next primary state.
	 *
	 * @return the next primary state
	 */
	String getNextPrimaryState();

	/**
	 * Checks if is default transition.
	 *
	 * @return true, if is default transition
	 */
	Boolean getDefaultTransition();

	/**
	 * Checks if the current transition operation is immediate. This is valid for transitions different from the default
	 * (<code>null</code>) purpose
	 *
	 * @return true, if is immediate
	 * @see #getPurpose()
	 */
	@Override
	boolean isImmediateAction();

	/**
	 * Gets the transition purpose. This field could be used for modifying the default logic of transitions as a task
	 * specific operations. If the value is <code>null</code> then the behavior is the default intended. Any non
	 * <code>null</code> value will be treaded accordingly.
	 *
	 * @return the transition purpose
	 */
	@Override
	String getPurpose();

	/**
	 * Getter method for ownerPrefix.
	 *
	 * @return the ownerPrefix
	 */
	String getOwnerPrefix();

	/**
	 * Gets the disabled reason.
	 *
	 * @return the disabled reason
	 */
	String getDisabledReasonId();

	/**
	 * Gets the confirmation message.
	 *
	 * @return the confirmation message
	 */
	String getConfirmationMessageId();

	/**
	 * Provides the group to which the current transition is bound.
	 *
	 * @return the group for the particular transition
	 */
	@Override
	String getGroup();

}