package com.sirma.itt.seip.tenant.audit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;

import javax.enterprise.inject.spi.BeanManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.audit.step.AuditSolrSubsystemAddressProvider;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.rest.RandomPortGenerator;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Tests for {@link AuditSolrProvisioning}
 */
public class AuditSolrProvisioningTest {

	@InjectMocks
	private AuditSolrProvisioning solrProvisioning;

	@Mock
	private AuditConfiguration auditConfiguration;
	@Mock
	private AuditSolrSubsystemAddressProvider auditSolrAddressProvider;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Mock
	private BeanManager beanManager;
	@Mock
	private ConfigurationManagement configurationManagement;
	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	private static int port = RandomPortGenerator.generatePort(8900, 9000);

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(port);

	private TenantInfo tenantInfo = new TenantInfo("tenant.com");
	private TenantInfo defaultTenant = new TenantInfo(SecurityContext.DEFAULT_TENANT);

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(auditConfiguration.getSolrExternalAdminAddress())
				.thenReturn(new ConfigurationPropertyMock<>("http://localhost:" + port + "/solr"));
		when(auditConfiguration.getSolrAddress())
				.thenReturn(new ConfigurationPropertyMock<>("http://localhost:" + port + "/solr"));
		when(auditConfiguration.getSolrAdminAddress())
				.thenReturn(new ConfigurationPropertyMock<>("http://localhost:" + port + "/solr/admin"));
		when(configurationManagement.addConfigurations(anyCollection()))
				.thenAnswer(a -> a.getArgumentAt(0, Collection.class));

