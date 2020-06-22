package com.sirma.sep.instance.batch.provisioning;

import java.lang.invoke.MethodHandles;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.sep.instance.batch.config.BatchConfigurationModel;

/**
 * Initializer class for the batch subsystem provisioning.
 * 
 * @author nvelkov
 */
public class BatchProvisioningInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private BatchConfigurationModel configurationModel;

	@Inject
	private Instance<BatchConfigurationProvisioner> provisionerInstance;

	/**
	 * Startup the batch subsystem provisioning.
	 * 
	 * @throws RollbackedException
	 *             if an {@link BatchProvisioningException} is thrown during execution
	 */
	@Startup(phase = StartupPhase.BEFORE_APP_START, order = -999, transactionMode = TransactionMode.NOT_SUPPORTED)
	public void provisionBatchSubsystem() throws RollbackedException {
		if (provisionerInstance.isUnsatisfied()) {
			LOGGER.warn("No batch provisioner available. Batch subsystem won't be provisioned.");
			return;
		}

		try {
			provisionerInstance.get().provision(configurationModel);
		} catch (BatchProvisioningException e) {
			LOGGER.error("Error while provisioning batch system {}.", e.getMessage());
			throw new RollbackedException(e);
		}
	}
}
