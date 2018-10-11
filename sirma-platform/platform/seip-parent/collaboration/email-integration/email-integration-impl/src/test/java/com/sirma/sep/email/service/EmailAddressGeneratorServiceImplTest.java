package com.sirma.sep.email.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.sep.email.address.resolver.EmailAddress;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.exception.EmailIntegrationException;

/**
 * Test for EmailAddressGeneratorServiceImpl
 *
 * @author svelikov
 */
public class EmailAddressGeneratorServiceImplTest {

	private static final String COMPANY_COM = "company.com";

	@InjectMocks
	private EmailAddressGeneratorService service;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private ExpressionsManager expressionsManager;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private EmailAddressResolver emailAddressResolver;

	@Mock
	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Before
	public void setup() {
		service = new EmailAddressGeneratorServiceImpl();
		MockitoAnnotations.initMocks(this);

		when(definitionService.getInstanceDefinition(any(Instance.class)))
				.thenReturn(createDefinitionModel());
		when(expressionsManager.evaluateRule(any(PropertyDefinition.class), any(), any(Instance.class)))
				.thenReturn("case-12-21-10-02-257");

		configureTestPrefix("test");

		configureTenantDomainAddress(COMPANY_COM);

		when(securityContext.getCurrentTenantId()).thenReturn(COMPANY_COM);

	}

	@Test(expected = EmailIntegrationException.class)
	public void generateEmailAddress_throws_error() throws EmailIntegrationException {
		when(emailAddressResolver.getEmailAddress("case-12-21-10-02-257-test@company.com"))
				.thenReturn(new EmailAddress());
		when(namespaceRegistryService.buildUri("emf:123456")).thenThrow(new IllegalArgumentException());

		service.generateEmailAddress(createInstance());
	}

	@Test
	public void generateEmailAddress_valid_data_test_mode_equal_domain_and_tenant() throws EmailIntegrationException {
		String emailAddress = service.generateEmailAddress(createInstance());
		Assert.assertEquals("case-12-21-10-02-257-test@company.com", emailAddress);
	}

	@Test
	public void generateEmailAddress_valid_data_test_mode_different_domain_and_tenant()
			throws EmailIntegrationException {
		configureTenantDomainAddress("email-domain.com");

		String emailAddress = service.generateEmailAddress(createInstance());

		assertEquals("case-12-21-10-02-257-test-company.com@email-domain.com", emailAddress);
	}

	@Test
	public void generateEmailAddress_valid_data_prod_mode_equal_domain_and_tenant() throws EmailIntegrationException {
		configureTestPrefix("");

		String emailAddress = service.generateEmailAddress(createInstance());

		assertEquals("case-12-21-10-02-257@company.com", emailAddress);
	}

	@Test
	public void generateEmailAddress_valid_data_prod_mode_different_domain_and_tenant()
			throws EmailIntegrationException {
		configureTenantDomainAddress("email-domain.com");
		configureTestPrefix("");

		String emailAddress = service.generateEmailAddress(createInstance());

		assertEquals("case-12-21-10-02-257-company.com@email-domain.com", emailAddress);
	}

	@Test
	public void generateEmailAddress_local_part_too_long() throws EmailIntegrationException {
		when(expressionsManager.evaluateRule(any(PropertyDefinition.class), any(), any(Instance.class))).thenReturn(
				"case-12-21-10-02-257-12-21-10-02-257-12-21-10-02-257-123456-257-12-21-10-02-257-12-21-10-02-257-12-21-10-02-257");

		String emailAddress = service.generateEmailAddress(createInstance());

		String expected = "case-12-21-10-02-257-12-21-10-02-257-12-21-10-02-257-123456-test@company.com";
		assertEquals(expected, emailAddress);

		String localPart = emailAddress.split("@")[0];
		assertEquals(64, localPart.length());
	}

	@Test
	public void generateEmailAddress_invalid_chars_and_consecutive_separators_in_generated_part()
			throws EmailIntegrationException {
		when(expressionsManager.evaluateRule(any(PropertyDefinition.class), any(), any(Instance.class)))
				.thenReturn("case-12-21-10-02-257 10:56, @CA*123 ");

		String emailAddress = service.generateEmailAddress(createInstance());
		assertEquals("case-12-21-10-02-257-10-56-CA-123-test@company.com", emailAddress);
	}

