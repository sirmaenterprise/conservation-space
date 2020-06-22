package com.sirma.itt.seip.instance.properties.patch;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Updates instance properties tables for the system user. Changes db and user id of the user to upper case if configuration
 * is with lower case. This is due to the configuration which created the user with lower case and is causing issues.
 * <p>
 * Issue: CMF-29893
 *
 * @author smustafov
 */
public class UpdateSystemUserPropertiesPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String USER_DB_ID_PLACEHOLDER = "emf:@user_id@-%";
	private static final String USER_ID_PLACEHOLDER = "@user_id@@%";

	static final String PROPERTIES_UPDATE_QUERY = "UPDATE emf_properties SET bean_id = replace(bean_id, ?, ?) WHERE bean_id LIKE ?";
	static final String PROPERTY_VALUE_UPDATE_QUERY = "UPDATE emf_propertyvalue SET stringvalue = replace(stringvalue, ?, ?) WHERE stringvalue LIKE ?";

	private SecurityConfiguration securityConfiguration;
	private DataSource tenantDataSource;
	private TransactionSupport transactionSupport;

	@Override
	public void setUp() throws SetupException {
		securityConfiguration = CDI
				.instantiateBean(SecurityConfiguration.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		tenantDataSource = CDI
				.instantiateBean(DatabaseConfiguration.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral())
				.getDataSource();
		transactionSupport = CDI
				.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		String currentSystemUserName = securityConfiguration.getSystemUserName().get();
		if (!SecurityContext.SYSTEM_USER_NAME.equals(currentSystemUserName)) {
			LOGGER.info("Going to update instance properties of system user: {}", currentSystemUserName);
			transactionSupport.invokeInNewTx(() -> updateProperties(currentSystemUserName));
		}
	}

	private void updateProperties(String currentSystemUserName) {
		executeUpdate(currentSystemUserName, PROPERTIES_UPDATE_QUERY, buildDbIdPattern(currentSystemUserName));
		executeUpdate(currentSystemUserName, PROPERTY_VALUE_UPDATE_QUERY, buildDbIdPattern(currentSystemUserName));
		executeUpdate(currentSystemUserName, PROPERTY_VALUE_UPDATE_QUERY, buildUserIdPattern(currentSystemUserName));
	}

	private void executeUpdate(String currentSystemUserName, String query, String filter) {
		try (Connection connection = tenantDataSource.getConnection();
				PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, currentSystemUserName);
			ps.setString(2, SecurityContext.SYSTEM_USER_NAME);
			ps.setString(3, filter);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EmfRuntimeException("Error executing update", e);
		}
	}

	private String buildDbIdPattern(String currentSystemUserName) {
		return USER_DB_ID_PLACEHOLDER.replace("@user_id@", currentSystemUserName);
	}

	private String buildUserIdPattern(String currentSystemUserName) {
		return USER_ID_PLACEHOLDER.replace("@user_id@", currentSystemUserName);
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// not needed
	}

	@Override
	public String getConfirmationMessage() {
		return null;
	}
}
