package com.sirmaenterprise.sep.roles.rest;

import java.io.Serializable;

/**
 * Represent a single action return from the management rest endpoints
 *
 * @author BBonev
 */
public class ActionResponse implements Serializable {

	private static final long serialVersionUID = -502358067032458007L;

	private String id;
	private String actionType;
	private String label;
	private String tooltip;
	private boolean enabled;
	private boolean immediate;
	private boolean visible;
	private String imagePath;

	public String getId() {
		return id;
	}

	public ActionResponse setId(String id) {
		this.id = id;
		return this;
	}

	public String getLabel() {
		return label;
	}

	public ActionResponse setLabel(String label) {
		this.label = label;
		return this;
	}

	public String getTooltip() {
		return tooltip;
	}

	public ActionResponse setTooltip(String tooltip) {
		this.tooltip = tooltip;
		return this;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public ActionResponse setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public String getActionType() {
		return actionType;
	}

	public ActionResponse setActionType(String actionType) {
		this.actionType = actionType;
		return this;
	}

	public boolean isImmediate() {
		return immediate;
	}

	public ActionResponse setImmediate(boolean immediate) {
		this.immediate = immediate;
		return this;
	}

	public boolean isVisible() {
		return visible;
	}

	public ActionResponse setVisible(boolean visible) {
		this.visible = visible;
		return this;
	}

	public String getImagePath() {
		return imagePath;
	}

	public ActionResponse setImagePath(String imagePath) {
		this.imagePath = imagePath;
		return this;
	}
}
