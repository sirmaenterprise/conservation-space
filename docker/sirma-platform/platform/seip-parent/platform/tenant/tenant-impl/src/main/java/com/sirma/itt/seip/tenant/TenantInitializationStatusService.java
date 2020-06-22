package com.sirma.itt.seip.tenant;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * Activated when a tenant creation is initialized, this service returns the status of the tenant
 * creation process.
 *
 * @author nvelkov
 */
@ApplicationScoped
public class TenantInitializationStatusService {

	private Map<String, Pair<Status, String>> tenantIdtoStatusMapping = CollectionUtils.createHashMap(5);

	/**
	 * Get the tenant creation status for the given tenantId.
	 *
	 * @param tenantId
	 *            the tenantId
	 * @return the tenant initialization status
	 */
	public Pair<Status, String> getStatus(String tenantId) {
		Pair<Status, String> status = tenantIdtoStatusMapping.get(tenantId);
		if (status != null) {
			return status;
		}
		return Pair.nullPair();
	}

	/**
	 * Set the status of the tenant creation process for the given tenantId.
	 *
	 * @param tenantId
	 *            the tenantId
	 * @param status
	 *            the status
	 * @param message
	 *            the message
	 */
	public void setStatus(String tenantId, Status status, String message) {
		tenantIdtoStatusMapping.put(tenantId, new Pair<>(status, message));
	}

	/**
	 * Check if the tenant creation process is completed;
	 *
	 * @param tenantId
	 *            the tenantId
	 * @return true if it's completed false otherwise
	 */
	public boolean isCompleted(String tenantId) {
		return Status.COMPLETED.equals(getStatus(tenantId).getFirst());
	}

	/**
	 * Check if the tenant creation process has failed.
	 *
	 * @param tenantId
	 *            the tenantId
	 * @return true if it has failed false otherwise
	 */
	public boolean hasFailed(String tenantId) {
		return Status.FAILED.equals(getStatus(tenantId).getFirst());
	}

	/**
	 * Check if the tenant creation process is in progress;
	 *
	 * @param tenantId
	 *            the tenantId
	 * @return true if it's in progress false otherwise
	 */
	public boolean isInProgress(String tenantId) {
		return Status.IN_PROGRESS.equals(getStatus(tenantId).getFirst());
	}

	/**
	 * Tenant creation statuses.
	 *
	 * @author nvelkov
	 */
	public enum Status {
		COMPLETED("COMPLETED"), IN_PROGRESS("IN_PROGRESS"), FAILED("FAILED");

		private final String status;

		private Status(final String status) {
			this.status = status;
		}

		/**
		 * Gets the status.
		 * 
		 * @return the status
		 */
		public String getStatus() {
			return status;
		}
	}
}