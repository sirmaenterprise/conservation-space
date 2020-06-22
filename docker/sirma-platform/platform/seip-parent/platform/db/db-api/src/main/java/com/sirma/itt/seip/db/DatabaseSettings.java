package com.sirma.itt.seip.db;

import java.net.URI;

import com.sirma.itt.seip.configuration.ConfigurationProperty;

/**
 * Provides database configurations.
 *
 * @author nvelkov
 */
public interface DatabaseSettings {

	/**
	 * Retrieve the database dialect configuration.
	 *
	 * @return the database dialect configuration
	 */
	ConfigurationProperty<String> getDatabaseDialectConfiguration();

	/**
	 * Retrieve the admin username configuration.
	 *
	 * @return the admin username configuration
	 */
	ConfigurationProperty<String> getAdminUsernameConfiguration();

	/**
	 * Retrieve the admin password configuration.
	 *
	 * @return the admin password configuration
	 */
	ConfigurationProperty<String> getAdminPasswordConfiguration();

	/**
	 * Retrieve the database host configuration.
	 *
	 * @return the database host configuration
	 */
	ConfigurationProperty<String> getDatabaseHostConfiguration();

	/**
	 * Retrieve the database dialect configuration.
	 *
	 * @return the database dialect configuration
	 */
	ConfigurationProperty<String> getDatabaseNameConfiguration();

	/**
	 * Retrieve the database port configuration.
	 *
	 * @return the database port configuration
	 */
	ConfigurationProperty<Integer> getDatabasePortConfiguration();

	/**
	 * Gets the current tenant database URI.
	 *
	 * @return the database URI
	 */
	ConfigurationProperty<URI> getDatabaseUri();

	/**
	 * Gets the current connection pool settings
	 *
	 * @return connection pool settings
	 */
	ConfigurationProperty<ConnectionPoolSettings> getConnectionPoolSettings();
	/**
	 * Gets the current connection timeout settings
	 *
	 * @return connection timeout settings
	 */
	ConfigurationProperty<ConnectionTimeoutSettings> getConnectionTimeoutSettings();

	/**
	 * Gets the current connection validation settings
	 *
	 * @return connection validation settings
	 */
	ConfigurationProperty<ConnectionValidationSettings> getConnectionValidationSettings();

	/**
	 * Wraps the possible configurations for the connection pool settings such as min and max pool size and flush strategy.
	 *
	 * @author BBonev
	 */
	public static class ConnectionPoolSettings {
		/**
		 * The min-pool-size element indicates the minimum number of connections a pool should hold. These are not
		 * created until a Subject is known from a request for a connection. This default to 0.
		 */
		private int minPoolSize;
		/**
		 * The initial-pool-size element indicates the initial number of connections a pool should hold.
		 * This default to 0.
		 */
		private int initialPoolSize;
		/**
		 * The max-pool-size element indicates the maximum number of connections for a pool. No more connections will
		 * be created in each sub-pool. This defaults to 20.
		 */
		private int maxPoolSize;
		/**
		 * Whether to attempt to prefill the connection pool. Default is false
		 */
		private boolean prefill;
		/**
		 * Define if the min-pool-size should be considered a strictly. Default false
		 */
		private boolean useStrictMin;
		/**
		 * Specifies how the pool should be flush in case of an error. Valid values are: <ul>
		 * <li>FailingConnectionOnly (default)
		 * <li>InvalidIdleConnections
		 * <li>IdleConnections
		 * <li>Gracefully
		 * <li>EntirePool
		 * <li>AllInvalidIdleConnections
		 * <li>AllIdleConnections
		 * <li>AllGracefully
		 * <li>AllConnections
		 * </ul>
		 */
		private PoolFlushStrategy flushStrategy;

		public int getMinPoolSize() {
			return minPoolSize;
		}

		public void setMinPoolSize(int minPoolSize) {
			this.minPoolSize = minPoolSize;
		}

		public int getInitialPoolSize() {
			return initialPoolSize;
		}

		public void setInitialPoolSize(int initialPoolSize) {
			this.initialPoolSize = initialPoolSize;
		}

		public int getMaxPoolSize() {
			return maxPoolSize;
		}

		public void setMaxPoolSize(int maxPoolSize) {
			this.maxPoolSize = maxPoolSize;
		}

		public boolean isPrefill() {
			return prefill;
		}

		public void setPrefill(boolean prefill) {
			this.prefill = prefill;
		}

		public boolean isUseStrictMin() {
			return useStrictMin;
		}

		public void setUseStrictMin(boolean useStrictMin) {
			this.useStrictMin = useStrictMin;
		}

		public PoolFlushStrategy getFlushStrategy() {
			return flushStrategy;
		}

		public void setFlushStrategy(PoolFlushStrategy flushStrategy) {
			this.flushStrategy = flushStrategy;
		}
	}

	/**
	 * Specifies how the pool should be flush in case of an error
	 *
	 * @author BBonev
	 */
	public enum PoolFlushStrategy {
		FAILING_CONNECTION_ONLY("FailingConnectionOnly"),
		INVALID_IDLE_CONNECTIONS("InvalidIdleConnections"),
		IDLE_CONNECTIONS("IdleConnections"),
		GRACEFULLY("Gracefully"),
		ENTIRE_POOL("EntirePool"),
		ALL_INVALID_IDLE_CONNECTIONS("AllInvalidIdleConnections"),
		ALL_IDLE_CONNECTIONS("AllIdleConnections"),
		ALL_GRACEFULLY("AllGracefully"),
		ALL_CONNECTIONS("AllConnections");

		private final String id;

		PoolFlushStrategy(String id) {
			this.id = id;
		}

		@Override
		public String toString() {
			return id;
		}