		when(auditSolrAddressProvider.provideAddressForNewTenant(any(String.class)))
				.thenReturn(URI.create("localahost"));
	}

	@Test
	public void provision() throws Exception {

		stubFor(post(urlEqualTo("/solr/config/upload")).willReturn(aResponse().withStatus(200)));
		stubFor(get(urlPathEqualTo("/solr/admin/cores")).willReturn(aResponse().withStatus(200)));

		solrProvisioning.provisionAuditModel(new HashMap<>(), tenantInfo, createContext());

		verify(configurationManagement).addConfigurations(anyCollection());
	}

	@Test
	public void provisionRecentActivities() throws Exception {
		stubFor(post(urlEqualTo("/solr/config/upload")).willReturn(aResponse().withStatus(200)));
		stubFor(get(urlPathEqualTo("/solr/admin/cores")).willReturn(aResponse().withStatus(200)));
		TenantRelationalContext context = createContext();
		solrProvisioning.provisionRecentActivitiesModel(tenantInfo);

		String coreName = context.getDatabaseName() + AuditSolrProvisioning.RECENT_ACTIVITIES_SUFFIX;
		String expectedUrl = "/solr/admin/cores?action=CREATE&name=" + coreName + "&configSet=" + coreName;

		verify(postRequestedFor(urlEqualTo("/solr/config/upload")));
		verify(getRequestedFor(urlEqualTo(expectedUrl)));
	}

	@Test(expected = RollbackedException.class)
	public void provision_noSolrForUpload() throws Exception {

		stubFor(post(urlEqualTo("/solr/config/upload")).willReturn(aResponse().withStatus(404)));
		stubFor(get(urlPathEqualTo("/solr/admin/cores")).willReturn(aResponse().withStatus(200)));

		solrProvisioning.provisionAuditModel(new HashMap<>(), tenantInfo, createContext());
	}

	@Test(expected = RollbackedException.class)
	public void provision_noAdminSolr() throws Exception {

		stubFor(post(urlEqualTo("/solr/config/upload")).willReturn(aResponse().withStatus(200)));
		stubFor(get(urlPathEqualTo("/solr/admin/cores")).willReturn(aResponse().withStatus(404)));

		solrProvisioning.provisionAuditModel(new HashMap<>(), tenantInfo, createContext());
	}

	@Test(expected = RollbackedException.class)
	public void provision_noConfigAdded() throws Exception {

		reset(configurationManagement);

		stubFor(post(urlEqualTo("/solr/config/upload")).willReturn(aResponse().withStatus(200)));
		stubFor(get(urlPathEqualTo("/solr/admin/cores")).willReturn(aResponse().withStatus(200)));

		solrProvisioning.provisionAuditModel(new HashMap<>(), tenantInfo, createContext());
	}

	@Test
	public void provision_defaultTenant() throws Exception {

		// the services should not be called - if called the test will fail
		stubFor(post(urlEqualTo("/solr/config/upload")).willReturn(aResponse().withStatus(404)));
		stubFor(get(urlPathEqualTo("/solr/admin/cores")).willReturn(aResponse().withStatus(404)));

		solrProvisioning.provisionAuditModel(new HashMap<>(), defaultTenant, createContext());

		verify(configurationManagement).addConfigurations(anyCollection());
	}

	@Test
	public void rollback() throws Exception {

		stubFor(get(urlPathEqualTo("/solr/admin/cores")).willReturn(aResponse().withStatus(200)));

		solrProvisioning.rollbackAuditCoreCreation(createContext(), tenantInfo);

		verify(configurationManagement, times(5)).removeConfiguration(any(String.class));
	}

	@Test
	public void rollbackRecentActivitiesCoreCreation() throws Exception {

		stubFor(get(urlPathEqualTo("/solr/admin/cores")).willReturn(aResponse().withStatus(500)));

		TenantRelationalContext context = createContext();
		solrProvisioning.rollbackRecentActivitiesCoreCreation(tenantInfo);

		String coreName = context.getDatabaseName() + AuditSolrProvisioning.RECENT_ACTIVITIES_SUFFIX;
		String expectedUrl = "/solr/admin/cores?action=UNLOAD&deleteInstanceDir=true&core=" + coreName;

		verify(getRequestedFor(urlEqualTo(expectedUrl)));
	}

	@Test
	public void rollback_defaultTenant() throws Exception {

		// the services should not be called - if called the test will fail
		stubFor(get(urlPathEqualTo("/solr/admin/cores")).willReturn(aResponse().withStatus(404)));

		solrProvisioning.rollbackAuditCoreCreation(createContext(), defaultTenant);

		verify(configurationManagement, times(5)).removeConfiguration(any(String.class));
	}

	@Test
	public void rollback_noAdminSolr() throws Exception {

		stubFor(get(urlPathEqualTo("/solr/admin/cores")).willReturn(aResponse().withStatus(404)));

		solrProvisioning.rollbackAuditCoreCreation(createContext(), tenantInfo);

		verify(configurationManagement, times(5)).removeConfiguration(any(String.class));
	}

	@Test
	public void rollback_couldNotRemoveConfig() throws Exception {

		stubFor(get(urlPathEqualTo("/solr/admin/cores")).willReturn(aResponse().withStatus(404)));

		doThrow(ConfigurationException.class).when(configurationManagement).removeConfiguration(any(String.class));

		solrProvisioning.rollbackAuditCoreCreation(createContext(), tenantInfo);

		verify(configurationManagement).removeConfiguration(any(String.class));
	}

	@Test
	public void coreExists() throws Exception {
		stubFor(get(urlPathEqualTo("/solr/tenant_name/select")).willReturn(aResponse().withStatus(200)));
		Assert.assertTrue(solrProvisioning.coreExists("tenant_name", tenantInfo));
	}

	@Test
	public void coreExists_coreAlreadyCreated() throws Exception {
		stubFor(get(urlPathEqualTo("/solr/tenant_name/select")).willReturn(aResponse().withStatus(404)));
		Assert.assertFalse(solrProvisioning.coreExists("tenant_name", tenantInfo));
	}

	private static TenantRelationalContext createContext() {
		TenantRelationalContext relationalContext = new TenantRelationalContext();
		relationalContext.setAccessUser("user");
		relationalContext.setAccessUserPassword("pass");
		relationalContext.setDatabaseName("tenant_com");
		relationalContext.setDatasourceName("java:jboss/datasources/tenant_com");
		relationalContext.setServerAddress(URI.create("postgres://localhost:5432"));
		return relationalContext;
	}

}
