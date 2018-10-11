package com.sirma.sep.instance.batch.provisioning;

import com.sirma.sep.instance.batch.config.BatchConfigurationModel;

/**
 * Responsible for provisioning the batch subsystem based on the given configurations.
 * 
 * @author nvelkov
 * @see {{@link com.sirma.sep.instance.batch.config.BatchConfigurationModel}
 */
public interface BatchConfigurationProvisioner {

	/**
	 * Provision the batch subsystem based on the given configurations.
	 * 
	 * @param configurationModel
	 *            the configuration model
	 * @throws BatchProvisioningException
	 *             occurs when a wrong response is returned from the remote operation
	 */
	void provision(BatchConfigurationModel configurationModel) throws BatchProvisioningException;

}
