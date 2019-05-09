package com.sirma.sep.model.management.persistence;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseEntity;

/**
 * Represent a persistent representation of a model change. The change data is stored as text in json.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 07/08/2018
 */
@PersistenceUnitBinding({PersistenceUnits.PRIMARY})
@Entity(name = "sep_model_changes")
@NamedQueries({
		@NamedQuery(name = ModelChangeEntity.QUERY_CHANGES_SINCE_KEY, query = ModelChangeEntity.QUERY_CHANGES_SINCE),
		@NamedQuery(name = ModelChangeEntity.QUERY_NOT_DEPLOYED_CHANGES_KEY, query = ModelChangeEntity.QUERY_NOT_DEPLOYED_CHANGES),
		@NamedQuery(name = ModelChangeEntity.QUERY_CHANGES_BY_REQUEST_ID_KEY, query = ModelChangeEntity.QUERY_CHANGES_BY_REQUEST_ID),
		@NamedQuery(name = ModelChangeEntity.QUERY_NOT_DEPLOYED_CHANGES_FOR_NODE_KEY, query = ModelChangeEntity.QUERY_NOT_DEPLOYED_CHANGES_FOR_NODE),
		@NamedQuery(name = ModelChangeEntity.QUERY_NOT_DEPLOYED_PATHS_BEFORE_VERSION_KEY, query = ModelChangeEntity.QUERY_NOT_DEPLOYED_PATHS_BEFORE_VERSION),
		@NamedQuery(name = ModelChangeEntity.QUERY_LAST_KNOWN_MODEL_VERSION_KEY, query = ModelChangeEntity.QUERY_LAST_KNOWN_MODEL_VERSION),
		@NamedQuery(name = ModelChangeEntity.UPDATE_AS_DEPLOYED_KEY, query = ModelChangeEntity.UPDATE_AS_DEPLOYED)})
public class ModelChangeEntity extends BaseEntity {

	public static final String QUERY_CHANGES_SINCE_KEY = "QUERY_CHANGES_SINCE";
	static final String QUERY_CHANGES_SINCE = "select c from com.sirma.sep.model.management.persistence.ModelChangeEntity c where c.appliedVersion is not null and c.appliedVersion > :version and c.deployedOn is null order by c.id ASC";
	public static final String QUERY_NOT_DEPLOYED_CHANGES_KEY = "QUERY_NOT_DEPLOYED_CHANGES";
	static final String QUERY_NOT_DEPLOYED_CHANGES = "select c from com.sirma.sep.model.management.persistence.ModelChangeEntity c where c.appliedVersion is not null and c.appliedVersion <= :version and c.deployedOn is null and c.status in (:status) order by c.id ASC";
	public static final String QUERY_CHANGES_BY_REQUEST_ID_KEY = "QUERY_CHANGES_BY_REQUEST_ID";
	static final String QUERY_CHANGES_BY_REQUEST_ID = "select c from com.sirma.sep.model.management.persistence.ModelChangeEntity c where c.requestId = :requestId and c.appliedOn is null order by c.id ASC";
	public static final String QUERY_NOT_DEPLOYED_PATHS_BEFORE_VERSION_KEY = "QUERY_NOT_DEPLOYED_PATHS_BEFORE_VERSION";
	static final String QUERY_NOT_DEPLOYED_PATHS_BEFORE_VERSION = "select c.path from com.sirma.sep.model.management.persistence.ModelChangeEntity c where c.deployedOn is null and c.appliedVersion is not null and c.appliedVersion <= :version and c.status in (:status) order by c.id ASC";
	public static final String QUERY_NOT_DEPLOYED_CHANGES_FOR_NODE_KEY = "QUERY_NOT_DEPLOYED_CHANGES_FOR_NODE";
	static final String QUERY_NOT_DEPLOYED_CHANGES_FOR_NODE = "select c from com.sirma.sep.model.management.persistence.ModelChangeEntity c where c.path like :nodeAddress AND c.deployedOn is null and c.appliedVersion is not null and c.appliedVersion <= :version order by c.id ASC";
	public static final String QUERY_LAST_KNOWN_MODEL_VERSION_KEY = "QUERY_LAST_KNOWN_MODEL_VERSION";
	static final String QUERY_LAST_KNOWN_MODEL_VERSION = "select max(c.appliedVersion) from com.sirma.sep.model.management.persistence.ModelChangeEntity c where c.appliedVersion is not null";
	public static final String UPDATE_AS_DEPLOYED_KEY = "UPDATE_AS_DEPLOYED";
	static final String UPDATE_AS_DEPLOYED = "update com.sirma.sep.model.management.persistence.ModelChangeEntity c set c.deployedOn = :deployedOn, c.status = :status where c.deployedOn is null and c.id in (:ids)";

