package com.sirmaenterprise.sep.roles.rest;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Model that defines the mapping between a role and action. It returned by role management endpoint.
 *
 * @author BBonev
 */
public class RoleAction implements Serializable {

	private static final long serialVersionUID = -5871363506188568554L;

	private String role;
	private String action;
	private boolean enabled;
	private Set<String> filters = new HashSet<>();

	public String getRole() {
		return role;
	}

	public RoleAction setRole(String role) {
		this.role = role;
		return this;
	}

	public String getAction() {
		return action;
	}

	public RoleAction setAction(String action) {
		this.action = action;
		return this;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public RoleAction setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public Set<String> getFilters() {
		return filters;
	}

	public RoleAction setFilters(Set<String> filters) {
		this.filters = filters;
		return this;
	}
}
