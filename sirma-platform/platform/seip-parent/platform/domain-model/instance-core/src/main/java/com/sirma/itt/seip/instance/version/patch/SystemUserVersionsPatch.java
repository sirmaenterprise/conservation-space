package com.sirma.itt.seip.instance.version.patch;

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
 * Patch that updates versions related tables where the system user id is not the proper one.
 * This is caused by wrongly set configuration for the system user name, the config is not used anymore,
 * but there was still broken data.
 *
 * @author smustafov
 */
public class SystemUserVersionsPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	static final String ARCHIVED_PROPERTIES_UPDATE_QUERY = "UPDATE emf_archivedproperties SET bean_id = replace(bean_id, ?, ?) WHERE bean_id LIKE ?";
	static final String ARCHIVED_PROPERTY_VALUE_UPDATE_QUERY = "UPDATE emf_archivedpropertyvalue SET stringvalue = replace(stringvalue, ?, ?) WHERE stringvalue LIKE ?";
	static final String ARCHIVED_ENTITY_UPDATE_QUERY = "UPDATE emf_archivedentity SET id = replace(id, ?, ?), targetid = replace(targetid, ?, ?) WHERE id LIKE ?";
	static final String USER_ID_PLACEHOLDER = "emf:@user_id@-%";

	private TransactionSupport transactionSupport;
	private SecurityConfiguration securityConfiguration;
	private DataSource tenantDataSource;

	@Override
	public void setUp() throws SetupException {
		transactionSupport = CDI
				.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		securityConfiguration = CDI
				.instantiateBean(SecurityConfiguration.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		tenantDataSource = CDI
				.instantiateBean(DatabaseConfiguration.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral())
				.getDataSource();
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		String currentSystemUserName = securityConfiguration.getSystemUserName().get();
		if (isWrongUsernameSet(currentSystemUserName)) {
			LOGGER.info("Going to update versions tables for system user: {}", currentSystemUserName);
			transactionSupport.invokeInNewTx(() -> updateVersionsData(currentSystemUserName));
		}
	}

	private boolean isWrongUsernameSet(String currentSystemUserName) {
		return !SecurityContext.SYSTEM_USER_NAME.equals(currentSystemUserName);
	}

	private void updateVersionsData(String currentSystemUserName) {
		updateArchivedProperties(currentSystemUserName);
		updateArchivedPropertiesValues(currentSystemUserName);
		updateArchivedEntity(currentSystemUserName);
	}

	private void updateArchivedProperties(String currentSystemUserName) {
		try (Connection connection = tenantDataSource.getConnection();
				PreparedStatement ps = connection.prepareStatement(ARCHIVED_PROPERTIES_UPDATE_QUERY)) {
			ps.setString(1, currentSystemUserName);
			ps.setString(2, SecurityContext.SYSTEM_USER_NAME);
			ps.setString(3, buildDbIdPattern(currentSystemUserName));
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EmfRuntimeException("Error executing update for emf_archivedproperties", e);
		}
	}

	private void updateArchivedPropertiesValues(String currentSystemUserName) {
		try (Connection connection = tenantDataSource.getConnection();
				PreparedStatement ps = connection.prepareStatement(ARCHIVED_PROPERTY_VALUE_UPDATE_QUERY)) {
			ps.setString(1, currentSystemUserName);
			ps.setString(2, SecurityContext.SYSTEM_USER_NAME);
			ps.setString(3, buildDbIdPattern(currentSystemUserName));
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EmfRuntimeException("Error executing update for emf_archivedpropertyvalue", e);
		}
	}

	private void updateArchivedEntity(String currentSystemUserName) {
		try (Connection connection = tenantDataSource.getConnection();
				PreparedStatement ps = connection.prepareStatement(ARCHIVED_ENTITY_UPDATE_QUERY)) {
			ps.setString(1, currentSystemUserName);
			ps.setString(2, SecurityContext.SYSTEM_USER_NAME);
			ps.setString(3, currentSystemUserName);
			ps.setString(4, SecurityContext.SYSTEM_USER_NAME);
			ps.setString(5, buildDbIdPattern(currentSystemUserName));
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EmfRuntimeException("Error executing update for emf_archivedpropertyvalue", e);
		}
	}

	private String buildDbIdPattern(String currentSystemUserName) {
		return USER_ID_PLACEHOLDER.replace("@user_id@", currentSystemUserName);
	}

	@Override
	public String getConfirmationMessage() {
		return "Version tables for system user are updated successfully";
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// not needed
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}
}
