package com.sirma.itt.seip.resources.patches;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;
import com.sirma.itt.semantic.model.vocabulary.EMF;
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
 * Changes the current admin group to the one mapped to the idp and copies its members.
 *
 * @author smustafov
 */
public class ChangeAdminGroup implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String CHANGE_MEMBERSHIP_QUEUE = "java:/jms.queue.ChangeGroupMembership";

	private ResourceService resourceService;
	private SenderService senderService;
	private TransactionSupport transactionSupport;
	private SecurityConfiguration securityConfiguration;
	private SecurityContext securityContext;
	private ConfigurationManagement configurationManagement;

	@Override
	public void setUp() throws SetupException {
		resourceService = CDI.instantiateBean(ResourceService.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		senderService = CDI.instantiateBean(SenderService.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		securityConfiguration = CDI.instantiateBean(SecurityConfiguration.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		securityContext = CDI.instantiateBean(SecurityContext.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		configurationManagement = CDI.instantiateBean(ConfigurationManagement.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		transactionSupport.invokeInNewTx(this::executeInTx);
	}

	private void executeInTx() {
		String currentAdminGroupId = EMF.PREFIX + ":" + securityConfiguration.getAdminGroup().get();
		String systemAdminGroupId = (String) resourceService.getSystemAdminGroup().getId();

		if (!currentAdminGroupId.equals(systemAdminGroupId)) {
			MessageSender messageSender = senderService.createSender(CHANGE_MEMBERSHIP_QUEUE,
					SendOptions.create().asTenantAdmin());

			JsonObjectBuilder eventData = Json.createObjectBuilder();
			JSON.addIfNotNull(eventData, "group", systemAdminGroupId);
			JSON.addIfNotNull(eventData, "add", getUsersOfCurrentAdminGroup(currentAdminGroupId));

			messageSender.send(eventData.build().toString());

			changeAdminGroupConfiguration();
			LOGGER.info("Changed admin group from {} to {}", currentAdminGroupId, systemAdminGroupId);
		}

		deleteIdpAdminGroup();
	}

	private List<String> getUsersOfCurrentAdminGroup(String currentAdminGroupId) {
		List<String> users = resourceService.getContainedResources(currentAdminGroupId)
				.stream()
				.map(Instance::getId)
				.map(String.class::cast)
				.collect(Collectors.toList());

		// remove admin user, because he is already in the admin group and idp throws exception when trying to add him
		// again
		users.remove(securityConfiguration.getAdminUser().get().getSystemId());

		return users;
	}

	private void changeAdminGroupConfiguration() {
		Configuration adminConfiguration = new Configuration(
				securityConfiguration.getAdminGroup().getName(),
				resourceService.getSystemAdminGroup().getName(),
				securityContext.getCurrentTenantId());
		configurationManagement.updateConfiguration(adminConfiguration);
	}

	private void deleteIdpAdminGroup() {
		Resource adminGroup = resourceService.findResource("emf:GROUP_admin");
		if (adminGroup != null) {
			resourceService.delete(adminGroup, new Operation(ActionTypeConstants.DELETE, true), true);
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
