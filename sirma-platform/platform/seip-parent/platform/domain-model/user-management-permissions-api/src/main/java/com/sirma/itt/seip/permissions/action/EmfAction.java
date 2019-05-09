package com.sirma.itt.seip.permissions.action;

import java.util.List;

import javax.json.JsonObject;

import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.permissions.Filterable;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Default {@link Action} implementation.
 *
 * @author BBonev
 */
public class EmfAction implements Action, Filterable {

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
	private boolean disabled = false;

	/**
	 * Tooltip bundle identifier
	 */
	private String tooltipValue;

	/**
	 * If this action is disabled, then the reason may be provided and can be visualized in the web page.
	 */
	private String disabledReason;

	/**
	 * Message that if provided, will be displayed in confirmation window before this action to be executed.
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
	 * If is set to be true, then the action should be immediate and the button should submit the form nevertheless
	 * there are uncompleted fields on the form.
	 */
	private boolean immediate = false;

	private String purpose = DefinitionUtil.TRANSITION_PERPOSE_ACTION;

	private List<String> filters;

	private boolean local;

	private boolean visible = true;

	private String group;

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
		return disabled;
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

	@Override
	public String getLabelId() {
		return this.label;
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
			this.disabled = isDisabled;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EmfAction [actionId=");
		builder.append(actionId);
		builder.append(", label=");
		builder.append(label);
		builder.append(", isDisabled=");
		builder.append(disabled);
		builder.append(", tooltip=");
		builder.append(tooltipValue);
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

	@Override
	public String getPurpose() {
		return purpose;
	}

	@Override
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	@Override
	public void setFilters(List<String> filters) {
		this.filters = filters;
	}

	@Override
	public List<String> getFilters() {
		return filters;
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
		if (!sealed) {
			this.local = local;
		}
	}

	@Override
	public JsonObject getConfigurationAsJson() {
		return null;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Sets the visible.
	 *
	 * @param visible
	 *            the new visible
	 */
	public void setVisible(boolean visible) {
		if (!sealed) {
			this.visible = visible;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (actionId == null ? 0 : actionId.hashCode());
		result = prime * result + (filters == null ? 0 : filters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Action)) {
			return false;
		}
		Action other = (Action) obj;
		return EqualsHelper.nullSafeEquals(actionId, other.getActionId());
	}

	/**
	 * Setter for tooltip
	 *
	 * @param tooltip
	 *            bundle identifier
	 */
	public void setTooltip(String tooltip) {
		if (!sealed) {
			this.tooltipValue = tooltip;
		}
	}

	@Override
	public String getTooltip() {
		return this.tooltipValue;
	}

	@Override
	public String getActionPath() {
		// Only Transition definitions have paths.
		return null;
	}

	@Override
	public Integer getOrder() {
		return null;
	}

	@Override
	public String getGroup() {
		return group;
	}

	/**
	 * Setter method for the group.
	 *
	 * @param group
	 *            value to set.
	 */
	public void setGroup(String group) {
		this.group = group;
	}
}