	@Test
	public void generateEmailAddress_with_fallback_generated_part() throws EmailIntegrationException {
		when(expressionsManager.evaluateRule(any(PropertyDefinition.class), any(), any(Instance.class))).thenReturn("");
		when(expressionsManager.evaluateRule("${seq({+project})}", String.class)).thenReturn("1");
		IRIMock iriMock = new IRIMock();
		iriMock.setLocalName("project");
		when(namespaceRegistryService.buildUri("emf:123456")).thenReturn(iriMock);

		String emailAddress = service.generateEmailAddress(createInstance());

		assertEquals("project-1-test@company.com", emailAddress);
	}

	@Test
	public void generateEmailAddress_when_collision_is_found_sequence_id_should_be_added()
			throws EmailIntegrationException {
		configureTenantDomainAddress("email-domain.com");
		when(emailAddressResolver.getEmailAddress("case-12-21-10-02-257-test-company.com@email-domain.com"))
				.thenReturn(new EmailAddress());
		IRIMock iriMock = new IRIMock();
		iriMock.setLocalName("project");
		when(namespaceRegistryService.buildUri("emf:123456")).thenReturn(iriMock);
		when(expressionsManager.evaluateRule("${seq({+project})}", String.class)).thenReturn("1");

		String emailAddress = service.generateEmailAddress(createInstance());

		assertEquals("case-12-21-10-02-257-1-test-company.com@email-domain.com", emailAddress);
	}

	private void configureTenantDomainAddress(String domain) {
		ConfigurationProperty<String> tenantDomain = new ConfigurationPropertyMock<>(domain);
		when(emailIntegrationConfiguration.getTenantDomainAddress()).thenReturn(tenantDomain);
	}

	private void configureTestPrefix(String prefix) {
		ConfigurationProperty<String> prefixConfiguration = new ConfigurationPropertyMock<>(prefix);
		when(emailIntegrationConfiguration.getTestEmailPrefix()).thenReturn(prefixConfiguration);
	}

	private static DefinitionModel createDefinitionModel() {
		DefinitionModelMock definitionModel = new DefinitionModelMock();
		PropertyDefinitionMock emailAddress = new PropertyDefinitionMock();
		emailAddress.setName("emailAddress");
		emailAddress.setRnc("${eval(${CL(${id.type})}-${get([identifier])})}");
		definitionModel.getFields().add(emailAddress);
		return definitionModel;
	}

	private Instance createInstance() {
		Instance instance = new EmfInstance();
		instance.add("id", "emf:123456");
		InstanceTypeMock type = new InstanceTypeMock();
		type.setId("emf:123456");
		instance.setType(type);
		return instance;
	}

	private static class DefinitionModelMock implements DefinitionModel {

		private static final long serialVersionUID = 1L;
		private List<PropertyDefinition> fields = new LinkedList<>();

		@Override
		public Integer getHash() {
			return null;
		}

		@Override
		public void setHash(Integer hash) {
			// empty
		}

		@Override
		public boolean hasChildren() {
			return false;
		}

		@Override
		public Node getChild(String name) {
			return null;
		}

		@Override
		public String getIdentifier() {
			return null;
		}

		@Override
		public void setIdentifier(String identifier) {
			// empty
		}

		@Override
		public List<PropertyDefinition> getFields() {
			return fields;
		}

		@Override
		public Long getRevision() {
			return null;
		}

		@Override
		public String getType() {
			return null;
		}

	}

	private class InstanceTypeMock implements InstanceType {

		private Serializable id;

		public void setId(Serializable id) {
			this.id = id;
		}

		@Override
		public Serializable getId() {
			return id;
		}

		@Override
		public String getCategory() {
			return null;
		}

		@Override
		public Set<InstanceType> getSuperTypes() {
			return null;
		}

		@Override
		public Set<InstanceType> getSubTypes() {
			return null;
		}

		@Override
		public boolean isVersionable() {
			return false;
		}

		@Override
		public boolean hasTrait(String trait) {
			return false;
		}

		@Override
		public String getProperty(String propertyName) {
			return null;
		}

	}

	private class IRIMock implements IRI {

		String localName;

		public void setLocalName(String localName) {
			this.localName = localName;
		}

		@Override
		public String stringValue() {
			return null;
		}

		@Override
		public String getNamespace() {
			return null;
		}

		@Override
		public String getLocalName() {
			return localName;
		}
	}
}