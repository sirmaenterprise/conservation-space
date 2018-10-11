package com.sirma.itt.seip.rule.model;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

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
	private Collection<String> objectTypes = Collections.emptyList();
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
	public Collection<String> getObjectTypes() {
		return objectTypes;
	}

	/**
	 * Setter method for objectTypes.
	 *
	 * @param objectTypes
	 *            the objectTypes to set
	 */
	public void setObjectTypes(Collection<String> objectTypes) {
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
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (asyncSupport ? 1231 : 1237);
		result = PRIME * result + (name == null ? 0 : name.hashCode());
		result = PRIME * result + (objectTypes == null ? 0 : objectTypes.hashCode());
		result = PRIME * result + (onDefinitions == null ? 0 : onDefinitions.hashCode());
		result = PRIME * result + (onOperations == null ? 0 : onOperations.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof BaseRuleConfig)) {
			return false;
		}
		BaseRuleConfig other = (BaseRuleConfig) obj;
		if (asyncSupport != other.asyncSupport) {
			return false;
		}
		if (!nullSafeEquals(name, other.name) || !nullSafeEquals(objectTypes, other.objectTypes) || !nullSafeEquals(
				onDefinitions, other.onDefinitions)) {
			return false;
		}
		return nullSafeEquals(onOperations, other.onOperations);
	}

}
