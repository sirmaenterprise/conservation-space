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

	private final String tenantId;

	private String tenantDisplayName;

	private String tenantDescription;

	/**
	 * Instantiates a new tenant info.
	 *
	 * @param tenantId the tenant id
	 */
	public TenantInfo(String tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * Instantiates a new tenant info with given tenant id and description.
	 *
	 * @param tenantDescription the tenant description
	 * @param tenantId          the tenant id
	 */
	public TenantInfo(String tenantId,String tenantDisplayName, String tenantDescription) {
		this.tenantId = tenantId;
		this.tenantDisplayName = tenantDisplayName;
		this.tenantDescription = tenantDescription;
	}

	/**
	 * Gets the tenant id.
	 *
	 * @return the tenant id
	 */
	public String getTenantId() {
		return tenantId;
	}

	public String getTenantDisplayName() {
		return tenantDisplayName;
	}

	public String getTenantDescription() {
		return tenantDescription;
	}

	@Override
	public String toString() {
		return "TenantInfo [tenantId=" + tenantId + ", tenantDescription=" + tenantDescription + "]";
	}

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
	 * @param obj the obj
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
		JsonUtil.addToJson(object, "tenantDescription", tenantDescription);
		return object;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		throw new UnsupportedOperationException();
	}

}
