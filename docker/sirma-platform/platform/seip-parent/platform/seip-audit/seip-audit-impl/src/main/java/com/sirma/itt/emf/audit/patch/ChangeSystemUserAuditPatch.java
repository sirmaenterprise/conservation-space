package com.sirma.itt.emf.audit.patch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;
import com.sirma.itt.semantic.model.vocabulary.EMF;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * If system user name configuration is not set to System, replaces audit activity logs with System.
 *
 * @author smustafov
 */
public class ChangeSystemUserAuditPatch implements CustomTaskChange {

	private static final String SQL_UPDATE_ERROR_MESSAGE = "Error executing audit log patch for system user";
	private static final String AUDIT_UPDATE_SQL_QUERY = "UPDATE emf_events SET userid = ?, username = ? WHERE userid = ?";
	private static final String AUDIT_UPDATE_SYSTEM_USER_SQL_QUERY = "UPDATE emf_events SET objectsystemid = ?, objecttitle = ?, objecturl = ? WHERE objectsystemid = ?";
	private static final String AUDIT_UPDATE_PROPERTY_SQL_QUERY = "UPDATE emf_events SET targetproperties = ? WHERE targetproperties = ?";

	private TransactionSupport transactionSupport;
	private SecurityConfiguration securityConfiguration;
	private SecurityContext securityContext;
	private DataSource dataSource;

	@Override
	public void setUp() throws SetupException {
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		securityConfiguration = CDI.instantiateBean(SecurityConfiguration.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		securityContext = CDI.instantiateBean(SecurityContext.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		dataSource = CDI.instantiateBean(AuditConfiguration.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral())
				.getDataSource();
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		String currentUserName = securityConfiguration.getSystemUserName().get();
		if (!currentUserName.equals(SecurityContext.SYSTEM_USER_NAME)) {
			transactionSupport.invokeInNewTx(() -> {
				String currentFullUserName = buildFullUserName(currentUserName);
				String properUserName = buildFullUserName(SecurityContext.SYSTEM_USER_NAME);

				updateAudit(currentFullUserName, properUserName);
				updateAuditSystemUser(currentFullUserName, properUserName);
				updateAuditProperties(currentFullUserName, properUserName);
			});
		}
	}

	private String buildFullUserName(String userName) {
		return EMF.PREFIX + ":" + userName + "-" + securityContext.getCurrentTenantId();
	}

	private void updateAudit(String currentUserName, String properUserName) {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement ps = connection.prepareStatement(AUDIT_UPDATE_SQL_QUERY)) {
			ps.setString(1, properUserName);
			ps.setString(2, SecurityContext.SYSTEM_USER_NAME + "@" + securityContext.getCurrentTenantId());
			ps.setString(3, currentUserName);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EmfRuntimeException(SQL_UPDATE_ERROR_MESSAGE, e);
		}
	}

	private void updateAuditSystemUser(String currentUserName, String properUserName) {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement ps = connection.prepareStatement(AUDIT_UPDATE_SYSTEM_USER_SQL_QUERY)) {
			ps.setString(1, properUserName);
			ps.setString(2, SecurityContext.SYSTEM_USER_NAME + "@" + securityContext.getCurrentTenantId());
			ps.setString(3, "#/idoc/" + properUserName);
			ps.setString(4, currentUserName);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EmfRuntimeException(SQL_UPDATE_ERROR_MESSAGE, e);
		}
	}

	private void updateAuditProperties(String currentUserName, String properUserName) {
		try (Connection connection = dataSource.getConnection();
				PreparedStatement ps = connection.prepareStatement(AUDIT_UPDATE_PROPERTY_SQL_QUERY)) {
			ps.setString(1, properUserName);
			ps.setString(2, currentUserName);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EmfRuntimeException(SQL_UPDATE_ERROR_MESSAGE, e);
		}
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// not needed
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}

	@Override
	public String getConfirmationMessage() {
		return null;
	}

}
