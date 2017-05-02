package com.sirma.itt.seip.db.patch;

import com.sirma.itt.seip.exception.RollbackedException;

/**
 * Service for running database patches for schema and data.
 * 
 * @author BBonev
 */
public interface PatchService {

	/**
	 * Executes the patching algorithm using the liquibase library.
	 *
	 * @throws RollbackedException
	 *             the rollbacked exception
	 */
	void patchSchema() throws RollbackedException;

	/**
	 * Run data patches after deployment phase but before users could access the server.
	 *
	 * @throws RollbackedException
	 *             the rollbacked exception
	 */
	void patchData() throws RollbackedException;

}