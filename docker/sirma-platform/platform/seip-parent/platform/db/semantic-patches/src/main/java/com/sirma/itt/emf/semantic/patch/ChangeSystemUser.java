package com.sirma.itt.emf.semantic.patch;

import java.lang.invoke.MethodHandles;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.util.CDI;
import com.sirma.itt.semantic.model.vocabulary.EMF;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;

/**
 * If system user name configuration is not set to System, copies semantic statements the current one to the one with
 * System user name.
 *
 * @author smustafov
 */
public class ChangeSystemUser extends UpdateSemanticTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String CURRENT_USER_PLACEHOLDER = "%currentUser%";
	private static final String TENANT_ID_PLACEHOLDER = "%tenantId%";
	private static final String SYSTEM_USER_COPY_STATEMENTS_QUERY = ResourceLoadUtil
			.loadResource(ChangeSystemUser.class, "patches/migration/changeSystemUser.sparql");

	private SecurityConfiguration securityConfiguration;
	private SecurityContext securityContext;

	@Override
	public void setUp() throws SetupException {
		super.setUp();
		securityConfiguration = CDI.instantiateBean(SecurityConfiguration.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		securityContext = CDI.instantiateBean(SecurityContext.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		String currentUserName = securityConfiguration.getSystemUserName().get();
		if (!currentUserName.equals(SecurityContext.SYSTEM_USER_NAME)) {
			LOGGER.info("Going to copy semantic statements from {} to {} user", currentUserName,
					SecurityContext.SYSTEM_USER_NAME);
			updateSemantic(buildFullUserName(currentUserName));
		}
	}

	private String buildFullUserName(String userName) {
		return EMF.PREFIX + ":" + userName + "-" + securityContext.getCurrentTenantId();
	}

	private void updateSemantic(String currentUserName) throws CustomChangeException {
		RepositoryConnection repositoryConnection = connectionFactory.produceConnection();
		try {
			String sparqlQuery = prepareQuery(currentUserName);
			Update updateQuery = repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, sparqlQuery);
			updateQuery.setIncludeInferred(false);
			updateQuery.execute();
		} catch (UpdateExecutionException | RepositoryException | MalformedQueryException e) {
			UpdateSemanticTask.rollbackConnection(repositoryConnection, e);
			throw new CustomChangeException("Error executing sparql query for system user migration", e);
		} finally {
			if (repositoryConnection != null) {
				connectionFactory.disposeConnection(repositoryConnection);
			}
		}
	}

	private String prepareQuery(String currentUserName) {
		String replaced = SYSTEM_USER_COPY_STATEMENTS_QUERY.replace(CURRENT_USER_PLACEHOLDER, currentUserName);
		return replaced.replace(TENANT_ID_PLACEHOLDER, securityContext.getCurrentTenantId());
	}

}
