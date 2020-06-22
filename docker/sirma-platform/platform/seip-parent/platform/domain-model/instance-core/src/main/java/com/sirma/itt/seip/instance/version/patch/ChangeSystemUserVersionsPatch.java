package com.sirma.itt.seip.instance.version.patch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.sirma.itt.seip.db.DatabaseConfiguration;
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
 * If system user name configuration is not set to System, replaces stringvalue column in emf_archivedpropertyvalue
 * table to the correct value.
 *
 * @author smustafov
 */
public class ChangeSystemUserVersionsPatch implements CustomTaskChange {

	private static final String VERSIONS_UPDATE_SQL_QUERY = "UPDATE emf_archivedpropertyvalue SET stringvalue = ? WHERE stringvalue = ?";

	private TransactionSupport transactionSupport;
	private SecurityConfiguration securityConfiguration;
	private SecurityContext securityContext;
	private DataSource tenantDataSource;

	@Override
	public void setUp() throws SetupException {
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		securityConfiguration = CDI.instantiateBean(SecurityConfiguration.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		securityContext = CDI.instantiateBean(SecurityContext.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		tenantDataSource = CDI
				.instantiateBean(DatabaseConfiguration.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral())
				.getDataSource();
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		String currentUserName = securityConfiguration.getSystemUserName().get();
		if (!currentUserName.equals(SecurityContext.SYSTEM_USER_NAME)) {
			transactionSupport.invokeInNewTx(() -> updateVersions(buildFullUserName(currentUserName),
					buildFullUserName(SecurityContext.SYSTEM_USER_NAME)));
		}
	}

	private String buildFullUserName(String userName) {
		return EMF.PREFIX + ":" + userName + "-" + securityContext.getCurrentTenantId();
	}

	private void updateVersions(String currentUserName, String properUserName) {
		try (Connection connection = tenantDataSource.getConnection();
				PreparedStatement ps = connection.prepareStatement(VERSIONS_UPDATE_SQL_QUERY)) {
			ps.setString(1, properUserName);
			ps.setString(2, currentUserName);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new EmfRuntimeException("Error executing versions patch for system user", e);
		}
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
