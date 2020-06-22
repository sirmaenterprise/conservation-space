package com.sirma.itt.seip.db;

/**
 * Datasource model, provisioned when creating datasource in the app server.
 *
 * @author nvelkov
 */
public class DatasourceModel {

	private String username;
	private String password;
	private boolean useJavaContext = true;
	private boolean useCCM = true;

	// Pool settings
	private int minPoolSize = 5;
	private int initialPoolSize = 100;
	private int maxPoolSize;
	private boolean prefill;
	private boolean useStrictMin;
	private String flushStrategy;

	private String driverName;
	private String jndiName;
	private String poolName;

	// Connection validation settings
	private boolean validateOnMatch = true;
	private boolean backgroundValidation;
	private int backgroundValidationMillis;
	private boolean useFastFail;
	private String validConnectionChecker;
	private String exceptionSorter;
	private String validConnectionSQL = "SELECT 1";

	// Connection timeout settings
	private int idleTimeoutMinutes;
	private int queryTimeoutSeconds;
	private int allocationRetries = 4;
	private int allocationRetryWaitMillis = 2500;

	private int preparedStatementCacheSize = 32;
	private boolean sharePreparedStatements = true;

	private String databaseHost;
	private int databasePort;
	private String databaseName;

	private String datasourceName;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isUseJavaContext() {
		return useJavaContext;
	}

	public void setUseJavaContext(boolean useJavaContext) {
		this.useJavaContext = useJavaContext;
	}

	public boolean isUseCCM() {
		return useCCM;
	}

	public void setUseCCM(boolean useCCM) {
		this.useCCM = useCCM;
	}

	public int getMinPoolSize() {
		return minPoolSize;
	}

	public void setMinPoolSize(int minPoolSize) {
		this.minPoolSize = minPoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public boolean isPoolPrefill() {
		return prefill;
	}

	public void setPoolPrefill(boolean poolPrefill) {
		this.prefill = poolPrefill;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getJndiName() {
		return jndiName;
	}

	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	public String getValidConnectionSQL() {
		return validConnectionSQL;
	}

	public void setValidConnectionSQL(String validConnectionSQL) {
		this.validConnectionSQL = validConnectionSQL;
	}

	public int getAllocationRetries() {
		return allocationRetries;
	}

	public void setAllocationRetries(int allocationRetries) {
		this.allocationRetries = allocationRetries;
	}

	public int getPreparedStatementCacheSize() {
		return preparedStatementCacheSize;
	}

	public void setPreparedStatementCacheSize(int preparedStatementCacheSize) {
		this.preparedStatementCacheSize = preparedStatementCacheSize;
	}

	public boolean isSharePreparedStatements() {
		return sharePreparedStatements;
	}

	public void setSharePreparedStatements(boolean sharePreparedStatements) {
		this.sharePreparedStatements = sharePreparedStatements;
	}

	public String getDatabaseHost() {
		return databaseHost;
	}

	public void setDatabaseHost(String databaseHost) {
		this.databaseHost = databaseHost;
	}

	public int getDatabasePort() {
		return databasePort;
	}

	public void setDatabasePort(int databasePort) {
		this.databasePort = databasePort;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getDatasourceName() {
		return datasourceName;
	}

	public void setDatasourceName(String datasourceName) {
		this.datasourceName = datasourceName;
	}

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
	}

	public boolean isUseFastFail() {
		return useFastFail;
	}

	public void setUseFastFail(boolean useFastFail) {
		this.useFastFail = useFastFail;
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

	public int getQueryTimeoutSeconds() {
		return queryTimeoutSeconds;
	}

	public void setQueryTimeoutSeconds(int queryTimeoutSeconds) {
		this.queryTimeoutSeconds = queryTimeoutSeconds;
	}

	public int getAllocationRetryWaitMillis() {
		return allocationRetryWaitMillis;
	}

	public void setAllocationRetryWaitMillis(int allocationRetryWaitMillis) {
		this.allocationRetryWaitMillis = allocationRetryWaitMillis;
	}

	public int getInitialPoolSize() {
		return initialPoolSize;
	}

	public void setInitialPoolSize(int initialPoolSize) {
		this.initialPoolSize = initialPoolSize;
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

	public String getFlushStrategy() {
		return flushStrategy;
	}

	public void setFlushStrategy(String flushStrategy) {
		this.flushStrategy = flushStrategy;
	}

	public int getIdleTimeoutMinutes() {
		return idleTimeoutMinutes;
	}

	public void setIdleTimeoutMinutes(int idleTimeoutMinutes) {
		this.idleTimeoutMinutes = idleTimeoutMinutes;
	}
}
