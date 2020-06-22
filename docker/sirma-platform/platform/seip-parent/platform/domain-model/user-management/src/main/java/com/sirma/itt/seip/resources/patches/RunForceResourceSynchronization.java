package com.sirma.itt.seip.resources.patches;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.synchronization.SyncRuntimeConfiguration;
import com.sirma.itt.seip.synchronization.SynchronizationRunner;
import com.sirma.itt.seip.util.CDI;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Runs force user and group synchronization. Needed for fixing isMemberOf relation for users (CMF-25551). This patch is
 * executed only if there are persisted users in relational db.
 *
 * @author smustafov
 */
public class RunForceResourceSynchronization implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private ResourceService resourceService;
	private SynchronizationRunner synchronizationRunner;

	@Override
	public void setUp() throws SetupException {
		resourceService = CDI.instantiateBean(ResourceService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		synchronizationRunner = CDI.instantiateBean(SynchronizationRunner.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		if (isUsersPersisted()) {
			LOGGER.info("Running force user and group synchronization");

			SyncRuntimeConfiguration syncConfiguration = new SyncRuntimeConfiguration();
			syncConfiguration.enableForceSynchronization();
			synchronizationRunner.runAll(syncConfiguration);

			LOGGER.info("Force user and group synchronization finished");
		}
	}

	/**
	 * Checks if there are any users in the relational db. If there are not it means that we are creating new tenant and
	 * they are not persisted.
	 *
	 * @return true when there are persisted users in relational db, otherwise false
	 */
	private boolean isUsersPersisted() {
		List<User> users = resourceService.getAllUsers();
		return users != null && !users.isEmpty();
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
