package com.sirma.itt.emf.security.model;

import java.io.Serializable;

import com.sirma.itt.emf.domain.model.Identity;

/**
 * Interface that represents a concrete user action or operation.
 * <p>
 * TODO action-button-template.xhtml
 * 
 * @author BBonev
 */
public interface Action extends Sealable, Identity, Serializable {

	/**
	 * Gets the action id that uniquely identifies the action.
	 * 
	 * @return the action id
	 */
	String getActionId();

	/**
	 * Gets the label.
	 * 
	 * @return the label
	 */
	String getLabel();

	/**
	 * Checks if is disabled.
	 * 
	 * @return true, if is disabled
	 */
	boolean isDisabled();

	/**
	 * Gets the disabled reason.
	 * 
	 * @return the disabled reason
	 */
	String getDisabledReason();

	/**
	 * Gets the confirmation message.
	 * 
	 * @return the confirmation message
	 */
	String getConfirmationMessage();

	/**
	 * Gets the icon image path.
	 * 
	 * @return the icon image path
	 */
	String getIconImagePath();

	/**
	 * Gets the javascript onclick attribute value.
	 * 
	 * @return the onclick
	 */
	String getOnclick();

	/**
	 * Checks if current operation is immediate.
	 * 
	 * @return true, if is immediate
	 */
	boolean isImmediateAction();
}
