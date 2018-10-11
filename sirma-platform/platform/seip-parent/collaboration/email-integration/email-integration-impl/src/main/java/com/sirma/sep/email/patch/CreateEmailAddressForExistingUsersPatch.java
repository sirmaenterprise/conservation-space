package com.sirma.sep.email.patch;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;
import com.sirma.sep.email.EmailIntegrationConstants;
import com.sirma.sep.email.EmailIntegrationHelper;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.entity.MailboxSupportable;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Patch for users that do not have email address created, due to a bug - CMF-28499.
 *
 * @author smustafov
 */
public class CreateEmailAddressForExistingUsersPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	private ResourceService resourceService;

	private DbDao dbDao;

	private TransactionSupport transactionSupport;

	@Override
	public void setUp() throws SetupException {
		emailIntegrationConfiguration = CDI
				.instantiateBean(EmailIntegrationConfiguration.class, CDI.getCachedBeanManager(),
						CDI.getDefaultLiteral());
		resourceService = CDI
				.instantiateBean(ResourceService.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		dbDao = CDI.instantiateBean(DbDao.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		transactionSupport = CDI
				.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		Collection<String> mailboxSupportableClasses = dbDao
				.fetchWithNamed(MailboxSupportable.QUERY_MAILBOX_SUPPORTABLE_KEY, Collections.emptyList());
		if (mailboxSupportableClasses.isEmpty()) {
			LOGGER.info("Skipping users' email address generation, because email integration is disabled");
			return;
		}

		List<User> users = resourceService.getAllUsers();
		for (User user : users) {
			if (user.get(EmailIntegrationConstants.EMAIL_ADDRESS) == null) {
				LOGGER.info("Going to generate email address for user: {}", user.getId());
				Resource resource = resourceService.findResource(user.getId());
				transactionSupport.invokeInNewTx(() -> createEmailAddress(resource));
			}
		}
	}

	private void createEmailAddress(Resource user) {
		String emailAddress = EmailIntegrationHelper
				.generateEmailAddress(user.getName(), emailIntegrationConfiguration.getTenantDomainAddress().get(),
						emailIntegrationConfiguration.getTestEmailPrefix().get());
		user.add(EmailIntegrationConstants.EMAIL_ADDRESS, emailAddress);

		resourceService.updateResource(user, new Operation(ActionTypeConstants.EDIT_DETAILS));
	}

	@Override
	public String getConfirmationMessage() {
		return "Created email addresses of users";
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// Nothing to do here
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}

}