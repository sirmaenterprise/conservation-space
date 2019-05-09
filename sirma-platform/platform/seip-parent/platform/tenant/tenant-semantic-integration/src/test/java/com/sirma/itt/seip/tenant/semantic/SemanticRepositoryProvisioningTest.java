package com.sirma.itt.seip.tenant.semantic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.emf.semantic.patch.SemanticPatchService;
import com.sirma.itt.emf.solr.configuration.SolrConfiguration;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.semantic.patch.BackingPatchService;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.SubsystemTenantAddressProvider;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;
import com.sirma.seip.semantic.events.SemanticModelUpdatedEvent;
import com.sirma.seip.semantic.management.RepositoryConfiguration;
import com.sirma.seip.semantic.management.RepositoryInfo;
import com.sirma.seip.semantic.management.RepositoryManagement;

/**
 * Tests for {@link SemanticRepositoryProvisioning}
 *
 * @author BBonev
 */
public class SemanticRepositoryProvisioningTest {

	@InjectMocks
	private SemanticRepositoryProvisioning repositoryProvisioning;

	@Mock
	private SubsystemTenantAddressProvider databaseProviderMock;
	@Mock
	private SubsystemTenantAddressProvider connectorProviderMock;

	@Spy
	private ConfigurationPropertyMock<SubsystemTenantAddressProvider> databaseProvider = new ConfigurationPropertyMock<>();
	@Spy
	private ConfigurationPropertyMock<SubsystemTenantAddressProvider> connectorProvider = new ConfigurationPropertyMock<>();

	@Spy
	private ConfigurationProperty<String> adminName = new ConfigurationPropertyMock<>("admin");
	@Spy
	private ConfigurationProperty<String> adminPassword = new ConfigurationPropertyMock<>("admin");

	@Mock
	private RepositoryManagement repositoryManagement;
	@Mock
	private ConfigurationManagement configurationManagement;
	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();
	@Mock
	private SemanticConfiguration semanticConfiguration;
	@Mock
	private SolrConfiguration solrConfiguration;
	@Mock
	private SemanticPatchService patchService;
	@Mock
	private RawConfigurationAccessor rawConfigurationAccessor;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Mock
	private BackingPatchService patchUtilService;
	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	private TenantInfo tenantInfo = new TenantInfo("tenant.com");
	private TenantInfo defaultTenant = new TenantInfo(SecurityContext.DEFAULT_TENANT);

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		databaseProvider.setValue(databaseProviderMock);
		connectorProvider.setValue(connectorProviderMock);

		when(configurationManagement.addConfigurations(anyCollection()))
				.then(a -> a.getArgumentAt(0, Collection.class));

		when(databaseProviderMock.provideAddressForNewTenant(any(String.class))).thenReturn(URI.create("localhost"));
		when(connectorProviderMock.provideAddressForNewTenant(any(String.class))).thenReturn(URI.create("localhost"));
	}

	@Test
	public void provisionSemanticRepo() throws Exception {
		Collection<TenantSemanticContext> context = new ArrayList<>();
		repositoryProvisioning.provision(new HashMap<>(), Collections.emptyList(), tenantInfo, context::add);

		assertFalse(context.isEmpty());
		TenantSemanticContext next = context.iterator().next();
		assertNotNull(next);
		assertEquals("tenant_com", next.getRepoName());
		assertNotNull(next.getSemanticAddress());

		verify(repositoryManagement).createRepository(any(RepositoryConfiguration.class));
	}

	@Test(expected = TenantCreationException.class)
	public void provisionSemanticRepo_alreadyExists() throws Exception {
		Collection<TenantSemanticContext> context = new ArrayList<>();

		when(repositoryManagement.isRepositoryExists(any(RepositoryInfo.class))).thenReturn(Boolean.TRUE);

		repositoryProvisioning.provision(new HashMap<>(), Collections.emptyList(), tenantInfo, context::add);
	}

	@Test(expected = TenantCreationException.class)
	public void provisionSemanticRepo_notCreated() throws Exception {
		Collection<TenantSemanticContext> context = new ArrayList<>();
		when(rawConfigurationAccessor.getRawConfigurationValue(any(String.class))).thenReturn("repo_name", "localhost");

		when(repositoryManagement.isRepositoryExists(any(RepositoryInfo.class))).thenReturn(Boolean.FALSE);

		repositoryProvisioning.provision(new HashMap<>(), Collections.emptyList(), defaultTenant, context::add);
	}

	@Test
	@SuppressWarnings("boxing")
	public void provisionSemanticRepo_defaultTenant() throws Exception {
		when(rawConfigurationAccessor.getRawConfigurationValue(any(String.class))).thenReturn("repo_name", "localhost");
		when(repositoryManagement.isRepositoryExists(any(RepositoryInfo.class))).thenReturn(Boolean.TRUE);

		Collection<TenantSemanticContext> context = new ArrayList<>();
		repositoryProvisioning.provision(new HashMap<>(), Collections.emptyList(), defaultTenant, context::add);

		assertFalse(context.isEmpty());
		TenantSemanticContext next = context.iterator().next();
		assertNotNull(next);
		assertEquals("repo_name", next.getRepoName());
		assertNotNull(next.getSemanticAddress());

		verify(repositoryManagement, never()).createRepository(any(RepositoryConfiguration.class));
	}

	@Test(expected = TenantCreationException.class)
	public void provisionSemanticRepo_defaultTenant_missingRepoName() throws Exception {
		when(rawConfigurationAccessor.getRawConfigurationValue(any(String.class))).thenReturn(null, "localhost");

		Collection<TenantSemanticContext> context = new ArrayList<>();
		repositoryProvisioning.provision(new HashMap<>(), Collections.emptyList(), defaultTenant, context::add);
	}

	@Test(expected = TenantCreationException.class)
	public void provisionSemanticRepo_defaultTenant_missingRepoAddress() throws Exception {
		when(rawConfigurationAccessor.getRawConfigurationValue(any(String.class))).thenReturn("repoName",
				(String) null);

		Collection<TenantSemanticContext> context = new ArrayList<>();
		repositoryProvisioning.provision(new HashMap<>(), Collections.emptyList(), defaultTenant, context::add);
	}

	@Test
	public void provisionSemanticRepo_withPatches() throws Exception {

		Collection<TenantSemanticContext> context = new ArrayList<>();
		File file = new File("");

		repositoryProvisioning.provision(new HashMap<>(), Arrays.asList(file), tenantInfo, context::add);

		assertFalse(context.isEmpty());
		TenantSemanticContext next = context.iterator().next();
		assertNotNull(next);
		assertEquals("tenant_com", next.getRepoName());
		assertNotNull(next.getSemanticAddress());

		verify(repositoryManagement).createRepository(any(RepositoryConfiguration.class));
		verify(patchUtilService).runPatchAndBackup(any(File.class), eq("tenant.com"));
		verify(semanticDefinitionService).modelUpdated();
	}

	@Test
	public void rollbackRepo() throws Exception {
		TenantSemanticContext context = new TenantSemanticContext();
		context.setRepoName("tenant_com");
		context.setSemanticAddress(URI.create("localhost"));
		repositoryProvisioning.rollback(context, tenantInfo);

		verify(repositoryManagement).deleteRepository(any());
	}

	@Test
	public void rollbackRepo_defaultTenant() throws Exception {
		TenantSemanticContext context = new TenantSemanticContext();
		context.setRepoName("tenant_com");
		context.setSemanticAddress(URI.create("localhost"));
		repositoryProvisioning.rollback(context, defaultTenant);

		verify(repositoryManagement, never()).deleteRepository(any());
	}

}
