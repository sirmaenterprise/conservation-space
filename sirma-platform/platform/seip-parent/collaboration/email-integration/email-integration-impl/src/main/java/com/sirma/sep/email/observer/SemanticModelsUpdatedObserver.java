package com.sirma.sep.email.observer;

import static com.sirma.sep.email.EmailIntegrationConstants.CLASS_NAME;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.event.SemanticDefinitionsReloaded;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.sep.email.EmailIntegrationConstants;
import com.sirma.sep.email.EmailIntegrationQueueNames;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.entity.MailboxSupportable;
import com.sirma.sep.email.event.CreateEmailAccountEvent;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.DomainAdministrationService;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Selects all instances of given type and for each one inserts property emf:mailboxAddress. Fires
 * {@link CreateEmailAccountEvent} to notify observers for the action.
 *
 * @author S.Djulgerova
 */
public class SemanticModelsUpdatedObserver implements Serializable {

	private static final long serialVersionUID = 2001718960953331611L;

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticModelsUpdatedObserver.class);

	private static final String USER = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#User";

	@Inject
	private DbDao dbDao;

	@Inject
	private Instance<SenderService> senderService;

	@Inject
	private NamespaceRegistryService registryService;

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private DomainAdministrationService domainAdministartionService;

	@Inject
	private Instance<EmailIntegrationConfiguration> emailIntegrationConfiguration;

	@Inject
	private RepositoryConnection repositoryConnection;

	/**
	 * This method intercepts {@link SemanticDefinitionsReloaded} and find all semantic classes marked as
	 * emf:mailboxSupportable. Extracted classes are compared with a list of classes stored in DB. If new class is
	 * marked as mailboxSupportable semantic is updated, mailboxes are created for all instances of given type and the
	 * class name is stored in the DB.
	 *
	 * @param event
	 *            observed event
	 */
	public void observeReloadDefinitionEvent(@Observes SemanticDefinitionsReloaded event) {
		Pair<List<String>, List<String>> forUpdate = buildChangeset();
		List<String> classesToAdd = forUpdate.getFirst();
		List<String> classesToRemove = forUpdate.getSecond();
		if (classesToAdd.isEmpty() && classesToRemove.isEmpty()) {
			return;
		}

		updateMailboxSupportable(classesToAdd, classesToRemove);

		createDomainIfNeed(classesToAdd);

		updateAccounts(forUpdate);
	}

	private Pair<List<String>, List<String>> buildChangeset() {
		Collection<String> newClasses = new ArrayList<>();
		for (ClassInstance classInstance : semanticDefinitionService.getClasses()) {
			if (classInstance.isMailboxSupportable()) {
				newClasses.add((String) classInstance.getId());
			}
		}
		Collection<String> existingClasses = dbDao.fetchWithNamed(MailboxSupportable.QUERY_MAILBOX_SUPPORTABLE_KEY,
				Collections.emptyList());

		List<String> classesToAdd = new ArrayList<>(newClasses);
		List<String> classesToRemove = new ArrayList<>(existingClasses);

		classesToAdd.removeAll(existingClasses);
		classesToRemove.removeAll(newClasses);

		// User should not be removed if there are other objects in the system which support email integration.
		// To prevent errors if user is removed than mailboxSupportable property is removed for all other objects.
		if (classesToRemove.contains(USER) && !newClasses.isEmpty()) {
			LOGGER.warn(
					"Mailbox support for {} is deactivated. To prevent errors in email integration module all other classes will be deactivated too.",
					USER);
			for (String clazz : newClasses) {
				String query = "DELETE DATA { " + registryService.getShortUri(clazz)
						+ " emf:isMailboxSupportable \"true\"^^xsd:boolean . }";
				executeUpdateQuery(query);
				LOGGER.warn("Mailbox support for class {} is deactivated.", clazz);
			}
			return new Pair<>(Collections.emptyList(), new ArrayList<>(existingClasses));
		}

		// Objects should not be marked as mailboxSupportable if user is not marked.
		// To prevent errors if some objects are added than mailboxSupportable property is added for user too
		if (!classesToAdd.isEmpty() && !classesToAdd.contains(USER) && existingClasses.isEmpty()) {
			executeUpdateQuery(
					ResourceLoadUtil.loadResource(SemanticModelsUpdatedObserver.class, "markUserQuery.sparql"));
			classesToAdd.add(USER);
		}
		return new Pair<>(classesToAdd, classesToRemove);
	}

	private void executeUpdateQuery(String query) {
		Update update = SPARQLQueryHelper.prepareUpdateQuery(repositoryConnection, query,
				CollectionUtils.emptyMap(), false);
		update.execute();
	}

	private void updateMailboxSupportable(List<String> classesToAdd, List<String> classesToRemove) {
		for (String uri : classesToAdd) {
			MailboxSupportable entity = new MailboxSupportable();
			entity.setClassName(uri);
			dbDao.saveOrUpdate(entity);
		}

		for (String uri : classesToRemove) {
			List<Pair<String, Object>> args = new ArrayList<>(1);
			args.add(new Pair<String, Object>(CLASS_NAME, uri));
			dbDao.executeUpdate(MailboxSupportable.QUERY_DELETE_MAILBOX_SUPPORTABLE_KEY, args);
		}
		LOGGER.debug("Mailbox supportable instances updated.");
	}

	private void createDomainIfNeed(List<String> classesToAdd) {
		if (!classesToAdd.isEmpty()) {
			if (emailIntegrationConfiguration.get().getTenantDomainAddress().isNotSet()) {
				throw new TenantCreationException(
						"Email integration can not be enabled for this tenant. The mandatory configuration "
								+ emailIntegrationConfiguration.get().getTenantDomainAddress().getName()
								+ " is missing. "
								+ "Please set the configuration before semantic update and try again.");
			} else {
				createEmailDomainForTenant();
			}
		}
	}

	private void updateAccounts(Pair<List<String>, List<String>> forUpdate) {
		LOGGER.debug("Begin updating email accounts for types: {}", forUpdate);
		SenderService sender = senderService.get();
		// Create accounts for instances of mailboxSupportable activated class
		for (String uri : forUpdate.getFirst()) {
			LOGGER.debug("Schedule mailboxSupportable activation for class: {}", uri);
			toggleMailboxSupportable(true, uri, sender);
		}
		// Remove accounts for every mailboxSupportable deactivated class
		for (String uri : forUpdate.getSecond()) {
			LOGGER.debug("Schedule mailboxSupportable deactivation for class: {}", uri);
			toggleMailboxSupportable(false, uri, sender);
		}
	}

	private void toggleMailboxSupportable(boolean activate, String uri, SenderService sender) {
		String className = registryService.getShortUri(uri);
		SendOptions sendOptions = createSendOptions();

		Map<String, Serializable> attributes = CollectionUtils.createHashMap(2);
		attributes.put(EmailIntegrationConstants.ACTIVATE, activate);
		attributes.put(CLASS_NAME, className);

		sender.send(EmailIntegrationQueueNames.ACTIVATE_MAILBOX_SUPPORTABLE_QUEUE, attributes, sendOptions);
	}

	protected SendOptions createSendOptions() {
		return SendOptions.create().asTenantAdmin();
	}

	/**
	 * Check if given domain exist and if not creates a domain in the mail server.
	 */
	private void createEmailDomainForTenant() {
		String tenantEmailDomain = emailIntegrationConfiguration.get().getTenantDomainAddress().get();
		try {
			if (!domainAdministartionService.getDomain(tenantEmailDomain).isPresent()) {
				domainAdministartionService.createDomain(tenantEmailDomain);
			}
		} catch (EmailIntegrationException e) {
			throw new TenantCreationException("Email integration can not be enabled for this tenant. "
					+ "Tenant domain address is not created. " + e.getMessage(), e);
		}
	}

}
