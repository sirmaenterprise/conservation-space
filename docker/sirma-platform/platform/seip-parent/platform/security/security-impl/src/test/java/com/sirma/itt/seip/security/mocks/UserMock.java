package com.sirma.itt.seip.security.mocks;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.sirma.itt.seip.security.User;

/**
 * The Class UserMock.
 *
 * @author bbonev
 */
public class UserMock implements User {

	private static final long serialVersionUID = 1676334304042815000L;
	private Serializable id;
	private String identityId;
	private String tenantId;
	private Map<String, Serializable> properties = new HashMap<>();
	private boolean canLogin = true;

	/**
	 * Instantiates a new user mock.
	 *
	 * @param identityId
	 *            the identity id
	 * @param tenantId
	 *            the tenant id
	 */
	public UserMock(String identityId, String tenantId) {
		this.identityId = identityId;
		this.tenantId = tenantId;
		id = "emf:" + identityId;
	}

	@Override
	public Serializable getSystemId() {
		return id;
	}

	@Override
	public String getIdentityId() {
		return identityId;
	}

	@Override
	public String getTenantId() {
		return tenantId;
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public String getLanguage() {
		return null;
	}

	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}

	@Override
	public String getTicket() {
		return null;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (identityId == null ? 0 : identityId.hashCode());
		result = prime * result + (tenantId == null ? 0 : tenantId.hashCode());
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
		if (!(obj instanceof UserMock)) {
			return false;
		}
		UserMock other = (UserMock) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (identityId == null) {
			if (other.identityId != null) {
				return false;
			}
		} else if (!identityId.equals(other.identityId)) {
			return false;
		}
		if (tenantId == null) {
			if (other.tenantId != null) {
				return false;
			}
		} else if (!tenantId.equals(other.tenantId)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public boolean canLogin() {
		return canLogin;
	}

	public UserMock setCanLogin(boolean canLogin) {
		this.canLogin = canLogin;
		return this;
	}

	@Override
	public TimeZone getTimezone() {
		return TimeZone.getDefault();
	}

}
