package com.sirma.itt.emf.security.model;

import java.io.Serializable;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.resources.EmfResourcesUtil;
import com.sirma.itt.emf.resources.ResourceProperties;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.EmfResource;
import com.sirma.itt.emf.resources.model.Resource;

/**
 * Default basic implementation for {@link com.sirma.itt.emf.security.model.User} and
 * {@link UserWithCredentials} interfaces.
 *
 * @author BBonev
 */
public class EmfUser extends EmfResource implements UserWithCredentials, Cloneable, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7118793748648952994L;
	/** The ticket. */
	private String ticket;
	/** The password. */
	private String password;
	/** The tenant id. */
	@Tag(11)
	protected String tenantId;
	private transient String displayName;

	/**
	 * Instantiates a new emf user.
	 */
	public EmfUser() {
		// just default constructor
		this(null, null);
	}

	/**
	 * Instantiates a new cmf user.
	 *
	 * @param name
	 *            the name
	 */
	public EmfUser(String name) {
		this(name, null);
	}

	/**
	 * Instantiates a new cmf user.
	 *
	 * @param name
	 *            the name
	 * @param password
	 *            the password
	 */
	public EmfUser(String name, String password) {
		this.identifier = name;
		this.password = password;
		setType(ResourceType.USER);
		getProperties().put(ResourceProperties.USER_ID, name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EmfUser clone() {
		EmfUser emfUser = new EmfUser(getName());
		emfUser.setTicket(getTicket());
		// clone properties
		emfUser.setProperties(PropertiesUtil.cloneProperties(getProperties()));
		emfUser.setId(getId());
		return emfUser;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EmfUser [identifier=");
		builder.append(identifier);
		builder.append(", password=");
		builder.append(password == null ? "null" : "PROTECTED");
		builder.append(", properties=");
		builder.append(properties);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Resource)) {
			return false;
		}
		Resource other = (Resource) obj;
		if (type != other.getType()) {
			return false;
		}
		if (identifier == null) {
			if (other.getIdentifier() != null) {
				return false;
			}
		} else if (!identifier.equals(other.getIdentifier())) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCredentials() {
		return password;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return getName();
	}

	@Override
	public String getTicket() {
		return ticket;
	}

	/**
	 * @param ticket
	 *            the ticket to set
	 */
	@Override
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	@Override
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * Setter method for tenantId.
	 *
	 * @param tenantId
	 *            the tenantId to set
	 */
	@Override
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	@Override
	public String getDisplayName() {
		if (displayName == null) {
			displayName = EmfResourcesUtil.buildDisplayName(getProperties());
		}
		return displayName;
	}

	@Override
	public ResourceType getType() {
		return ResourceType.USER;
	}

	@Override
	public String getLanguage() {
		return (String) getProperties().get(ResourceProperties.LANGUAGE);
	}
}
