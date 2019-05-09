package com.sirma.sep.instance.batch;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseEntity;

/**
 * Entity that stores all information for a single batch job. The stored information could be used to restart or query
 * info for the job.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/01/2019
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "sep_batch_job")
@NamedQueries({ @NamedQuery(name = BatchJobEntity.QUERY_ALL_JOBS_KEY, query = BatchJobEntity.QUERY_ALL_JOBS),
		@NamedQuery(name = BatchJobEntity.QUERY_JOB_BY_INSTANCE_ID_KEY, query = BatchJobEntity.QUERY_JOB_BY_INSTANCE_ID)})
public class BatchJobEntity extends BaseEntity {
	public static final String QUERY_ALL_JOBS_KEY = "QUERY_ALL_JOBS";
	static final String QUERY_ALL_JOBS = "select b from BatchJobEntity b";

	public static final String QUERY_JOB_BY_INSTANCE_ID_KEY = "QUERY_JOB_BY_INSTANCE_ID";
	static final String QUERY_JOB_BY_INSTANCE_ID = "select b from BatchJobEntity b where b.jobInstanceId = :jobInstanceId";

	@Column(name = "job_instance_id", unique = true, nullable = false, length = 32)
	private String jobInstanceId;
	@Column(name = "job_name", length = 100, nullable = false)
	private String jobName;
	@Column(name = "alias", length = 100)
	private String alias;
	@Column(name = "execution_id")
	private long executionId;
	@Column(name = "properties", columnDefinition = "TEXT")
	private String properties;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_on")
	private Date createdOn;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_on")
	private Date updatedOn;

	public String getJobInstanceId() {
		return jobInstanceId;
	}

	public void setJobInstanceId(String jobInstanceId) {
		this.jobInstanceId = jobInstanceId;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public long getExecutionId() {
		return executionId;
	}

	public void setExecutionId(long executionId) {
		this.executionId = executionId;
	}

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BatchJobEntity)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		BatchJobEntity that = (BatchJobEntity) o;
		return getExecutionId() == that.getExecutionId() &&
				Objects.equals(getJobInstanceId(), that.getJobInstanceId()) &&
				Objects.equals(getJobName(), that.getJobName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getJobInstanceId(), getJobName(), getExecutionId());
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
}
