package com.sirma.sep.email.observer;

import static com.sirma.sep.email.EmailIntegrationConstants.ACTIVATE;
import static com.sirma.sep.email.EmailIntegrationConstants.CLASS_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.FIRST_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.LAST_NAME;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.inject.Instance;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.event.SemanticDefinitionsReloaded;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.sep.email.EmailIntegrationQueueNames;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.configuration.EmailIntegrationConfigurationImpl;
import com.sirma.sep.email.entity.MailboxSupportable;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.domain.DomainInformation;
import com.sirma.sep.email.service.DomainAdministrationService;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Test for {@link SemanticModelsUpdatedObserver}.
 *
 * @author S.Djulgerova
 */
public class SemanticModelsUpdatedObserverTest {

	@InjectMocks
	private SemanticModelsUpdatedObserver observer;

	@Mock
	private DbDao dbDao;

	@Mock
	private javax.enterprise.inject.Instance<SenderService> senderService;

	@Mock
	protected NamespaceRegistryService registryService;

	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Mock
	private DomainAdministrationService domainAdministartionService;

	@Mock
	private javax.enterprise.inject.Instance<EmailIntegrationConfiguration> emailIntegrationConfiguration;

	@Mock
	private RepositoryConnection repositoryConnection;

	@Mock
	private DomainInformation DomainInformation;

	private EmailIntegrationConfiguration configuration;

	private SendOptions sendOptions = SendOptions.create().asTenantAdmin();

