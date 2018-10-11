package com.sirma.itt.seip.tenant.db;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;

import javax.enterprise.inject.spi.BeanManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.stubbing.Answer;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.db.DatasourceProvisioner;
import com.sirma.itt.seip.db.patch.PatchService;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.mock.ConfigurationPropertyMock;
import com.sirma.itt.seip.tenant.wizard.SubsystemTenantAddressProvider;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.util.ReflectionUtils;

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
	private DbProvisioning dbProvisioning;

	@Mock
	private BeanManager beanManager;
	@Mock
	private ConfigurationManagement configurationManagement;
	@Mock
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();
	@Mock
	private DatasourceProvisioner datasourceProvisioner;

	private TenantInfo tenantInfo = new TenantInfo("tenant.com");

	@Before
	public void beforeMethod() throws RollbackedException {
		MockitoAnnotations.initMocks(this);
		DatabaseConfiguration databaseConfiguration = Mockito.mock(DatabaseConfiguration.class, (Answer<Object>) invocation -> {
			if (String.class.equals(invocation.getMethod().getReturnType())) {
				return "value";
			}
			return new ConfigurationPropertyMock<>("value");
		});
		ReflectionUtils.setFieldValue(provider, "databaseConfiguration", databaseConfiguration);

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
	public void provisionDb_defaultTenant() throws Exception {
		when(datasourceProvisioner.getXaDataSourceServerName(Matchers.anyString())).thenReturn("dataSourceServerName");
		when(datasourceProvisioner.getXaDataSourcePort(Matchers.anyString())).thenReturn("port");
		TenantRelationalContext context = new TenantRelationalContext();
		provider.provision(new HashMap<>(), new TenantInfo(SecurityContext.DEFAULT_TENANT), null, context);

		assertNotNull(context.getServerAddress());

		verify(dbProvisioning, never()).createDatabase(anyString(), anyString(), anyString(), anyString(), anyString(),
				anyString(), anyString());
	}

	@Test(expected = TenantCreationException.class)
	public void provisionDb_defaultTenant_NoDs() throws Exception {
		TenantRelationalContext context = new TenantRelationalContext();
		provider.provision(new HashMap<>(), new TenantInfo(SecurityContext.DEFAULT_TENANT), null, context);
	}

	@Test
	public void rollbackDb() throws Exception {
		TenantRelationalContext context = new TenantRelationalContext(URI.create("postgres://localhost:5432"), "user",
				"password", "db_name", "datasource");
		provider.rollback(context, null, tenantInfo, true);

		verify(dbProvisioning).dropDatabase(anyString(), anyString(), any(URI.class), anyString(), anyString());
	}

	@Test
	public void rollbackDb_notContext() throws Exception {
		provider.rollback(null, null, tenantInfo, true);

		verify(dbProvisioning, never()).dropDatabase(anyString(), anyString(), any(URI.class), anyString(),
				anyString());
	}

	@Test
	public void rollbackDb_defaultTenant() throws Exception {
		TenantRelationalContext context = new TenantRelationalContext(URI.create("postgres://localhost:5432"), "user",
				"password", "db_name", "datasource");
		provider.rollback(context, null, new TenantInfo(SecurityContext.DEFAULT_TENANT), false);

		verify(dbProvisioning, never()).dropDatabase(anyString(), anyString(), any(URI.class), anyString(),
				anyString());
	}

	@Test
	public void rollbackDb_noDS() throws Exception {
		TenantRelationalContext context = new TenantRelationalContext(URI.create("postgres://localhost:5432"), "user",
				"password", "db_name", null);
		provider.rollback(context, null, tenantInfo, true);

		verify(dbProvisioning).dropDatabase(anyString(), anyString(), any(URI.class), anyString(), anyString());
	}

	@Test
	public void rollbackDb_noAddress() throws Exception {
		TenantRelationalContext context = new TenantRelationalContext(null, "user", "password", "db_name",
				"datasource");
		provider.rollback(context, null, tenantInfo, true);

		verify(dbProvisioning, never()).dropDatabase(anyString(), anyString(), any(URI.class), anyString(),
				anyString());
	}

}
