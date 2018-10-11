/**
 *
 */
package com.sirma.itt.seip.testutil.mocks;

import javax.json.JsonObject;

import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * @author BBonev
 */
public class ActionMock implements Action {
	private static final long serialVersionUID = -7242764678550159752L;

	private final String actionId;
	private String label;
	private boolean isDisabled = false;
	private String disabledReason;
	private String confirmationMessage;
	private String iconImagePath;
	private boolean sealed = false;
	private LabelProvider labelProvider;
	private String onclick;
	private boolean immediate = false;
	private String purpose = "action";
	private boolean local;
	private JsonObject configuration;
	private String tooltip;
	private String actionPath;
	private String group;

	/**
	 * Instantiates a new action mock.
	 *
	 * @param actionId
	 *            the action id
	 */
	public ActionMock(String actionId) {
		this.actionId = actionId;
		if (actionId == null) {
			throw new IllegalArgumentException("Action Id cannot be null");
		}
	}

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
	 * @param labelId
	 *            the label id
	 * @return the label internal
	 */
	private String getLabelInternal(String labelId) {
		if (labelProvider == null) {
			return labelId;
		}
		String localLabel = labelProvider.getLabel(labelId);
		if (localLabel == null) {
			localLabel = labelProvider.getValue(labelId);
		}
		if (localLabel != null && (localLabel.endsWith(".confirm") || localLabel.endsWith(".disabled.reason"))) {
			return null;
		}
		return localLabel;
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
		return onclick;
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
		builder.append(", tooltip=");
		builder.append(tooltip);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSealed() {
		return sealed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void seal() {
		sealed = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return ((actionId == null ? 0 : actionId.hashCode()) + 31) * 31;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Identity)) {
			return false;
		}
		Identity other = (Identity) obj;
		return EqualsHelper.nullSafeEquals(getIdentifier(), other.getIdentifier());
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return getActionId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIdentifier(String identifier) {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPurpose() {
		return purpose;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	/**
	 * Check if this is local action or globally true. Default is false
	 *
	 * @return if this is local action
	 */
	@Override
	public boolean isLocal() {
		return local;
	}

	/**
	 * Set he local info
	 *
	 * @param local
	 *            the scope to set
	 */
	public void setLocal(boolean local) {
		this.local = local;
	}

	@Override
	public JsonObject getConfigurationAsJson() {
		return configuration;
	}

	/**
	 * @param configuration
	 *            the configuration to set
	 */
	public void setConfiguration(JsonObject configuration) {
		this.configuration = configuration;
	}

	/**
	 * Setter for tooltip
	 *
	 * @param tooltip
	 *            bundle identifier
	 */
	public void setTooltip(String tooltip) {
		if (!sealed) {
			this.tooltip = tooltip;
		}
	}

	@Override
	public String getTooltip() {
		return getLabelInternal(tooltip);
	}

	@Override
	public String getLabelId() {
		return label;
	}

	@Override
	public String getActionPath() {
		return actionPath;
	}

	/**
	 * Setter for actionPath.
	 *
	 * @param actionPath
	 *            the actionPath
	 */
	public void setActionPath(String actionPath) {
		this.actionPath = actionPath;
	}

	@Override
	public Integer getOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

}
