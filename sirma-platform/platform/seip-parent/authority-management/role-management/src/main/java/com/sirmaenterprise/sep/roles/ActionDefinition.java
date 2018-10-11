package com.sirmaenterprise.sep.roles;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

/**
 * DTO that represents an user defined or internal action
 *
 * @since 2017-03-27
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
public class ActionDefinition {
	private String id;
	private String actionType;
	private boolean enabled;
	private boolean userDefined;
	private boolean immediate;
	private boolean visible = true;
	private String imagePath;

	public String getId() {
		return id;
	}

	public ActionDefinition setId(String id) {
		this.id = id;
		return this;
	}

	public String getActionType() {
		return actionType;
	}

	public ActionDefinition setActionType(String actionType) {
		this.actionType = actionType;
		return this;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public ActionDefinition setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public boolean isUserDefined() {
		return userDefined;
	}

	public ActionDefinition setUserDefined(boolean userDefined) {
		this.userDefined = userDefined;
		return this;
	}

	public boolean isImmediate() {
		return immediate;
	}

	public ActionDefinition setImmediate(boolean immediate) {
		this.immediate = immediate;
		return this;
	}

	public boolean isVisible() {
		return visible;
	}

	public ActionDefinition setVisible(boolean visible) {
		this.visible = visible;
		return this;
	}

	public String getImagePath() {
		return imagePath;
	}

	public ActionDefinition setImagePath(String imagePath) {
		this.imagePath = imagePath;
		return this;
	}

	@Override
	public int hashCode() {
		return 31 + ((id == null) ? 0 : id.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ActionDefinition)) {
			return false;
		}
		ActionDefinition other = (ActionDefinition) obj;
		return nullSafeEquals(id, other.getId());
	}
}
