/**
 *
 */
package com.sirma.itt.seip.tenant.context;

import org.json.JSONObject;

import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Persistent entity to represent a single tenant information.
 *
 * @author BBonev
 */
public class Tenant implements JsonRepresentable {

	/** The tenant id. */
	private String tenantId;
	/** The status. */
	private TenantStatus status;
	/** The tenant admin. */
	private String tenantAdmin;
	/** The display name. */
	private String displayName;
	/** The description. */
	private String description;

	/**
	 * Instantiates a new tenant.
	 */
	public Tenant() {
		// nothing to do
	}

	/**
	 * Instantiates a new tenant.
	 *
	 * @param tenantId
	 *            the tenant id
	 */
	public Tenant(String tenantId) {
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
	 * Sets the tenant id.
	 *
	 * @param tenantId
	 *            the new tenant id
	 */
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * Gets the tenant status.
	 * 
	 * @return the status
	 */
	public TenantStatus getStatus() {
		return status;
	}

	/**
	 * Sets the tenant status.
	 * 
	 * @param status
	 *            the status to set
	 */
	public void setStatus(TenantStatus status) {
		this.status = status;
	}

	/**
	 * Gets the tenant admin.
	 *
	 * @return the tenant admin
	 */
	public String getTenantAdmin() {
		return tenantAdmin;
	}

	/**
	 * Sets the tenant admin.
	 *
	 * @param tenantAdmin
	 *            the new tenant admin
	 */
	public void setTenantAdmin(String tenantAdmin) {
		this.tenantAdmin = tenantAdmin;
	}

	/**
	 * Gets the display name.
	 *
	 * @return the display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the display name.
	 *
	 * @param displayName
	 *            the new display name
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description
	 *            the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * To json object.
	 *
	 * @return the JSON object
	 */
	@Override
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "id", tenantId);
		if (status != null) {
			JsonUtil.addToJson(object, "status", status.name());
		}
		JsonUtil.addToJson(object, "tenantAdmin", tenantAdmin);
		JsonUtil.addToJson(object, "label", displayName);
		JsonUtil.addToJson(object, "description", description);
		return object;
	}

	/**
	 * From json object.
	 *
	 * @param jsonObject
	 *            the json object
	 */
	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		setTenantId(JsonUtil.getStringValue(jsonObject, "id"));
		setTenantAdmin(JsonUtil.getStringValue(jsonObject, "tenantAdmin"));
		setDisplayName(JsonUtil.getStringValue(jsonObject, "label"));
		setDescription(JsonUtil.getStringValue(jsonObject, "description"));
		Integer jsonStatus = JsonUtil.getIntegerValue(jsonObject, "status");
		if (jsonStatus != null) {
			setStatus(TenantStatus.fromInteger(jsonStatus));
		}
	}

	/**
	 * To TenantInfo object
	 * 
	 * @return The TenantInfo object
	 */
	public TenantInfo toTenantInfo() {
		return new TenantInfo(getTenantId());
	}
}
