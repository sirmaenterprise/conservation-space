package com.sirma.itt.seip.tenant.audit;

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

import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.emf.audit.patch.AuditDbPatchService;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.DbProvisioning;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tenant.wizard.SubsystemTenantAddressProvider;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.wildfly.WildflyControllerService;

/**
 * Tests for {@link AuditDbProvisioning}
 *
 * @author BBonev
 */
public class AuditDbProvisioningTest {

	/** The provisiner. */
	@InjectMocks
	private AuditDbProvisioning provider;

	@Spy
	protected ConfigurationProperty<String> dbDialect = new ConfigurationPropertyMock<>("postgresql");
	@Spy
	protected ConfigurationProperty<String> dbUser = new ConfigurationPropertyMock<>("admin");
	@Spy
	protected ConfigurationProperty<String> dbPass = new ConfigurationPropertyMock<>("5up3r@DM1N");
	@Mock
	private SubsystemTenantAddressProvider addressProvider;
	@Mock
	private AuditDbPatchService patchDbService;
	@Mock
	private AuditConfiguration auditConfiguration;
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
		provider.provision(new HashMap<>(), tenantInfo, null, context);

		assertNotNull(context.getAccessUser());
		assertNotNull(context.getAccessUserPassword());
		assertNotNull(context.getDatabaseName());
		assertNotNull(context.getDatasourceName());
		assertNotNull(context.getServerAddress());

		verify(dbProvisioning).createDatabase(anyString(), anyString(), anyString(), anyString(), anyString(),
				anyString(), anyString());
	}

	@Test
	public void provisionDb_reuse() throws Exception {
		TenantRelationalContext context = new TenantRelationalContext();
		TenantRelationalContext contextToUse = new TenantRelationalContext(URI.create("postgres://localhost:5432"),
				"user", "password", "db_name", "datasource");

		provider.provision(new HashMap<>(), tenantInfo, contextToUse, context);

		assertNotNull(context.getAccessUser());
		assertNotNull(context.getAccessUserPassword());
		assertNotNull(context.getDatabaseName());
		assertNotNull(context.getDatasourceName());
		assertNotNull(context.getServerAddress());

		verify(dbProvisioning, never()).createDatabase(anyString(), anyString(), anyString(), anyString(), anyString(),
				anyString(), anyString());
	}

	@Test
	public void rollbackDb() throws Exception {
		TenantRelationalContext context = new TenantRelationalContext(URI.create("postgres://localhost:5432"), "user",
				"password", "db_name", "datasource");
		provider.rollback(context, tenantInfo, true);

		verify(dbProvisioning).dropDatabase(anyString(), anyString(), any(URI.class), anyString(), anyString());
	}

	@Test
	public void rollbackDb_notContext() throws Exception {
		provider.rollback(new TenantRelationalContext(), tenantInfo, true);

		verify(dbProvisioning, never()).dropDatabase(anyString(), anyString(), any(URI.class), anyString(),
				anyString());
	}

	@Test
	public void rollbackDb_defaultTenant() throws Exception {
		TenantRelationalContext context = new TenantRelationalContext(URI.create("postgres://localhost:5432"), "user",
				"password", "db_name", "datasource");
		provider.rollback(context, new TenantInfo(SecurityContext.DEFAULT_TENANT), true);

		verify(dbProvisioning, never()).dropDatabase(anyString(), anyString(), any(URI.class), anyString(),
				anyString());
	}

	@Test
	public void rollbackDb_noDS() throws Exception {
		TenantRelationalContext context = new TenantRelationalContext(URI.create("postgres://localhost:5432"), "user",
				"password", "db_name", null);
		provider.rollback(context, tenantInfo, true);

		verify(dbProvisioning).dropDatabase(anyString(), anyString(), any(URI.class), anyString(), anyString());
	}

	@Test
	public void rollbackDb_notAllowed() throws Exception {
		TenantRelationalContext context = new TenantRelationalContext(URI.create("postgres://localhost:5432"), "user",
				"password", "db_name", "datasource");
		provider.rollback(context, tenantInfo, false);

		verify(dbProvisioning, never()).dropDatabase(anyString(), anyString(), any(URI.class), anyString(),
				anyString());
	}
}
