package com.sirma.itt.seip.resources.patches;

import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Updates users in remote store. Thats in order to init the new claim mapping for isActive field in the idp.
 *
 * @author smustafov
 */
public class UpdateUsersInRemoteStore implements CustomTaskChange {

	private static final String UPDATE_REMOTE_USER_ACCOUNT = "java:/jms.queue.UpdateRemoteUserAccount";

	private ResourceService resourceService;
	private SenderService senderService;
	private TransactionSupport transactionSupport;

	@Override
	public void setUp() throws SetupException {
		resourceService = CDI.instantiateBean(ResourceService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		senderService = CDI.instantiateBean(SenderService.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		transactionSupport.invokeInNewTx(() -> {
			MessageSender messageSender = senderService.createSender(UPDATE_REMOTE_USER_ACCOUNT,
					SendOptions.create().asTenantAdmin());

			resourceService.getAllUsers().forEach(user -> messageSender.sendText(user.getId().toString()));
		});
	}

	@Override
	public String getConfirmationMessage() {
		return "Users are updated in remote store";
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
