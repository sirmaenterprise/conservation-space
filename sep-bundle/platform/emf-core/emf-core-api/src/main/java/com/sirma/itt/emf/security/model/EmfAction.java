package com.sirma.itt.emf.security.model;

import java.io.Serializable;

import com.sirma.itt.emf.domain.model.Identity;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Default {@link Action} implementation
 *
 * @author BBonev
 */
public class EmfAction implements Action, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6923482971961494803L;

	/** The action id. */
	private final String actionId;

	/**
	 * The action name string to be displayed in the web page.
	 */
	private String label;

	/**
	 * If this action should be disabled in the web page.
	 */
	private boolean isDisabled = false;

	/**
	 * If this action is disabled, then the reason may be provided and can be visualized in the web
	 * page.
	 */
	private String disabledReason;

	/**
	 * Message that if provided, will be displayed in confirmation window before this action to be
	 * executed.
	 */
	private String confirmationMessage;

	/** The icon image path. */
	private String iconImagePath;

	/** The sealed. */
	private boolean sealed = false;

	/** The label provider. */
	private LabelProvider labelProvider;

	/** The onclick. */
	private String onclick;

	/**
	 * If is set to be true, then the action should be immediate and the button should submit the
	 * form nevertheless there are uncompleted fields on the form.
	 */
	private boolean immediate = false;

	/**
	 * Instantiates a new cmf action.
	 *
	 * @param actionId
	 *            the action id
	 */
	public EmfAction(String actionId) {
		this.actionId = actionId;
		if (actionId == null) {
			throw new IllegalArgumentException("Action Id cannot be null");
		}
	}

	/**
	 * Instantiates a new cmf action.
	 *
	 * @param id
	 *            the id
	 * @param labelProvider
	 *            the label provider
	 */
	public EmfAction(String id, LabelProvider labelProvider) {
		this(id);
		this.labelProvider = labelProvider;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getActionId() {
		return actionId;
	}

	/**
	 * Getter method for isDisabled.
	 *
	 * @return the isDisabled
	 */
	@Override
	public boolean isDisabled() {
		return isDisabled;
	}

	/**
	 * Getter method for disabledReason.
	 *
	 * @return the disabledReason
	 */
	@Override
	public String getDisabledReason() {
		return getLabelInternal(disabledReason);
	}

	/**
	 * Getter method for confirmationMessage.
	 *
	 * @return the confirmationMessage
	 */
	@Override
	public String getConfirmationMessage() {
		return getLabelInternal(confirmationMessage);
	}

	/**
	 * Getter method for iconImagePath.
	 *
	 * @return the iconImagePath
	 */
	@Override
	public String getIconImagePath() {
		return iconImagePath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLabel() {
		return getLabelInternal(label);
	}

	/**
	 * Gets the label internal.
	 *
	 * @param l
	 *            the l
	 * @return the label internal
	 */
	private String getLabelInternal(String l) {
		if (labelProvider == null) {
			return l;
		}
		String local = labelProvider.getLabel(l);
		if (local == null) {
			local = labelProvider.getValue(l);
		}
		if ((local != null) && (local.endsWith(".confirm") || local
				.endsWith(".disabled.reason"))) {
			return null;
		}
		return local;
	}

	/**
	 * Setter method for isDisabled.
	 *
	 * @param isDisabled
	 *            the isDisabled to set
	 */
	public void setDisabled(boolean isDisabled) {
		if (!sealed) {
			this.isDisabled = isDisabled;
		}
	}

	/**
	 * Setter method for disabledReason.
	 *
	 * @param disabledReason
	 *            the disabledReason to set
	 */
	public void setDisabledReason(String disabledReason) {
		if (!sealed) {
			this.disabledReason = disabledReason;
		}
	}

	/**
	 * Setter method for confirmationMessage.
	 *
	 * @param confirmationMessage
	 *            the confirmationMessage to set
	 */
	public void setConfirmationMessage(String confirmationMessage) {
		if (!sealed) {
			this.confirmationMessage = confirmationMessage;
		}
	}

	/**
	 * Setter method for iconImagePath.
	 *
	 * @param iconImagePath
	 *            the iconImagePath to set
	 */
	public void setIconImagePath(String iconImagePath) {
		if (!sealed) {
			this.iconImagePath = iconImagePath;
		}
	}

	/**
	 * Setter method for label.
	 *
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		if (!sealed) {
			this.label = label;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOnclick() {
		return this.onclick;
	}

	/**
	 * Setter method for onclick.
	 *
	 * @param onclick
	 *            the onclick to set
	 */
	public void setOnclick(String onclick) {
		if (!sealed) {
			this.onclick = onclick;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EmfAction [actionId=");
		builder.append(actionId);
		builder.append(", label=");
		builder.append(label);
		builder.append(", isDisabled=");
		builder.append(isDisabled);
		builder.append(", disabledReason=");
		builder.append(disabledReason);
		builder.append(", confirmationMessage=");
		builder.append(confirmationMessage);
		builder.append(", iconImagePath=");
		builder.append(iconImagePath);
		builder.append(", onclick=");
		builder.append(onclick);
		builder.append(", sealed=");
		builder.append(sealed);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public boolean isSealed() {
		return sealed;
	}

	@Override
	public void seal() {
		sealed = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return (((actionId == null) ? 0 : actionId.hashCode()) + 31) * 31;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Identity)) {
			return false;
		}
		Identity other = (Identity) obj;
		if (actionId == null) {
			if (other.getIdentifier() != null) {
				return false;
			}
		} else if (!actionId.equals(other.getIdentifier())) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isImmediateAction() {
		return immediate;
	}

	/**
	 * Setter method for immediate.
	 *
	 * @param immediate
	 *            the immediate to set
	 */
	public void setImmediate(boolean immediate) {
		if (!sealed) {
			this.immediate = immediate;
		}
	}

	@Override
	public String getIdentifier() {
		return getActionId();
	}

	@Override
	public void setIdentifier(String identifier) {
		// nothing to do
	}

}
