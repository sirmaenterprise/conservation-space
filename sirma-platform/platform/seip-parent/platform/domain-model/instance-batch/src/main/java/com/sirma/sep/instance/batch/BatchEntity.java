package com.sirma.sep.instance.batch;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.customtype.BooleanCustomType;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseEntity;

/**
 * Entity that represents a single batch item that needs to be processed. It's mapped by job name and job id
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 12/06/2017
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "sep_batch_entity")
@NamedQueries({ @NamedQuery(name = BatchEntity.DELETE_DATA_BY_JOB_ID_KEY, query = BatchEntity.DELETE_DATA_BY_JOB_ID),
		@NamedQuery(name = BatchEntity.QUERY_JOB_DATA_KEY, query = BatchEntity.QUERY_JOB_DATA),
		@NamedQuery(name = BatchEntity.UPDATE_AS_PROCESSED_KEY, query = BatchEntity.UPDATE_AS_PROCESSED),
		@NamedQuery(name = BatchEntity.QUERY_JOBS_INFO_KEY, query = BatchEntity.QUERY_JOBS_INFO),
		@NamedQuery(name = BatchEntity.QUERY_JOB_INFO_KEY, query = BatchEntity.QUERY_JOB_INFO)})
public class BatchEntity extends BaseEntity {

	/**
	 * Delete all entries that are part of the given job id
	 */
	public static final String DELETE_DATA_BY_JOB_ID_KEY = "DELETE_DATA_BY_JOB_ID";
	static final String DELETE_DATA_BY_JOB_ID = "delete from BatchEntity where jobInstanceId=:jobId";

	/**
	 * Query job data (instance ids) for the given job id
	 */
	public static final String QUERY_JOB_DATA_KEY = "QUERY_JOB_DATA";
	static final String QUERY_JOB_DATA = "select instanceId from BatchEntity where jobInstanceId=:jobId order by id";

	/**
	 * Update a given set of instances ids for a given job id as processed. The marked identifiers will not be
	 * returned again for the given job instance id.
	 */
	public static final String UPDATE_AS_PROCESSED_KEY = "UPDATE_AS_PROCESSED";
	static final String UPDATE_AS_PROCESSED =
			"update BatchEntity set processed = 1 where jobInstanceId=:jobId AND instanceId in "
					+ "(:instanceIds)";

	public static final String QUERY_JOBS_INFO_KEY = "QUERY_JOBS_INFO";
	static final String QUERY_JOBS_INFO = "select jobInstanceId, count(processed) from BatchEntity where processed = :processed group by jobInstanceId";

	public static final String QUERY_JOB_INFO_KEY = "QUERY_JOB_INFO";
	static final String QUERY_JOB_INFO = "select count(processed) from BatchEntity where jobInstanceId = :jobId and processed = :processed";

	@Column(name = "job_instance_id", length = 32, nullable = false)
	private String jobInstanceId;
	@Column(name = "job_name", length = 100, nullable = false)
	private String jobName;
	@Column(name = "instance_id", length = 100, nullable = false)
	private String instanceId;

	@Type(type = BooleanCustomType.TYPE_NAME)
	@Column(name = "processed")
	private boolean processed;

	/**
	 * Default constructor
	 */
	public BatchEntity() {
		// default constructor
	}

	/**
	 * Initialize all required entity properties
	 *  @param jobName the job name
	 * @param jobInstanceId the job instance id
	 * @param instanceId the instance id that need to be processed
	 */
	public BatchEntity(String jobName, String jobInstanceId, String instanceId) {
		this.jobInstanceId = jobInstanceId;
		this.jobName = jobName;
		this.instanceId = instanceId;
	}

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

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}

		BatchEntity that = (BatchEntity) o;
		return jobInstanceId.equals(that.jobInstanceId)
				&& jobName.equals(that.jobName)
				&& instanceId.equals(that.instanceId);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + jobInstanceId.hashCode();
		result = 31 * result + jobName.hashCode();
		result = 31 * result + instanceId.hashCode();
		return result;
	}
}
