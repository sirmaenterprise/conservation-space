package com.sirma.itt.cmf.beans.model;

import java.io.Serializable;
import java.util.Map;

import org.json.JSONObject;

import com.sirma.itt.emf.domain.JsonRepresentable;
import com.sirma.itt.emf.domain.model.Entity;

/**
 * Entity used to carry data for a single work log entry for a task.
 * 
 * @author BBonev
 */
public class WorkLogEntry implements Entity<Serializable>, JsonRepresentable {

	/** The id. */
	private Serializable id;

	/** The user. */
	private String user;

	/** The properties. */
	private Map<String, Serializable> properties;

	/** The user display name. */
	private String userDisplayName;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JSONObject toJSONObject() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setId(Serializable id) {
		this.id = id;
	}

	/**
	 * Getter method for user.
	 * 
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Setter method for user.
	 * 
	 * @param user
	 *            the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Getter method for properties.
	 * 
	 * @return the properties
	 */
	public Map<String, Serializable> getProperties() {
		return properties;
	}

	/**
	 * Setter method for properties.
	 * 
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(Map<String, Serializable> properties) {
		this.properties = properties;
	}

	/**
	 * Sets the user display name.
	 * 
	 * @param userDisplayName
	 *            the new user display name
	 */
	public void setUserDisplayName(String userDisplayName) {
		this.userDisplayName = userDisplayName;
	}

	/**
	 * Gets the user display name.
	 * 
	 * @return the user display name
	 */
	public String getUserDisplayName() {
		return userDisplayName;
	}
}
