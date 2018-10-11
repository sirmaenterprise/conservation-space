package com.sirma.sep.email.jms.listener;

import static com.sirma.sep.email.EmailIntegrationConstants.CLASS_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.DISPLAY_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;
import static com.sirma.sep.email.EmailIntegrationConstants.FIRST_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.GIVEN_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.INSTANCE_ID;
import static com.sirma.sep.email.EmailIntegrationConstants.LAST_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.SN;
import static com.sirma.sep.email.EmailIntegrationConstants.ZIMBRA_ACCOUNT_STATUS;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.version.VersionMode;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.ReadOnly;
import com.sirma.sep.email.EmailIntegrationConstants;
import com.sirma.sep.email.EmailIntegrationConstants.EmailAccountStatus;
import com.sirma.sep.email.EmailIntegrationHelper;
import com.sirma.sep.email.EmailIntegrationQueueNames;
import com.sirma.sep.email.address.resolver.EmailAddress;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.event.CreateEmailAccountEvent;
import com.sirma.sep.email.observer.SemanticModelsUpdatedObserver;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Handles mailboxSupportable property change when models are changed. There are different scenarios which have to be
 * followed after such change. <br>
 * When the mailboxSupportable is activated for instances which don't have mailboxes, then a mailbox creation process is
 * triggered. Otherwise the mailboxes of the instances are activated and the emailAddress is set as a property again.
 * <br>
 * When the mailboxSupportable is deactivated, then the mailboxes are marked as closed which makes them inaccessible and
 * the emailAddress property is deleted from the instances.
 * 
 * @author svelikov
 */
