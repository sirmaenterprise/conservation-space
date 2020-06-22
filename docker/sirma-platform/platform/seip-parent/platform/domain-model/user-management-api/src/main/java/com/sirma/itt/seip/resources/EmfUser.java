package com.sirma.itt.seip.resources;

import java.io.Serializable;
import java.util.Map;
import java.util.TimeZone;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.GenericProxy;
import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.collections.SealedMap;
import com.sirma.itt.seip.domain.TenantAware;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Default basic implementation for {@link com.sirma.itt.seip.resources.User} and {@link UserWithCredentials}
 * interfaces.
 *
 * @author BBonev
 */
public class EmfUser extends EmfResource implements UserWithCredentials, Sealable, TenantAware, GenericProxy<User> {

	private static final long serialVersionUID = -7118793748648952994L;
	/** The ticket. */
	private String ticket;
	/** The password. */
	private String password;
	/** The tenant id. */
	@Tag(11)
	protected String tenantId;

	private transient boolean seal = false;
	private User user;

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
		this.name = name;
		this.password = password;
		setType(ResourceType.USER);
		addIfNotNull(ResourceProperties.USER_ID, name);
	}

	/**
	 * Instantiates a new emf user.
	 *
	 * @param user
	 *            the user
	 */
	public EmfUser(User user) {
		this(user.getIdentityId());
		setContainer(user.getTenantId());
		setId(user.getSystemId());
		setDisplayName(user.getDisplayName());
		setTicket(user.getTicket());
		addAllProperties(user.getProperties());
		setLanguage(user.getLanguage());
		setTimezone(user.getTimezone());
		setActive(user.isActive());
		this.user = user;
	}

	@Override
	public EmfUser createCopy() {
		EmfUser emfUser = new EmfUser(getName());
		emfUser.setTicket(getTicket());
		emfUser.setIdentifier(getIdentifier());
		emfUser.setTenantId(getTenantId());
		emfUser.setSource(getSource());
		emfUser.setActive(isActive());
		if (isDeleted()) {
			emfUser.markAsDeleted();
		}
		// clone properties
		emfUser.setProperties(PropertiesUtil.cloneProperties(getProperties()));
		emfUser.setId(getId());
		return emfUser;
	}

	@Override
	public String toString() {
		return new StringBuilder(256).append("EmfUser [name=")
				.append(getName())
				.append(", password=")
				.append(password == null ? "null" : "PROTECTED")
				.append(", properties=")
				.append(getProperties())
				.append("]").toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Resource)) {
			return false;
		}
		Resource otherUser = (Resource) obj;
		return type == otherUser.getType() && EqualsHelper.nullSafeEquals(getName(), otherUser.getName());
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (name == null ? 0 : name.hashCode());
		result = PRIME * result + (type == null ? 0 : type.hashCode());
		return result;
	}

	@Override
	public String getCredentials() {
		return password;
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
		return getString(ResourceProperties.LANGUAGE);
	}

	@Override
	public void setLanguage(String language) {
		add(ResourceProperties.LANGUAGE, language);
	}

	@Override
	public String getEmail() {
		return getString(ResourceProperties.EMAIL);
	}

	@Override
	public void setEmail(String email) {
		add(ResourceProperties.EMAIL, email);
	}

	@Override
	public void setName(String name) {
		if (isSealed()) {
			return;
		}
		super.setName(name);
	}

	@Override
	public void seal() {
		if (isSealed()) {
			return;
		}
		// seal the instance properties to prevent further modifications to use properties
		setProperties(new SealedMap<>(getProperties()));
		seal = true;
	}

	@Override
	public boolean isSealed() {
		return seal;
	}

	@Override
	public String getContainer() {
		return getTenantId();
	}

	@Override
	public void setContainer(String container) {
		setTenantId(container);
	}

	/**
	 * Gets the user name
	 *
	 * @return the identity id
	 */
	@Override
	public String getIdentityId() {
		return getName();
	}

	/**
	 * Gets the system database id.
	 *
	 * @return the system id
	 */
	@Override
	public Serializable getSystemId() {
		return getId();
	}

	@Override
	public User getTarget() {
		return user;
	}

	@Override
	public void setTarget(User target) {
		user = target;
	}

	@Override
	public User cloneProxy() {
		if (user != null) {
			return new EmfUser(user);
		}
		return createCopy();
	}

	@Override
	public void addAllProperties(Map<String, ? extends Serializable> newProperties) {
		super.addAllProperties(newProperties);
		setDisplayName(null);
	}

	@Override
	public boolean isActive() {
		if (user != null) {
			return user.isActive();
		}
		return super.isActive();
	}

	@Override
	public boolean canLogin() {
		if (user != null) {
			return user.canLogin();
		}
		return UserWithCredentials.super.canLogin();
	}

	@Override
	public TimeZone getTimezone() {
		return get(ResourceProperties.TIMEZONE, TimeZone.class, TimeZone::getDefault);
	}

	@Override
	public void setTimezone(TimeZone timezone) {
		add(ResourceProperties.TIMEZONE, timezone);
	}
}
