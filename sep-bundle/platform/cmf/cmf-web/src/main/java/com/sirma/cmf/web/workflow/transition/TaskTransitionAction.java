package com.sirma.cmf.web.workflow.transition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.domain.Pair;

/**
 * The Class TaskTransitionAction.
 *
 * @author svelikov
 */
public class TaskTransitionAction {

	/** The transition id. */
	private String transitionId;

	/** The button label. */
	private String buttonLabel;

	/** The immediate. */
	private boolean immediate;

	/** The tooltip. */
	private String tooltip;

	/** The confirmation. */
	private String confirmation;

	/** The disabled. */
	private boolean disabled;

	/** The render icon. */
	private boolean renderIcon;

	/** The icon name. */
	private String iconName;
	/** temporary map - cache. */
	private Map<String, ControlParam> uiParams;
	/** temporary map - cache. */
	private Map<String, ControlParam> controlParams;
	/** the picklist definition - might be null if this is immediate action. */
	private ControlDefinition picklistControl;
	/** used in conjunction with 'picklistControl' */
	private String userSelectionField;

	/**
	 * Instantiates a new task transition action.
	 *
	 * @param transitionId
	 *            the transition id
	 * @param buttonLabel
	 *            the button label
	 * @param tooltip
	 *            the tooltip
	 * @param isImmediate
	 *            the is immediate
	 * @param picklistControl
	 *            is the picklist wrapper to run on transition complete
	 */
	public TaskTransitionAction(String transitionId, String buttonLabel, String tooltip,
			boolean isImmediate, Pair<String, ControlDefinition> picklistControl) {
		this.transitionId = transitionId;
		this.buttonLabel = buttonLabel;
		this.tooltip = tooltip;
		this.immediate = isImmediate;
		this.disabled = false;
		this.renderIcon = false;
		this.picklistControl = picklistControl != null ? picklistControl.getSecond() : null;
		this.userSelectionField = picklistControl != null ? picklistControl.getFirst() : null;
	}

	/**
	 * Instantiates a new task transition action.
	 *
	 * @param transitionId
	 *            the transition id
	 * @param buttonLabel
	 *            the button label
	 * @param immediate
	 *            the immediate
	 * @param tooltip
	 *            the tooltip
	 * @param confirmation
	 *            the confirmation
	 * @param disabled
	 *            the disabled
	 * @param renderIcon
	 *            the render icon
	 * @param iconName
	 *            the icon name
	 */
	public TaskTransitionAction(String transitionId, String buttonLabel, boolean immediate,
			String tooltip, String confirmation, boolean disabled, boolean renderIcon,
			String iconName) {
		this.transitionId = transitionId;
		this.buttonLabel = buttonLabel;
		this.immediate = immediate;
		this.tooltip = tooltip;
		this.confirmation = confirmation;
		this.disabled = disabled;
		this.renderIcon = renderIcon;
		this.iconName = iconName;
	}

	/**
	 * Getter method for buttonLabel.
	 *
	 * @return the buttonLabel
	 */
	public String getButtonLabel() {
		return buttonLabel;
	}

	/**
	 * Setter method for buttonLabel.
	 *
	 * @param buttonLabel
	 *            the buttonLabel to set
	 */
	public void setButtonLabel(String buttonLabel) {
		this.buttonLabel = buttonLabel;
	}

	/**
	 * Getter method for tooltip.
	 *
	 * @return the tooltip
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * Setter method for tooltip.
	 *
	 * @param tooltip
	 *            the tooltip to set
	 */
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	/**
	 * Getter method for confirmation.
	 *
	 * @return the confirmation
	 */
	public String getConfirmation() {
		return confirmation;
	}

	/**
	 * Setter method for confirmation.
	 *
	 * @param confirmation
	 *            the confirmation to set
	 */
	public void setConfirmation(String confirmation) {
		this.confirmation = confirmation;
	}

	/**
	 * Getter method for disabled.
	 *
	 * @return the disabled
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * Setter method for disabled.
	 *
	 * @param disabled
	 *            the disabled to set
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	/**
	 * Getter method for renderIcon.
	 *
	 * @return the renderIcon
	 */
	public boolean isRenderIcon() {
		return renderIcon;
	}

	/**
	 * Setter method for renderIcon.
	 *
	 * @param renderIcon
	 *            the renderIcon to set
	 */
	public void setRenderIcon(boolean renderIcon) {
		this.renderIcon = renderIcon;
		if (renderIcon) {
			String iconNameClass = "icon_" + transitionId.toLowerCase();
			this.iconName = iconNameClass;
		}
	}

	/**
	 * Getter method for iconName.
	 *
	 * @return the iconName
	 */
	public String getIconName() {
		return iconName;
	}

	/**
	 * Setter method for iconName.
	 *
	 * @param iconName
	 *            the iconName to set
	 */
	public void setIconName(String iconName) {
		this.iconName = iconName;
	}

	/**
	 * Getter method for transitionId.
	 *
	 * @return the transitionId
	 */
	public String getTransitionId() {
		return transitionId;
	}

	/**
	 * Setter method for transitionId.
	 *
	 * @param transitionId
	 *            the transitionId to set
	 */
	public void setTransitionId(String transitionId) {
		this.transitionId = transitionId;
	}

	/**
	 * Getter method for immediate.
	 *
	 * @return the immediate
	 */
	public boolean isImmediate() {
		return immediate;
	}

	/**
	 * Setter method for immediate.
	 *
	 * @param immediate
	 *            the immediate to set
	 */
	public void setImmediate(boolean immediate) {
		this.immediate = immediate;
	}

	/**
	 * Checks whether this tranistion has a picker control
	 *
	 * @return true if there it is
	 */
	public boolean hasPickerControl() {
		return picklistControl != null;
	}

	/**
	 * Gets the picker control param as defined in the definition by its key
	 *
	 * @param id
	 *            is the key to get value for
	 * @return the value for the key defined for the current control
	 */
	public String getPickerControlUIParam(String id) {
		if (hasPickerControl()) {
			if (uiParams == null) {
				uiParams = getAsMap(picklistControl.getUiParams());
			}
			ControlParam controlParam = uiParams.get(id);
			if (controlParam != null) {
				return controlParam.getValue();
			}
		}
		return null;
	}
	/**
	 * Gets the picker control param as defined in the definition by its key
	 *
	 * @param id
	 *            is the key to get value for
	 * @return the value for the key defined for the current control
	 */
	public String getPickerControlParam(String id) {
		if (hasPickerControl()) {
			if (controlParams == null) {
				controlParams = getAsMap(picklistControl.getControlParams());
			}
			ControlParam controlParam = controlParams.get(id);
			if (controlParam != null) {
				return controlParam.getValue();
			}
		}
		return null;
	}
	/**
	 * Loads the control parameters as map keyed by its id.
	 *
	 * @param controlParameters
	 *            is the list of params to use
	 * @return the map of parameters
	 */
	protected Map<String, ControlParam> getAsMap(List<ControlParam> controlParameters) {
		Map<String, ControlParam> map = new HashMap<String, ControlParam>(controlParameters.size());
		for (ControlParam controlParam : controlParameters) {
			map.put(controlParam.getName(), controlParam);
		}
		return map;

	}

	/**
	 * Getter method for userSelectionField.
	 *
	 * @return the userSelectionField
	 */
	public String getUserSelectionField() {
		return userSelectionField;
	}

	/**
	 * Setter method for userSelectionField.
	 *
	 * @param userSelectionField
	 *            the userSelectionField to set
	 */
	public void setUserSelectionField(String userSelectionField) {
		this.userSelectionField = userSelectionField;
	}

}
