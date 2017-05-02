package com.sirmaenterprise.sep.roles.rest;

import java.io.Serializable;

/**
 * Model object that represent a single role returned from the management endpoints
 *
 * @author BBonev
 */
public class RoleResponse implements Serializable {

	private static final long serialVersionUID = -4651865687093177030L;

	private String id;
	private String label;
	private int order;
	private boolean canWrite;
	private boolean canRead;
	private boolean userDefined;

	public String getId() {
		return id;
	}

	public RoleResponse setId(String id) {
		this.id = id;
		return this;
	}

	public String getLabel() {
		return label;
	}

	public RoleResponse setLabel(String label) {
		this.label = label;
		return this;
	}

	public int getOrder() {
		return order;
	}

	public RoleResponse setOrder(int order) {
		this.order = order;
		return this;
	}

	public boolean isCanWrite() {
		return canWrite;
	}

	public RoleResponse setCanWrite(boolean canWrite) {
		this.canWrite = canWrite;
		return this;
	}

	public boolean isCanRead() {
		return canRead;
	}

	public RoleResponse setCanRead(boolean canRead) {
		this.canRead = canRead;
		return this;
	}

	public boolean isUserDefined() {
		return userDefined;
	}

	public RoleResponse setUserDefined(boolean userDefined) {
		this.userDefined = userDefined;
		return this;
	}
}