public class MailboxSupportableActivationListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailboxSupportableActivationListener.class);

	private static final String COULD_NOT_INSERT_DATA = "Could not insert data. ";

	private static final int BATCH_SIZE = 1000;

	@Inject
	private EventService eventService;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private Instance<SenderService> senderService;

	@Inject
	private NamespaceRegistryService registryService;

	@Inject
	private EmailAddressResolver emailAddressResolver;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	@ReadOnly
	private RepositoryConnection repositoryConnection;

	@Inject
	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	/**
	 * When mailboxSupportable is changed for given class, then the instances of that type are updated according to if
	 * they has had mailbox or not. <br>
	 * Mailbox activation for users and instances is handled differently because of the way user's email addresses are
	 * generated.
	 * 
	 * @param msg
	 *            The payload message.
	 */
	@QueueListener(EmailIntegrationQueueNames.ACTIVATE_MAILBOX_SUPPORTABLE_QUEUE)
	public void onMailboxSupportableChanged(Message msg) {
		try {
			MapMessage message = (MapMessage) msg;
			String className = message.getString(CLASS_NAME);
			Boolean activate = message.getBoolean(EmailIntegrationConstants.ACTIVATE);
			if ("emf:User".equals(className)) {
				if (activate) {
					activateUserMailboxes();
				} else {
					deactivateUserMailboxes();
				}
			} else {
				if (activate) {
					activateObjectMailboxes(className);
				} else {
					deactivateObjectMailboxes(className);
				}
			}
		} catch (Exception e) {
			LOGGER.warn(
					"There's error during mailbox activation operation. Message will be redelivered and operation will be retried. "
							+ "If max delivery attemps limit is riched this message will be send to dead letters queue. "
							+ e.getMessage(),
					e);
			throw new RollbackedRuntimeException("Mailbox activation operation failed " + e.getMessage(), e);
		}
	}

	// Activation

	/**
	 * Collect all instances without emailAddress property and trigger batch update
	 * 
	 * @param className
	 *            the class marked as mailbox supportable
	 */
	private void activateObjectMailboxes(String className) {
		String tenantId = securityContext.getCurrentTenantId();

		String selectInstancesWithoutEmailAddressQuery = getSelectQuery(className, false);
		TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection,
				selectInstancesWithoutEmailAddressQuery, CollectionUtils.emptyMap(), false);

		try (TupleQueryResultIterator resultIterator = new TupleQueryResultIterator(tupleQuery.evaluate())) {

			Set<String> instanceIds = new LinkedHashSet<>();
			resultIterator.stream(false).forEach(bindingSet -> {
				if (instanceIds.size() == BATCH_SIZE) {
					batchObjectsUpdate(instanceIds, tenantId);
					instanceIds.clear();
				}
				IRI instanceUri = (IRI) bindingSet.getBinding(SPARQLQueryHelper.OBJECT).getValue();
				String instanceId = registryService.getShortUri(instanceUri);
				instanceIds.add(instanceId);
			});

			batchObjectsUpdate(instanceIds, tenantId);

		} catch (QueryEvaluationException e) {
			throw new SemanticPersistenceException(COULD_NOT_INSERT_DATA + e.getMessage(), e);
		}
	}

	private void batchObjectsUpdate(Collection<String> instanceIds, String tenantId) {
		if (CollectionUtils.isEmpty(instanceIds)) {
			return;
		}

		transactionSupport.invokeInNewTx(() -> handleObjectsUpdate(instanceIds, tenantId));
	}

	/**
	 * Every instance in the batch is checked if there is an email address registered in the resolver. When email
	 * address exists, then the respective mailbox has been closed and an activation operation is scheduled. Otherwise
	 * email address generation is scheduled which in turn triggers mailbox creation operation.
	 * 
	 * @param instanceIds
	 *            Batch of instance ids to be updated.
	 * @param tenantId
	 *            The tenant id in which context the operation should be executed.
	 */
	private void handleObjectsUpdate(Collection<String> instanceIds, String tenantId) {
		SendOptions sendOptions = SendOptions.create().asTenantAdmin();
		MessageSender generateAddressQueueSender = senderService.get()
				.createSender(EmailIntegrationQueueNames.GENERATE_EMAIL_ADDRESS_QUEUE, sendOptions);

		MessageSender updateAccountQueueSender = senderService.get()
				.createSender(EmailIntegrationQueueNames.UPDATE_EMAIL_ACCOUNT_QUEUE, sendOptions);

		for (String instanceId : instanceIds) {
			EmailAddress existingEmailAddress = emailAddressResolver.getEmailAddress(instanceId, tenantId);

			// When an object get's an account it's registered in the resolver. Accounts are unregistered from the
			// resolver only if the object is hard deleted for some reason.
			// The object hasn't had a mailbox before, so schedule creation operation.
			if (existingEmailAddress == null) {
				LOGGER.debug("Schedule email address generation for instances");
				Map<String, String> attributes = Collections.singletonMap(INSTANCE_ID, instanceId);
				generateAddressQueueSender.send(attributes);
			} else {
				// Otherwise trigger account activation and add the email address to the instance again.
				LOGGER.debug("Schedule email account activation for instances");
				Map<String, String> attributes = CollectionUtils.createHashMap(2);
				attributes.put(ZIMBRA_ACCOUNT_STATUS, EmailAccountStatus.ACTIVE.getStatus());
				attributes.put(EMAIL_ADDRESS, existingEmailAddress.getEmailAddress());
				updateAccountQueueSender.send(attributes);

				com.sirma.itt.seip.domain.instance.Instance instance = domainInstanceService.loadInstance(instanceId);
				instance.add(EMAIL_ADDRESS, existingEmailAddress.getEmailAddress());
				updateInstanceWithoutVersion(instance);
			}
		}
	}

	/**
	 * When mailboxSupportable is changed for users, then users are updated according to if they has had mailbox or not.
	 * <br>
	 * When a user didn't have email address registered in the resolver, then an address is generated and
	 * {@link CreateEmailAccountEvent} is fired which in turn triggers mailbox creation operation. The user is updated
	 * with emf:mailboxAddress property<br>
	 */
	private void activateUserMailboxes() {
		String tenantId = securityContext.getCurrentTenantId();

		SendOptions sendOptions = SendOptions.create().asTenantAdmin();
		MessageSender updateAccountQueueSender = senderService.get()
				.createSender(EmailIntegrationQueueNames.UPDATE_EMAIL_ACCOUNT_QUEUE, sendOptions);

		TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection,
				ResourceLoadUtil.loadResource(SemanticModelsUpdatedObserver.class, "selectExistingUsersQuery.sparql"),
				CollectionUtils.emptyMap(), false);

		try (TupleQueryResultIterator resultIterator = new TupleQueryResultIterator(tupleQuery.evaluate())) {
			for (BindingSet bindingSet : resultIterator) {
				IRI instanceUri = (IRI) bindingSet.getBinding(SPARQLQueryHelper.OBJECT).getValue();
				String instanceId = registryService.getShortUri(instanceUri);

				EmailAddress existingEmailAddress = emailAddressResolver.getEmailAddress(instanceId, tenantId);
				if (existingEmailAddress == null) {
					LOGGER.debug("Schedule email account creation for users");
					String generatedEmailAddress = EmailIntegrationHelper.generateEmailAddress(
							bindingSet.getBinding("title").getValue().stringValue(),
							emailIntegrationConfiguration.getTenantDomainAddress().get(),
							emailIntegrationConfiguration.getTestEmailPrefix().get());

					Map<String, String> attributes = new HashMap<>();
					attributes.put(GIVEN_NAME, bindingSet.getBinding(FIRST_NAME).getValue().stringValue());
					attributes.put(SN, bindingSet.getBinding(LAST_NAME).getValue().stringValue());
					attributes.put(DISPLAY_NAME,
							EmailIntegrationHelper.generateDisplayName(
									bindingSet.getBinding(FIRST_NAME).getValue().stringValue(),
									bindingSet.getBinding(LAST_NAME).getValue().stringValue()));
					eventService.fire(new CreateEmailAccountEvent(instanceId, generatedEmailAddress, attributes));
				} else {
					LOGGER.debug("Schedule email account activation for users");
					Map<String, String> attributes = CollectionUtils.createHashMap(2);
					attributes.put(ZIMBRA_ACCOUNT_STATUS, EmailAccountStatus.ACTIVE.getStatus());
					attributes.put(EMAIL_ADDRESS, existingEmailAddress.getEmailAddress());
					updateAccountQueueSender.send(attributes);

					com.sirma.itt.seip.domain.instance.Instance instance = domainInstanceService
							.loadInstance(instanceId);
					instance.add(EMAIL_ADDRESS, existingEmailAddress.getEmailAddress());
					updateInstanceWithoutVersion(instance);
				}

			}
		} catch (QueryEvaluationException e) {
			throw new SemanticPersistenceException(COULD_NOT_INSERT_DATA + e.getMessage(), e);
		}
	}

	// Deactivation

	private void deactivateObjectMailboxes(String className) {
		String selectInstancesWithEmailAddressQuery = getSelectQuery(className, true);
		TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection, selectInstancesWithEmailAddressQuery,
				CollectionUtils.emptyMap(), false);

		deactivateMailboxes(tupleQuery);
	}

	private void deactivateUserMailboxes() {
		TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection,
				ResourceLoadUtil.loadResource(SemanticModelsUpdatedObserver.class, "selectExistingUsersQuery.sparql"),
				CollectionUtils.emptyMap(), false);

		deactivateMailboxes(tupleQuery);
	}

	private void deactivateMailboxes(TupleQuery tupleQuery) {
		String tenantId = securityContext.getCurrentTenantId();

		try (TupleQueryResultIterator resultIterator = new TupleQueryResultIterator(tupleQuery.evaluate())) {

			Set<String> instanceIds = new LinkedHashSet<>();
			resultIterator.stream(false).forEach(bindingSet -> {
				if (instanceIds.size() == BATCH_SIZE) {
					triggerMailboxDeactivation(instanceIds, tenantId);
					instanceIds.clear();
				}
				IRI instanceUri = (IRI) bindingSet.getBinding(SPARQLQueryHelper.OBJECT).getValue();
				String instanceId = registryService.getShortUri(instanceUri);
				instanceIds.add(instanceId);
			});

			triggerMailboxDeactivation(instanceIds, tenantId);

		} catch (QueryEvaluationException e) {
			throw new SemanticPersistenceException(COULD_NOT_INSERT_DATA + e.getMessage(), e);
		}
	}

	private void triggerMailboxDeactivation(Collection<String> instanceIds, String tenantId) {
		if (CollectionUtils.isEmpty(instanceIds)) {
			return;
		}

		transactionSupport.invokeInNewTx(() -> {
			SendOptions sendOptions = SendOptions.create().asTenantAdmin();
			MessageSender updateEmailAccountQueueSender = senderService.get()
					.createSender(EmailIntegrationQueueNames.UPDATE_EMAIL_ACCOUNT_QUEUE, sendOptions);

			LOGGER.debug("Schedule email account deactivation for instances");
			for (String instanceId : instanceIds) {
				Map<String, String> attributes = CollectionUtils.createHashMap(2);
				attributes.put(ZIMBRA_ACCOUNT_STATUS, EmailAccountStatus.CLOSED.getStatus());
				EmailAddress emailAddress = emailAddressResolver.getEmailAddress(instanceId, tenantId);
				if (emailAddress == null) {
					LOGGER.warn("Email address is not found for instance with id " + instanceId);
					continue;
				}
				attributes.put(EMAIL_ADDRESS, emailAddress.getEmailAddress());
				updateEmailAccountQueueSender.send(attributes);

				com.sirma.itt.seip.domain.instance.Instance instance = domainInstanceService.loadInstance(instanceId);
				instance.remove(EMAIL_ADDRESS);
				// TODO: change the code to send emails for each instance that should be have it's mail support deactivated
				// do the save there and send a message to do actual deactivation (UPDATE_EMAIL_ACCOUNT_QUEUE)
				updateInstanceWithoutVersion(instance);
			}
		});
	}

	private String getSelectQuery(String className, boolean hasMailbox) {
		StringBuilder selectQuery = new StringBuilder();
		selectQuery.append("SELECT DISTINCT ?instance ?title WHERE { ?instance dcterms:title ?title . ?instance a ");
		selectQuery.append(className);
		selectQuery.append("; emf:isDeleted \"false\"^^xsd:boolean; FILTER (").append(hasMailbox ? "" : "NOT")
				.append(" EXISTS { ?instance emf:mailboxAddress ?mailboxAddress } )}");
		return selectQuery.toString();
	}

	private void updateInstanceWithoutVersion(com.sirma.itt.seip.domain.instance.Instance instance) {
		InstanceSaveContext saveContext = InstanceSaveContext
				.create(instance, new Operation(ActionTypeConstants.EDIT_DETAILS))
				.disableValidation("Validation should not block activation/deactivation of accounts");
		saveContext.getVersionContext().setVersionMode(VersionMode.NONE);
		domainInstanceService.save(saveContext);
	}

}