	@Before
	public void init() throws Exception {
		observer = new SemanticModelsUpdatedObserver() {
			@Override
			protected SendOptions createSendOptions() {
				return sendOptions;
			}
		};
		MockitoAnnotations.initMocks(this);

		configuration = mock(EmailIntegrationConfigurationImpl.class);
		when(emailIntegrationConfiguration.get()).thenReturn(configuration);
		when(configuration.getTenantDomainAddress()).thenReturn(new ConfigurationPropertyMock("sirmaplatform.com"));

		SenderService senderServiceMock = mock(SenderService.class);
		when(senderService.get()).thenReturn(senderServiceMock);
		MessageSender messageSender = mock(MessageSender.class);
		when(senderServiceMock.createSender(anyString(), any(SendOptions.class))).thenReturn(messageSender);

		when(repositoryConnection.prepareUpdate(Mockito.anyObject(), Mockito.anyString())).thenReturn(mock(Update.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void observeReloadDefinitionEvent_skip_processing_on_empty_changeset() throws EmailIntegrationException {
		when(semanticDefinitionService.getClasses()).thenReturn(new LinkedList<>());
		when(domainAdministartionService.getDomain("sirmaplatform.com")).thenReturn(Optional.of(DomainInformation));

		SenderService senderServiceMock = mock(SenderService.class);
		SemanticModelsUpdatedObserver observerSpy = spy(observer);

		observerSpy.observeReloadDefinitionEvent(new SemanticDefinitionsReloaded());

		// db call is not performed
		verify(dbDao, never()).executeUpdate(anyString(), anyList());
		verify(dbDao, never()).saveOrUpdate(Mockito.any(MailboxSupportable.class));
		// domain should not be created
		verify(domainAdministartionService, never()).createDomain(anyString());
		// message to the queue is never sent
		verify(senderServiceMock, never()).send(anyString(), anyList(), any(SendOptions.class));
	}

	@Test
	public void observeReloadDefinitionEvent_schedule_mailbox_supportable_activation()
			throws EmailIntegrationException {
		when(semanticDefinitionService.getClasses()).thenReturn(
				mockClassInstances("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project"));
		when(domainAdministartionService.getDomain("sirmaplatform.com")).thenReturn(Optional.of(DomainInformation));

		SenderService senderServiceMock = mock(SenderService.class);
		when(senderService.get()).thenReturn(senderServiceMock);
		MessageSender messageSender = mock(MessageSender.class);
		when(senderServiceMock.createSender(anyString(), any(SendOptions.class))).thenReturn(messageSender);

		when(registryService.getShortUri("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project"))
				.thenReturn("emf:Project");

		SemanticModelsUpdatedObserver observerSpy = spy(observer);

		observerSpy.observeReloadDefinitionEvent(new SemanticDefinitionsReloaded());

		Map<String, Serializable> attributes = new HashMap<>(2);
		attributes.put(CLASS_NAME, "emf:Project");
		attributes.put(ACTIVATE, true);
		verify(senderServiceMock, times(1)).send(EmailIntegrationQueueNames.ACTIVATE_MAILBOX_SUPPORTABLE_QUEUE,
				attributes, sendOptions);
	}

	@Test
	public void observeReloadDefinitionEvent_mark_class_and_user_as_mailbox_supportable()
			throws EmailIntegrationException {
		when(semanticDefinitionService.getClasses()).thenReturn(
				mockClassInstances("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project"));
		when(domainAdministartionService.getDomain("sirmaplatform.com")).thenReturn(Optional.of(DomainInformation));

		observer.observeReloadDefinitionEvent(new SemanticDefinitionsReloaded());

		// When a class is activated to ensure correct work of email integration module the user is activated too.
		// That's why we expect dbDao to be called twice.
		verify(dbDao, times(2)).saveOrUpdate(any(MailboxSupportable.class));
	}

	@Test
	public void observeReloadDefinitionEvent_mark_user_as_mailbox_supportable() throws EmailIntegrationException {
		when(semanticDefinitionService.getClasses())
				.thenReturn(mockClassInstances("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#User"));
		when(domainAdministartionService.getDomain("sirmaplatform.com")).thenReturn(Optional.of(DomainInformation));

		observer.observeReloadDefinitionEvent(new SemanticDefinitionsReloaded());

		verify(dbDao, times(1)).saveOrUpdate(any(MailboxSupportable.class));
	}

	@Test
	public void observeReloadDefinitionEvent_unmark_class_as_mailbox_supportable() throws EmailIntegrationException {
		when(semanticDefinitionService.getClasses()).thenReturn(new ArrayList<>());
		Collection<String> existingClasses = new ArrayList<>();
		existingClasses.add("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project");
		when(dbDao.fetchWithNamed(any(String.class), any(List.class))).thenReturn(existingClasses);
		when(domainAdministartionService.getDomain("sirmaplatform.com")).thenReturn(Optional.of(DomainInformation));

		observer.observeReloadDefinitionEvent(new SemanticDefinitionsReloaded());

		verify(dbDao, times(1)).executeUpdate(anyString(), Mockito.anyList());
	}

	@Test
	public void observeReloadDefinitionEvent_unmark_user_as_mailbox_supportable() throws EmailIntegrationException {
		when(semanticDefinitionService.getClasses()).thenReturn(
				mockClassInstances("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project"));
		Collection<String> existingClasses = new ArrayList<>();
		existingClasses.add("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#User");
		when(dbDao.fetchWithNamed(any(String.class), any(List.class))).thenReturn(existingClasses);
		when(domainAdministartionService.getDomain("sirmaplatform.com")).thenReturn(Optional.of(DomainInformation));

		observer.observeReloadDefinitionEvent(new SemanticDefinitionsReloaded());

		verify(dbDao, times(1)).executeUpdate(anyString(), Mockito.anyList());
	}

	@Test
	public void observeReloadDefinitionEvent_mark_and_unmark_classes_as_mailbox_supportable()
			throws EmailIntegrationException {
		when(semanticDefinitionService.getClasses())
				.thenReturn(mockClassInstances("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case"));
		Collection<String> existingClasses = new ArrayList<>();
		existingClasses.add("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project");
		when(dbDao.fetchWithNamed(any(String.class), any(List.class))).thenReturn(existingClasses);
		when(domainAdministartionService.getDomain("sirmaplatform.com")).thenReturn(Optional.of(DomainInformation));

		observer.observeReloadDefinitionEvent(new SemanticDefinitionsReloaded());

		verify(dbDao, times(1)).executeUpdate(anyString(), Mockito.anyList());
		verify(dbDao, times(1)).saveOrUpdate(any(MailboxSupportable.class));
	}

	@Test
	public void observeReloadDefinitionEvent_creates_new_domain() throws EmailIntegrationException {
		when(semanticDefinitionService.getClasses()).thenReturn(
				mockClassInstances("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project"));
		when(domainAdministartionService.getDomain("sirmaplatform.com")).thenReturn(Optional.empty());

		observer.observeReloadDefinitionEvent(new SemanticDefinitionsReloaded());

		verify(domainAdministartionService, times(1)).createDomain(anyString());
	}

	@Test
	public void observeReloadDefinitionEvent_doesnt_create_new_domain() throws EmailIntegrationException {
		when(semanticDefinitionService.getClasses()).thenReturn(
				mockClassInstances("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project"));
		when(domainAdministartionService.getDomain("sirmaplatform.com")).thenReturn(Optional.of(DomainInformation));

		observer.observeReloadDefinitionEvent(new SemanticDefinitionsReloaded());

		verify(domainAdministartionService, Mockito.never()).createDomain(anyString());
	}

	private List<ClassInstance> mockClassInstances(String id) {
		List<ClassInstance> instances = new LinkedList<>();
		ClassInstance project = new ClassInstance();
		project.setId(id);
		project.add("mailboxSupportable", true);
		instances.add(project);
		return instances;
	}

	public MapBindingSet getBindingSet() {
		MapBindingSet bindingSet = new MapBindingSet();
		bindingSet.addBinding("instance",
				new URIImpl("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project"));
		bindingSet.addBinding("user",
				new URIImpl("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#User"));
		bindingSet.addBinding("title", new ValueStub());
		bindingSet.addBinding(FIRST_NAME, new ValueStub());
		bindingSet.addBinding(LAST_NAME, new ValueStub());
		return bindingSet;
	}

	class TupleQueryResultStub implements TupleQueryResult {

		private int index = 0;
		private final int max;
		private final List<String> bindingNames;

		/**
		 * Instantiates a new tuple query result stub.
		 *
		 * @param elements
		 *            the elements
		 * @param initial
		 *            the initial
		 * @param bindingNames
		 *            the binding names
		 */
		TupleQueryResultStub(int elements, int initial, List<String> bindingNames) {
			max = elements;
			this.bindingNames = bindingNames;
		}

		@Override
		public void close() throws QueryEvaluationException {
		}

		@Override
		public boolean hasNext() throws QueryEvaluationException {
			return index < max;
		}

		@Override
		public BindingSet next() throws QueryEvaluationException {
			++index;
			return getBindingSet();
		}

		@Override
		public void remove() throws QueryEvaluationException {
			Assert.fail("Remove should not be called!");
		}

		@Override
		public List<String> getBindingNames() throws QueryEvaluationException {
			return bindingNames;
		}
	}

	class ValueStub implements Value {

		@Override
		public String stringValue() {
			return "stellaDev@stella.com";
		}

	}

}
