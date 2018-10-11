package com.sirma.itt.seip.resources.patches;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Deletes users with initial status. There was a bug (CMF-27410) that some users were created with ids that are
 * converted to lowercase, prior to that these users are created with INIT status and because the synchronization is
 * case sensitive it cannot sync them appropriately. Thats why we delete these users from relational db and afterwards
 * on server startup the synchronization will create them properly.
 *
 * @author smustafov
 */
public class DeleteUsersWithInitialStatus implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final Operation DELETE_OP = new Operation(ActionTypeConstants.DELETE);
	private static final Operation EDIT_OP = new Operation(ActionTypeConstants.EDIT_DETAILS);

	private ResourceService resourceService;
	private TransactionSupport transactionSupport;

	@Override
	public void setUp() throws SetupException {
		resourceService = CDI.instantiateBean(ResourceService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		transactionSupport.invokeInNewTx(this::executeInTx);
	}

	private void executeInTx() {
		List<User> users = resourceService.getAllUsers();

		for (User user : users) {
			Serializable status = user.get(DefaultProperties.STATUS);

			if (status == null) {
				LOGGER.warn("Found user with empty status: {}. Saving the user with active status.", user.getId());
				Resource resource = resourceService.getResource(user.getId());
				resource.add(DefaultProperties.STATUS, PrimaryStates.ACTIVE_KEY);
				resourceService.updateResource(resource, EDIT_OP);
			} else if (status.equals(PrimaryStates.INITIAL_KEY)) {
				LOGGER.info("Deleting {} user with INIT status", user.getId());
				resourceService.delete(user, DELETE_OP, true);
			}
		}
	}

	@Override
	public String getConfirmationMessage() {
		return "Deleted users with Initial status";
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