	@Column(name = "request_id", length = 16, nullable = false, updatable = false)
	private String requestId;
	@Column(name = "path", length = 2048, nullable = false)
	private String path;
	@Column(name = "initial_version", nullable = false, updatable = false)
	private long initialVersion;
	@Column(name = "applied_version")
	private Long appliedVersion;
	@Column(name = "created_by", nullable = false, updatable = false)
	private String createdBy;
	@Column(name = "change_data", columnDefinition = "text", nullable = false)
	private String changeData;
	@Column(name = "created_on", updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdOn;
	@Column(name = "applied_on")
	@Temporal(TemporalType.TIMESTAMP)
	private Date appliedOn;
	@Column(name = "deployed_on")
	@Temporal(TemporalType.TIMESTAMP)
	private Date deployedOn;
	@Column(name = "failed_on")
	@Temporal(TemporalType.TIMESTAMP)
	private Date failedOn;
	@Column(name = "status", length = 32)
	private String status;
	@Column(name = "status_message", length = 2048)
	private String statusMessage;

	public long getInitialVersion() {
		return initialVersion;
	}

	public void setInitialVersion(long initialVersion) {
		this.initialVersion = initialVersion;
	}

	public Long getAppliedVersion() {
		return appliedVersion;
	}

	public void setAppliedVersion(Long appliedVersion) {
		this.appliedVersion = appliedVersion;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getChangeData() {
		return changeData;
	}

	public void setChangeData(String changeData) {
		this.changeData = changeData;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getAppliedOn() {
		return appliedOn;
	}

	public void setAppliedOn(Date appliedOn) {
		this.appliedOn = appliedOn;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	void setFailedOn(Date failToApply) {
		this.failedOn = failToApply;
	}

	public Date getFailedOn() {
		return failedOn;
	}

	public Date getDeployedOn() {
		return deployedOn;
	}

	public void setDeployedOn(Date deployedOn) {
		this.deployedOn = deployedOn;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String failedWith) {
		this.statusMessage = failedWith;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String changeStatus) {
		this.status = changeStatus;
	}

	@Override
	public String toString() {
		return new StringBuilder(512)
				.append("ModelChangeEntity{")
				.append("requestId='").append(requestId).append('\'')
				.append(", path='").append(path).append('\'')
				.append(", initialVersion=").append(initialVersion)
				.append(", appliedVersion=").append(appliedVersion)
				.append(", createdBy='").append(createdBy).append('\'')
				.append(", createdOn=").append(createdOn)
				.append(", appliedOn=").append(appliedOn)
				.append(", deployedOn=").append(deployedOn)
				.append(", failedOn=").append(failedOn)
				.append(", status=").append(status)
				.append(", statusMessage=").append(statusMessage)
				.append('}')
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ModelChangeEntity)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		ModelChangeEntity entity = (ModelChangeEntity) o;
		return Objects.equals(requestId, entity.requestId) &&
				Objects.equals(path, entity.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), requestId, path);
	}
}
