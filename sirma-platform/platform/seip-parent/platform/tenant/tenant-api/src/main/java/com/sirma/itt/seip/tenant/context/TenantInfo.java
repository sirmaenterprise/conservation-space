/**
 *
 */
package com.sirma.itt.seip.tenant.context;

import org.json.JSONObject;

import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Represents an information for a single tenant.
 *
 * @author BBonev
 */
public class TenantInfo implements JsonRepresentable {

	/** The tenant id. */
	private final String tenantId;

	/**
	 * Instantiates a new tenant info.
	 *
	 * @param tenantId
	 *            the tenant id
	 */
	public TenantInfo(String tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * Gets the tenant id.
	 *
	 * @return the tenant id
	 */
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TenantInfo [tenantId=").append(tenantId).append("]");
		return builder.toString();
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (tenantId == null ? 0 : tenantId.hashCode());
		return result;
	}

	/**
	 * Equals.
	 *
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TenantInfo)) {
			return false;
		}
		TenantInfo other = (TenantInfo) obj;
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
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "tenantId", tenantId);
		return object;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		throw new UnsupportedOperationException();
	}

}
