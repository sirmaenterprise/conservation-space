package com.sirma.itt.seip.rule.model;

import java.util.Collection;
import java.util.Collections;

/**
 * DTO to hold basic rule configuration.
 *
 * @author BBonev
 */
public class BaseRuleConfig {
	private String name;
	private boolean asyncSupport = false;
	private Collection<Class<?>> objectTypes = Collections.emptyList();
	private Collection<String> onOperations = Collections.emptyList();
	private Collection<String> onDefinitions = Collections.emptyList();

	/**
	 * Getter method for name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter method for name.
	 *
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter method for asyncSupport.
	 *
	 * @return the asyncSupport
	 */
	public boolean isAsyncSupport() {
		return asyncSupport;
	}

	/**
	 * Setter method for asyncSupport.
	 *
	 * @param asyncSupport
	 *            the asyncSupport to set
	 */
	public void setAsyncSupport(boolean asyncSupport) {
		this.asyncSupport = asyncSupport;
	}

	/**
	 * Getter method for objectTypes.
	 *
	 * @return the objectTypes
	 */
	public Collection<Class<?>> getObjectTypes() {
		return objectTypes;
	}

	/**
	 * Setter method for objectTypes.
	 *
	 * @param objectTypes
	 *            the objectTypes to set
	 */
	public void setObjectTypes(Collection<Class<?>> objectTypes) {
		this.objectTypes = objectTypes;
	}

	/**
	 * Getter method for onOperations.
	 *
	 * @return the onOperations
	 */
	public Collection<String> getOnOperations() {
		return onOperations;
	}

	/**
	 * Setter method for onOperations.
	 *
	 * @param onOperations
	 *            the onOperations to set
	 */
	public void setOnOperations(Collection<String> onOperations) {
		this.onOperations = onOperations;
	}

	/**
	 * Getter method for onDefinitions.
	 *
	 * @return the onDefinitions
	 */
	public Collection<String> getOnDefinitions() {
		return onDefinitions;
	}

	/**
	 * Setter method for onDefinitions.
	 *
	 * @param onDefinitions
	 *            the onDefinitions to set
	 */
	public void setOnDefinitions(Collection<String> onDefinitions) {
		this.onDefinitions = onDefinitions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (asyncSupport ? 1231 : 1237);
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (objectTypes == null ? 0 : objectTypes.hashCode());
		result = prime * result + (onDefinitions == null ? 0 : onDefinitions.hashCode());
		result = prime * result + (onOperations == null ? 0 : onOperations.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof BaseRuleConfig)) {
			return false;
		}
		BaseRuleConfig other = (BaseRuleConfig) obj;
		if (asyncSupport != other.asyncSupport) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (objectTypes == null) {
			if (other.objectTypes != null) {
				return false;
			}
		} else if (!objectTypes.equals(other.objectTypes)) {
			return false;
		}
		if (onDefinitions == null) {
			if (other.onDefinitions != null) {
				return false;
			}
		} else if (!onDefinitions.equals(other.onDefinitions)) {
			return false;
		}
		if (onOperations == null) {
			if (other.onOperations != null) {
				return false;
			}
		} else if (!onOperations.equals(other.onOperations)) {
			return false;
		}
		return true;
	}

}
