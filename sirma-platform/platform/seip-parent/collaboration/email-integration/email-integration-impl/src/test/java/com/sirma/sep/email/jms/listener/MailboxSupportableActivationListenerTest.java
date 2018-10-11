package com.sirma.sep.email.jms.listener;

import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;
import static com.sirma.sep.email.EmailIntegrationConstants.FIRST_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.INSTANCE_ID;
import static com.sirma.sep.email.EmailIntegrationConstants.LAST_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.ZIMBRA_ACCOUNT_STATUS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.URIImpl;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.version.VersionMode;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.ConnectionFactory;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.sep.email.EmailIntegrationConstants;
import com.sirma.sep.email.EmailIntegrationConstants.EmailAccountStatus;
import com.sirma.sep.email.address.resolver.EmailAddress;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Tests for {@link MailboxSupportableActivationListener}.
 * 
 * @author svelikov
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class MailboxSupportableActivationListenerTest {

	@InjectMocks
	private MailboxSupportableActivationListener mailboxSupportableActivationListener;

	@Mock
	private EventService eventService;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private ConnectionFactory connectionFactory;

	@Mock
	private Instance<SenderService> senderService;

	@Mock
	private NamespaceRegistryService registryService;

	@Mock
	private EmailAddressResolver emailAddressResolver;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	@Mock
	private RepositoryConnection repositoryConnection;

	@Mock
	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	@Before
	public void setup() {
		mailboxSupportableActivationListener = new MailboxSupportableActivationListener();
		MockitoAnnotations.initMocks(this);

		when(emailIntegrationConfiguration.getTenantDomainAddress())
				.thenReturn(new ConfigurationPropertyMock("sirmaplatform.com"));
		when(emailIntegrationConfiguration.getTestEmailPrefix()).thenReturn(new ConfigurationPropertyMock("test"));

		mockMessageSender();

		when(securityContext.getCurrentTenantId()).thenReturn("tenant.com");

		EmailAddress emailAddress = new EmailAddress();
		emailAddress.setEmailAddress("project1-test@tenant.com");
		when(emailAddressResolver.getEmailAddress(anyString(), anyString())).thenReturn(emailAddress);

		loadInstanceWithEmailAddressProperty("emf:123456");
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void onMailboxSupportableChanged_for_doamin_class_throws_exception() throws JMSException {
		MapMessage message = Mockito.mock(MapMessage.class);
		when(message.getString(EmailIntegrationConstants.CLASS_NAME)).thenReturn("Project");

		TupleQuery query = mockRepositoryConnection(1);
		when(query.evaluate()).thenThrow(QueryEvaluationException.class);

		mailboxSupportableActivationListener.onMailboxSupportableChanged(message);
	}

	@Test
	public void onMailboxSupportableChanged_create_mailboxes_for_instances_without_email_address() throws JMSException {
		MapMessage message = mockMapMessage("Project", true);

		mockRepositoryConnection(1);

		when(registryService.getShortUri((IRI) getBindingSet().getBinding(SPARQLQueryHelper.OBJECT).getValue()))
				.thenReturn("emf:123456");

		MessageSender generateAddressQueueSender = mockMessageSender();

		when(emailAddressResolver.getEmailAddress(anyString(), anyString())).thenReturn(null);

		mailboxSupportableActivationListener.onMailboxSupportableChanged(message);

		// Message for generateEmailAddress queue should be sent
		Map<String, String> expectedAttributes = Collections.singletonMap(INSTANCE_ID, "emf:123456");
		verify(generateAddressQueueSender, times(1)).send(expectedAttributes);
	}

	@Test
	public void onMailboxSupportableChanged_update_instances_with_existing_mailboxes() throws JMSException {
		MapMessage message = mockMapMessage("Project", true);

		mockRepositoryConnection(1);

		when(registryService.getShortUri((IRI) getBindingSet().getBinding(SPARQLQueryHelper.OBJECT).getValue()))
				.thenReturn("emf:123456");

		MessageSender updateAccountQueueSender = mockMessageSender();

		mailboxSupportableActivationListener.onMailboxSupportableChanged(message);

		// Queue message should be sent with proper arguments
		Map<String, String> expectedAttributes = createAttributesMap("project1-test@tenant.com",
				EmailAccountStatus.ACTIVE);
		verify(updateAccountQueueSender, Mockito.times(1)).send(expectedAttributes);

		// Instance should be updated
		verify(domainInstanceService, times(1)).save(any(InstanceSaveContext.class));
	}

	@Test
	public void onMailboxSupportableChanged_deactivate_mailboxes_for_instances() throws JMSException {
		MapMessage message = mockMapMessage("Project", false);

		mockRepositoryConnection(1);
		when(registryService.getShortUri((IRI) getBindingSet().getBinding(SPARQLQueryHelper.OBJECT).getValue()))
				.thenReturn("emf:123456");

		MessageSender updateAccountQueueSender = mockMessageSender();

		loadInstanceWithEmailAddressProperty("emf:123456");

		ArgumentCaptor<InstanceSaveContext> argument = ArgumentCaptor.forClass(InstanceSaveContext.class);

		mailboxSupportableActivationListener.onMailboxSupportableChanged(message);

		// message should be sent to the queue
		Map<String, String> expectedAttributes = createAttributesMap("project1-test@tenant.com",
				EmailAccountStatus.CLOSED);
		verify(updateAccountQueueSender, times(1)).send(expectedAttributes);

		// instance should be updated without new version creation
		verify(domainInstanceService, times(1)).save(argument.capture());
		InstanceSaveContext saveContext = argument.getValue();
		assertEquals("emf:123456", saveContext.getInstanceId());
		assertEquals(ActionTypeConstants.EDIT_DETAILS, saveContext.getOperation().getOperation());
		assertEquals(VersionMode.NONE, saveContext.getVersionContext().getVersionMode());

		// the emailAddress property must be removed on deactivation
		assertNull(saveContext.getInstance().get(EMAIL_ADDRESS));
	}

	@Test
	public void onMailboxSupportableChanged_do_not_deactivate_mailboxes_for_instance_without_email()
			throws JMSException {
		MapMessage message = mockMapMessage("Project", false);

		mockRepositoryConnection(1);
		when(registryService.getShortUri((IRI) getBindingSet().getBinding(SPARQLQueryHelper.OBJECT).getValue()))
				.thenReturn("emf:123456");

		MessageSender updateAccountQueueSender = mockMessageSender();

		loadInstanceWithEmailAddressProperty("emf:123456");

		when(emailAddressResolver.getEmailAddress(anyString(), anyString())).thenReturn(null);
		mailboxSupportableActivationListener.onMailboxSupportableChanged(message);

		// message should not be sent to the queue
		verify(updateAccountQueueSender, times(0)).send(anyObject());

		// instance should not be updated
		verify(domainInstanceService, times(0)).save(anyObject());
	}

	@Test
	public void onMailboxSupportableChanged_no_instances_for_update_skip_processing() throws JMSException {
		MapMessage message = mockMapMessage("Project", false);

		mockRepositoryConnection(0);

		MessageSender updateAccountQueueSender = mockMessageSender();

		mailboxSupportableActivationListener.onMailboxSupportableChanged(message);

		verify(updateAccountQueueSender, never()).send(any());
		verify(domainInstanceService, never()).save(any());
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void onMailboxSupportableChanged_for_users_throws_exception() throws JMSException {
		MapMessage message = mockMapMessage("emf:User", true);

		TupleQuery query = mockRepositoryConnection(1);
		when(query.evaluate()).thenThrow(QueryEvaluationException.class);

		mailboxSupportableActivationListener.onMailboxSupportableChanged(message);
	}

	@Test
	public void onMailboxSupportableChanged_activate_mailboxes_for_users_with_email_addresses() throws JMSException {
		MapMessage message = mockMapMessage("emf:User", true);

		mockRepositoryConnection(1);
		when(registryService.getShortUri((IRI) getBindingSet().getBinding("instance").getValue()))
				.thenReturn("emf:User");

		MailboxSupportableActivationListener spy = Mockito.spy(mailboxSupportableActivationListener);

		MessageSender updateAccountQueueSender = mockMessageSender();

		EmailAddress emailAddress = new EmailAddress();
		emailAddress.setEmailAddress("project1-test@tenant.com");
		when(emailAddressResolver.getEmailAddress(anyString(), anyString())).thenReturn(emailAddress);
		loadInstanceWithEmailAddressProperty("emf:User");

		spy.onMailboxSupportableChanged(message);

		Map<String, String> expectedAttributes = createAttributesMap("project1-test@tenant.com",
				EmailAccountStatus.ACTIVE);
		verify(updateAccountQueueSender, times(1)).send(expectedAttributes);
	}

	@Test
	public void onMailboxSupportableChanged_create_mailboxes_for_users_without_email_addresses() throws JMSException {
		MapMessage message = mockMapMessage("emf:User", true);

		mockRepositoryConnection(1);
		when(registryService.getShortUri((IRI) getBindingSet().getBinding("instance").getValue()))
				.thenReturn("emf:User");

		MailboxSupportableActivationListener spy = Mockito.spy(mailboxSupportableActivationListener);

		mockMessageSender();

		when(emailAddressResolver.getEmailAddress(anyString(), anyString())).thenReturn(null);

		spy.onMailboxSupportableChanged(message);

		verify(eventService, times(1)).fire(Mockito.any(EmfEvent.class));
	}

	@Test
	public void onMailboxSupportableChanged_deactivate_user_mailboxes() throws JMSException {
		MapMessage message = mockMapMessage("emf:User", false);

		mockRepositoryConnection(1);
		when(registryService.getShortUri((IRI) getBindingSet().getBinding("instance").getValue()))
				.thenReturn("emf:User");

		loadInstanceWithEmailAddressProperty("emf:User");

		MessageSender updateAccountQueueSender = mockMessageSender();

		MailboxSupportableActivationListener spy = Mockito.spy(mailboxSupportableActivationListener);

		ArgumentCaptor<InstanceSaveContext> argument = ArgumentCaptor.forClass(InstanceSaveContext.class);

		spy.onMailboxSupportableChanged(message);

		// Update accounts message should be sent
		Map<String, String> expectedAttributes = createAttributesMap("project1-test@tenant.com",
				EmailAccountStatus.CLOSED);
		verify(updateAccountQueueSender, times(1)).send(expectedAttributes);

		// instance should be updated without new version creation
		verify(domainInstanceService, times(1)).save(argument.capture());
		InstanceSaveContext saveContext = argument.getValue();
		assertEquals("emf:User", saveContext.getInstanceId());
		assertEquals(ActionTypeConstants.EDIT_DETAILS, saveContext.getOperation().getOperation());
		assertEquals(VersionMode.NONE, saveContext.getVersionContext().getVersionMode());

		// the emailAddress property must be removed on deactivation
		assertNull(saveContext.getInstance().get(EMAIL_ADDRESS));
	}

	private TupleQuery mockRepositoryConnection(int resultsCount) {
		TupleQuery query = mock(TupleQuery.class);
		when(query.evaluate()).thenReturn(new TupleQueryResultStub(resultsCount, 0, null));
		when(connectionFactory.produceConnection()).thenReturn(repositoryConnection);
		when(repositoryConnection.prepareTupleQuery(eq(QueryLanguage.SPARQL), anyString())).thenReturn(query);
		return query;
	}

	private Map<String, String> createAttributesMap(String emailAddress, EmailAccountStatus status) {
		Map<String, String> attributes = CollectionUtils.createHashMap(2);
		attributes.put(ZIMBRA_ACCOUNT_STATUS, status.getStatus());
		attributes.put(EMAIL_ADDRESS, emailAddress);
		return attributes;
	}

	private MapMessage mockMapMessage(String className, boolean activate) throws JMSException {
		MapMessage message = Mockito.mock(MapMessage.class);
		when(message.getString(EmailIntegrationConstants.CLASS_NAME)).thenReturn(className);
		when(message.getBoolean(EmailIntegrationConstants.ACTIVATE)).thenReturn(activate);
		return message;
	}

	private void loadInstanceWithEmailAddressProperty(String instanceId) {
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("emailAddress", "project1-test@tenant.com");
		properties.put("title", "Instance Title");
		com.sirma.itt.seip.domain.instance.Instance mockInstance = mockInstance(instanceId, properties);
		when(domainInstanceService.loadInstance(instanceId)).thenReturn(mockInstance);
	}

	private MessageSender mockMessageSender() {
		SenderService senderServiceMock = mock(SenderService.class);
		when(senderService.get()).thenReturn(senderServiceMock);
		MessageSender sender = mock(MessageSender.class);
		when(senderServiceMock.createSender(anyString(), any(SendOptions.class))).thenReturn(sender);
		return sender;
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

	class ValueStub implements Value {

		@Override
		public String stringValue() {
			return "stellaDev@stella.com";
		}

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

	private com.sirma.itt.seip.domain.instance.Instance mockInstance(String instanceId,
			Map<String, Serializable> properties) {
		com.sirma.itt.seip.domain.instance.Instance instance = new EmfInstance();
		instance.setId(instanceId);
		if (properties != null) {
			instance.setProperties(properties);
		}
		return instance;
	}

}