		/**
		 * Resolves the pool strategy by it's id
		 *
		 * @param id the id to resolve
		 * @return the found strategy
		 * @throws IllegalArgumentException if the given id does not match any of the strategies
		 */
		public static PoolFlushStrategy parse(String id) {
			for (PoolFlushStrategy value : values()) {
				if (value.id.equals(id)) {
					return value;
				}
			}
			throw new IllegalArgumentException("Invalid enum value " + id);
		}
	}

	/**
	 * Wraps the possible configurations for the connection timeout settings such as global query timeout or connection
	 * reallocation configurations.
	 *
	 * @author BBonev
	 */
	class ConnectionTimeoutSettings {
		/**
		 * The idle-timeout-minutes elements indicates the maximum time in minutes a connection may be idle before being
		 * closed. The actual maximum time depends also on the IdleRemover scan time, which is 1/2 the smallest
		 * idle-timeout-minutes of any pool.
		 */
		private int idleTimeoutMinutes;
		/**
		 * Any configured query timeout in seconds The default is no timeout
		 */
		private int queryTimeout;
		/**
		 * The allocation retry element indicates the number of times that allocating
		 * a connection should be tried before throwing an exception. The default is 0.
		 */
		private int allocationRetry;
		/**
		 * The allocation retry wait millis element indicates the time in milliseconds
		 * to wait between retrying to allocate a connection. The default is 5000 (5 seconds).
		 */
		private int allocationRetryWaitMillis;

		public int getIdleTimeoutMinutes() {
			return idleTimeoutMinutes;
		}

		public void setIdleTimeoutMinutes(int idleTimeoutMinutes) {
			this.idleTimeoutMinutes = idleTimeoutMinutes;
		}

		public int getQueryTimeout() {
			return queryTimeout;
		}

		public void setQueryTimeout(int queryTimeout) {
			this.queryTimeout = queryTimeout;
		}

		public int getAllocationRetry() {
			return allocationRetry;
		}

		public void setAllocationRetry(int allocationRetry) {
			this.allocationRetry = allocationRetry;
		}

		public int getAllocationRetryWaitMillis() {
			return allocationRetryWaitMillis;
		}

		public void setAllocationRetryWaitMillis(int allocationRetryWaitMillis) {
			this.allocationRetryWaitMillis = allocationRetryWaitMillis;
		}
	}

	/**
	 * Wraps the possible configurations for the connection validation settings.
	 *
	 * @author BBonev
	 */
	class ConnectionValidationSettings {
		/**
		 * The validate-on-match element indicates whether or not connection level validation should be done when a
		 * connection factory attempts to match a managed connection for a given set. This is typically exclusive to the
		 * use of background validation
		 */
		private boolean validateOnMatch = true;
		/**
		 * An element to specify that connections should be validated on a background thread versus being validated
		 * prior to use
		 */
		private boolean backgroundValidation;
		/**
		 * The background-validation-millis element specifies the amount of time, in millis, that background validation
		 * will run. Setting this to positive value will enable {@link #backgroundValidation} and disable {@link #validateOnMatch}
		 */
		private int backgroundValidationMillis;
		/**
		 * Whether fail a connection allocation on the first connection if it is invalid (true) or keep trying until
		 * the pool is exhausted of all potential connections (false) default false.
		 */
		private boolean useFastFail;
		/**
		 * Specify an SQL statement to check validity of a pool connection. This may be called when managed connection
		 * is taken from pool for use.
		 */
		private String checkValidConnectionSql;
		/**
		 * An org.jboss.jca.adapters.jdbc.ValidConnectionChecker that provides a SQLException
		 * isValidConnection(Connection e) method to validate is a connection is valid. An exception means the
		 * connection is destroyed. This overrides the check-valid-connection-sql when present
		 */
		private String validConnectionChecker;
		/**
		 * An org.jboss.jca.adapters.jdbc.ExceptionSorter that provides a boolean isExceptionFatal(SQLException e)
		 * method to validate is an exception should be broadcast to all javax.resource.spi.ConnectionEventListener as
		 * a connectionErrorOccurred message
		 */
		private String exceptionSorter;

		public boolean isValidateOnMatch() {
			return validateOnMatch;
		}

		public void setValidateOnMatch(boolean validateOnMatch) {
			this.validateOnMatch = validateOnMatch;
		}

		public boolean isBackgroundValidation() {
			return backgroundValidation;
		}

		public void setBackgroundValidation(boolean backgroundValidation) {
			this.backgroundValidation = backgroundValidation;
		}

		public int getBackgroundValidationMillis() {
			return backgroundValidationMillis;
		}

		public void setBackgroundValidationMillis(int backgroundValidationMillis) {
			this.backgroundValidationMillis = backgroundValidationMillis;

			boolean isBackgroundValidationMillisSet = backgroundValidationMillis > 0;
			setValidateOnMatch(!isBackgroundValidationMillisSet);
			setBackgroundValidation(isBackgroundValidationMillisSet);
		}

		public boolean isUseFastFail() {
			return useFastFail;
		}

		public void setUseFastFail(boolean useFastFail) {
			this.useFastFail = useFastFail;
		}

		public String getCheckValidConnectionSql() {
			return checkValidConnectionSql;
		}

		public void setCheckValidConnectionSql(String checkValidConnectionSql) {
			this.checkValidConnectionSql = checkValidConnectionSql;
		}

		public String getValidConnectionChecker() {
			return validConnectionChecker;
		}

		public void setValidConnectionChecker(String validConnectionChecker) {
			this.validConnectionChecker = validConnectionChecker;
		}

		public String getExceptionSorter() {
			return exceptionSorter;
		}

		public void setExceptionSorter(String exceptionSorter) {
			this.exceptionSorter = exceptionSorter;
		}
	}
}
