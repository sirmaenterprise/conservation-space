package com.sirma.sep.instance.batch.config;

import com.sirma.itt.seip.db.Datasources;

/**
 * The configuration model of the batch subsystem.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @author nvelkov
 * @apiNote https://wildscribe.github.io/Wildfly/9.0.0.Final/subsystem/batch/index.html
 * @since 14/06/2017
 */
public class BatchConfigurationModel {

	/**
	 * The type of the job repository to use
	 */
	private JobRepositoryType repositoryType = JobRepositoryType.JDBC;

	/**
	 * The JNDI name a JDBC job-repository will use to connect to the database.
	 */
	private String jndiName = Datasources.coreJndi();

	/**
	 * The name of the created thread factory.
	 */
	private String threadFactoryName;

	/**
	 * Specifies the name of a thread group to create for this thread factory.
	 */
	private String threadFactoryGroupName;

	/**
	 * The "thread-name-pattern" is the template used to create names for threads. The following
	 * patterns may be used: 
	 * %% - emit a percent sign 
	 * %t - emit the per-factory thread sequence number 
	 * %g - emit the global thread sequence number 
	 * %f - emit the factory sequence number 
	 * %i - emit the thread ID 
	 * %G - emit the thread group name
	 */
	private String threadFactoryNamePattern;

	/**
	 * May be used to specify the thread priority of created threads. Maximum value is 10.
	 */
	private int threadFactoryPriority;

	/**
	 * The maximum thread pool size.
	 */
	private int threadPoolSize = 10;

	/**
	 * Used to specify the amount of time that pool threads should be kept running when idle; if not
	 * specified, threads will run until the executor is shut down.
	 */
	private int threadPoolKeepAliveInSeconds = 30;

	/**
	 * Specifies the name of a specific thread factory to use to create worker threads. If not
	 * defined an appropriate default thread factory will be used.
	 */
	private String threadPoolThreadFactoryName;

	/**
	 * Create a {@link BatchConfigurationModel} with in memory repository.
	 * 
	 * @return the configuration model
	 */
	public static BatchConfigurationModel withInMemoryRepository() {
		return new BatchConfigurationModel().inMemory();
	}

	/**
	 * Create a {@link BatchConfigurationModel} with jdbc repository.
	 * 
	 * @param jndiName
	 *            the jndi name of the jdbc repository.
	 * @return the configuration model
	 */
	public static BatchConfigurationModel withJdbcRepository(String jndiName) {
		return new BatchConfigurationModel().jdbc(jndiName);
	}

	private BatchConfigurationModel inMemory() {
		repositoryType = JobRepositoryType.IN_MEMORY;
		return this;
	}

	private BatchConfigurationModel jdbc(String jdbcJndi) {
		repositoryType = JobRepositoryType.JDBC;
		this.jndiName = jdbcJndi;
		return this;
	}

	/**
	 * Specifies job repository types.
	 * 
	 * @author nvelkov
	 */
	public enum JobRepositoryType {
		IN_MEMORY("in-memory"), JDBC("jdbc");

		private final String value;

		private JobRepositoryType(String value) {
			this.value = value;
		}

		/**
		 * Get the value of the repository type.
		 * 
		 * @return the value of the repository type
		 */
		public String getValue() {
			return value;
		}

	}

	public JobRepositoryType getRepositoryType() {
		return repositoryType;
	}

	public void setRepositoryType(JobRepositoryType repositoryType) {
		this.repositoryType = repositoryType;
	}

	public String getThreadFactoryName() {
		return threadFactoryName;
	}

	public void setThreadFactoryName(String threadFactoryName) {
		this.threadFactoryName = threadFactoryName;
	}

	public String getThreadFactoryGroupName() {
		return threadFactoryGroupName;
	}

	public void setThreadFactoryGroupName(String threadFactoryGroupName) {
		this.threadFactoryGroupName = threadFactoryGroupName;
	}

	public String getThreadFactoryNamePattern() {
		return threadFactoryNamePattern;
	}

	public void setThreadFactoryNamePattern(String threadFactoryNamePattern) {
		this.threadFactoryNamePattern = threadFactoryNamePattern;
	}

	public int getThreadFactoryPriority() {
		return threadFactoryPriority;
	}

	public void setThreadFactoryPriority(int threadFactoryPriority) {
		this.threadFactoryPriority = threadFactoryPriority;
	}

	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	public int getThreadPoolKeepAliveInSeconds() {
		return threadPoolKeepAliveInSeconds;
	}

	public void setThreadPoolKeepAliveInSeconds(int threadPoolKeepAliveInSeconds) {
		this.threadPoolKeepAliveInSeconds = threadPoolKeepAliveInSeconds;
	}

	public String getThreadPoolThreadFactoryName() {
		return threadPoolThreadFactoryName;
	}

	public void setThreadPoolThreadFactoryName(String threadPoolThreadFactoryName) {
		this.threadPoolThreadFactoryName = threadPoolThreadFactoryName;
	}

	public String getJndiName() {
		return jndiName;
	}

	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}
}
