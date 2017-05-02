package com.sirma.itt.seip.tenant.db;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.dmr.ModelNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.db.patch.PatchService;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.mock.ConfigurationPropertyMock;
import com.sirma.itt.seip.tenant.wizard.SubsystemTenantAddressProvider;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.wildfly.WildflyControllerService;

/**
 * The Class RelationDbProvisioningTest.
 */
public class RelationDbProvisioningTest {

	/** The provisiner. */
	@InjectMocks
	private RelationDbProvisioning provider;

	@Spy
	protected ConfigurationProperty<String> dbDialect = new ConfigurationPropertyMock<>("postgresql");
	@Spy
	protected ConfigurationProperty<String> dbUser = new ConfigurationPropertyMock<>("admin");
	@Spy
	protected ConfigurationProperty<String> dbPass = new ConfigurationPropertyMock<>("5up3r@DM1N");
	@Mock
	private PatchService patchDbService;
	@Mock
	private SubsystemTenantAddressProvider addressProvider;
	@Mock
	private DatabaseConfiguration databaseConfiguration;
	@Mock
	private DbProvisioning dbProvisioning;

	@Mock
	private WildflyControllerService controller;

	@Mock
	private BeanManager beanManager;
	@Mock
	private ConfigurationManagement configurationManagement;
	@Mock
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	private TenantInfo tenantInfo = new TenantInfo("tenant.com");

	@Mock
	private ModelNode node;

	@Before
	public void beforeMethod() throws RollbackedException {
		MockitoAnnotations.initMocks(this);

		ModelNode node = mock(ModelNode.class);
		ModelNode outcome = mock(ModelNode.class);
		when(outcome.asString()).thenReturn("success");
		when(node.get("outcome")).thenReturn(outcome);
		when(controller.execute(any(ModelNode.class))).thenReturn(node);

		when(addressProvider.provideAddressForNewTenant(any(String.class)))
				.thenReturn(URI.create("postgres://localhost:5432"));
	}

	@Test
	public void provisionDb() throws Exception {
		TenantRelationalContext context = new TenantRelationalContext();
		provider.provision(new HashMap<>(), tenantInfo, context);

		assertNotNull(context.getAccessUser());
		assertNotNull(context.getAccessUserPassword());
		assertNotNull(context.getDatabaseName());
		assertNotNull(context.getDatasourceName());
		assertNotNull(context.getServerAddress());

		verify(dbProvisioning).createDatabase(anyString(), anyString(), anyString(), anyString(), anyString(),
				anyString(), anyString());
	}

	@Test
	public void provisionDb_defaultTenant() throws Exception {
		mockSuccess();
		mockProperty("localhost", "5432");

		TenantRelationalContext context = new TenantRelationalContext();
		provider.provision(new HashMap<>(), new TenantInfo(SecurityContext.DEFAULT_TENANT), context);

		assertNotNull(context.getServerAddress());

		verify(dbProvisioning, never()).createDatabase(anyString(), anyString(), anyString(), anyString(), anyString(),
				anyString(), anyString());
	}

	@Test(expected = TenantCreationException.class)
	public void provisionDb_defaultTenant_NoDs() throws Exception {
		mockSuccess();
		mockProperty(null, "5432");

		TenantRelationalContext context = new TenantRelationalContext();
		provider.provision(new HashMap<>(), new TenantInfo(SecurityContext.DEFAULT_TENANT), context);
	}

	@Test
	public void rollbackDb() throws Exception {
		TenantRelationalContext context = new TenantRelationalContext(URI.create("postgres://localhost:5432"), "user",
				"password", "db_name", "datasource");
		provider.rollback(context, tenantInfo);

		verify(dbProvisioning).dropDatabase(anyString(), anyString(), any(URI.class), anyString(), anyString());
	}

	@Test
	public void rollbackDb_notContext() throws Exception {
		provider.rollback(null, tenantInfo);

		verify(dbProvisioning, never()).dropDatabase(anyString(), anyString(), any(URI.class), anyString(),
				anyString());
	}

	@Test
	public void rollbackDb_defaultTenant() throws Exception {
		TenantRelationalContext context = new TenantRelationalContext(URI.create("postgres://localhost:5432"), "user",
				"password", "db_name", "datasource");
		provider.rollback(context, new TenantInfo(SecurityContext.DEFAULT_TENANT));

		verify(dbProvisioning, never()).dropDatabase(anyString(), anyString(), any(URI.class), anyString(),
				anyString());
	}

	@Test
	public void rollbackDb_noDS() throws Exception {
		TenantRelationalContext context = new TenantRelationalContext(URI.create("postgres://localhost:5432"), "user",
				"password", "db_name", null);
		provider.rollback(context, tenantInfo);

		verify(dbProvisioning).dropDatabase(anyString(), anyString(), any(URI.class), anyString(), anyString());
	}

	@Test
	public void rollbackDb_noAddress() throws Exception {
		TenantRelationalContext context = new TenantRelationalContext(null, "user", "password", "db_name",
				"datasource");
		provider.rollback(context, tenantInfo);

		verify(dbProvisioning, never()).dropDatabase(anyString(), anyString(), any(URI.class), anyString(),
				anyString());
	}

	private void mockSuccess() throws RollbackedException {
		ModelNode outcome = mock(ModelNode.class);
		when(outcome.asString()).thenReturn("success");
		when(node.get("outcome")).thenReturn(outcome);
		when(controller.execute(any(ModelNode.class))).thenReturn(node);
	}

	private void mockProperty(String valueToReturn, String... values) {
		ModelNode value = mock(ModelNode.class);
		when(value.asString()).thenReturn(valueToReturn, values);
		ModelNode result = mock(ModelNode.class);
		when(result.get("value")).thenReturn(value);
		when(node.get("result")).thenReturn(result);
	}
}
