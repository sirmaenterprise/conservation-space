package com.sirma.itt.seip.permissions.db.patches;

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
import com.sirma.itt.semantic.model.vocabulary.EMF;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * If system user name configuration is not set to System, replaces assigned authorities with System.
 *
 * @author smustafov
 */
public class ChangeSystemUserPermissionsPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String PERMISSIONS_UPDATE_SQL_QUERY = "UPDATE sep_authority_role_assignment SET authority = ? WHERE authority = ?";

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
			transactionSupport.invokeInNewTx(() -> updatePermissions(buildFullUserName(currentUserName),
					buildFullUserName(SecurityContext.SYSTEM_USER_NAME)));
		}
	}

	private String buildFullUserName(String userName) {
		return EMF.PREFIX + ":" + userName + "-" + securityContext.getCurrentTenantId();
	}

	private void updatePermissions(String currentUserName, String properUserName) {
		try (Connection connection = tenantDataSource.getConnection();
				PreparedStatement ps = connection.prepareStatement(PERMISSIONS_UPDATE_SQL_QUERY)) {
			ps.setString(1, properUserName);
			ps.setString(2, currentUserName);

			int updateCount = ps.executeUpdate();
			LOGGER.info("Updated {} number of rows, from {} to {}", updateCount, currentUserName,
					properUserName);
		} catch (SQLException e) {
			throw new EmfRuntimeException("Error executing permissions patch for system user", e);
		}
	}

	@Override
	public String getConfirmationMessage() {
		return null;
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
