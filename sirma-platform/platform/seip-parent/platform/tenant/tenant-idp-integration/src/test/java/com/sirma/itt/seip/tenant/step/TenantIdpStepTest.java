package com.sirma.itt.seip.tenant.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.provision.IdpTenantInfo;
import com.sirma.itt.seip.tenant.provision.IdpTenantProvisioning;
import com.sirma.itt.seip.tenant.wizard.TenantDeletionContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Tests for {@link TenantIdpStep}.
 *
 * @author smustafov
 */
public class TenantIdpStepTest {

	private static final String TENANT_ID = "sep.test";
	private static final String TENANT_DISPLAY_NAME = "tenant display name";
	private static final String TENANT_DESCRIPTION = "tenant description";

	@InjectMocks
	private TenantIdpStep idpStep;

	@Mock
	private SecurityContextManager securityContextManager;
	@Mock
	private SecurityConfiguration securityConfiguration;
	@Mock
	private ConfigurationManagement configurationManagement;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();

	private Collection<IdpTenantProvisioning> plugins = new ArrayList<>();
	@Spy
	private Plugins<IdpTenantProvisioning> availableIdpProvisioners = new Plugins<>("", plugins);

	@Mock
	private IdpTenantProvisioning idpTenantProvisioning;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);

		plugins.clear();
		mockConfigurations();
	}

	@Test(expected = TenantCreationException.class)
	public void should_ThrowException_When_NoPasswordEntered() throws Exception {
		TenantStepData data = buildStepData("missing-password.json");
		TenantInitializationContext context = buildContext(null);

		idpStep.execute(data, context);
	}

	@Test
	public void should_ProvisionWso_ByDefault() throws Exception {
		TenantStepData data = buildStepData("valid-model.json");
		TenantInitializationContext context = buildContext("admin");

		withIdpImplementation("wso2Idp");

		idpStep.execute(data, context);

		verifyProvisioning("admin", "qwerty", "admin@" + TENANT_ID, "System", "Administrator");
		assertTrue(data.isCompleted());
	}

	@Test
	public void should_ProvisionKeycloak_When_Chosen() throws Exception {
		TenantStepData data = buildStepData("keycloak-valid-model.json");
		TenantInitializationContext context = buildContext("admin");

		withIdpImplementation("keycloak");

		idpStep.execute(data, context);

		verifyProvisioning("admin", "qwerty", "admin@mail.com", "Tenant", "Admin");
		assertTrue(data.isCompleted());
	}

	@Test(expected = EmfRuntimeException.class)
	public void should_ThrowException_When_IdpImplMissing() throws Exception {
		TenantStepData data = buildStepData("valid-model.json");
		TenantInitializationContext context = buildContext("admin");

		withIdpImplementation("keycloak");

		idpStep.execute(data, context);
	}

	@Test
	public void should_DeleteTenant_When_ProvisioningFails() throws Exception {
		TenantStepData data = buildStepData("keycloak-valid-model.json");
		TenantInitializationContext context = buildContext("admin");

		withIdpImplementation("keycloak");
		doThrow(new EmfRuntimeException()).when(idpTenantProvisioning).provision(any());

		try {
			idpStep.execute(data, context);
		} catch (EmfRuntimeException e) {
			verify(idpTenantProvisioning).delete(TENANT_ID);
		}
	}

	@Test
	public void delete_Should_DoNothing_When_IdpStepNotCompleted() throws Exception {
		TenantStepData data = buildStepData("valid-model.json");
		withIdpImplementation("wso2Idp");

		idpStep.delete(data, buildDeletionContext(true, "wso2Idp"));

		verify(idpTenantProvisioning, never()).delete(TENANT_ID);
	}

	@Test
	public void delete_Should_DeleteTenant() throws Exception {
		TenantStepData data = buildStepData("valid-model.json");
		data.completedSuccessfully();

		withIdpImplementation("wso2Idp");

		idpStep.delete(data, buildDeletionContext(false, "wso2Idp"));

		verify(idpTenantProvisioning).delete(TENANT_ID);
	}

	@Test
	public void should_HaveIdentifier() {
		assertEquals("IdpInitialization", idpStep.getIdentifier());
	}

	private void verifyProvisioning(String adminUsername, String password, String mail, String firstName,
			String lastName) {
		ArgumentCaptor<IdpTenantInfo> argumentCaptor = ArgumentCaptor.forClass(IdpTenantInfo.class);
		verify(idpTenantProvisioning).provision(argumentCaptor.capture());

		IdpTenantInfo tenantInfo = argumentCaptor.getValue();
		assertEquals(TENANT_ID, tenantInfo.getTenantId());
		assertEquals(TENANT_DISPLAY_NAME, tenantInfo.getTenantDisplayName());
		assertEquals(TENANT_DESCRIPTION, tenantInfo.getTenantDescription());
		assertEquals(adminUsername + "@" + TENANT_ID, tenantInfo.getAdminUsername());
		assertEquals(password, tenantInfo.getAdminPassword());
		assertEquals(mail, tenantInfo.getAdminMail());
		assertEquals(firstName, tenantInfo.getAdminFirstName());
		assertEquals(lastName, tenantInfo.getAdminLastName());
	}

	private TenantStepData buildStepData(String modelName) throws Exception {
		return new TenantStepData(idpStep.getIdentifier(), getJsonModel(modelName));
	}

	private JSONObject getJsonModel(String name) throws Exception {
		Path path = Paths.get(getClass().getResource(name).toURI());
		String jsonString = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		return new JSONObject(jsonString);
	}

	private static TenantInitializationContext buildContext(String adminUser) {
		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo info = new TenantInfo(TENANT_ID, TENANT_DISPLAY_NAME, TENANT_DESCRIPTION);
		context.setTenantInfo(info);
		context.setAdminUser(adminUser + "@" + TENANT_ID);
		return context;
	}

	private void withIdpImplementation(String idp) {
		when(idpTenantProvisioning.isApplicable(idp)).thenReturn(true);
		plugins.add(idpTenantProvisioning);
	}

	private void mockConfigurations() {
		when(securityConfiguration.getAdminUserName()).thenReturn(new ConfigurationPropertyMock<>("admin"));
		when(securityConfiguration.getAdminPasswordConfiguration()).thenReturn("password");
		when(securityConfiguration.getAdminGroup()).thenReturn(new ConfigurationPropertyMock<>("admin"));
		when(securityConfiguration.getIdpProviderName()).thenReturn(new ConfigurationPropertyMock<>());
	}

	private TenantDeletionContext buildDeletionContext(boolean rollback, String idpProvider) {
		ConfigurationPropertyMock<String> propertyMock = new ConfigurationPropertyMock<>();
		propertyMock.setName("idp.provider");
		when(securityConfiguration.getIdpProviderName()).thenReturn(propertyMock);

		TenantDeletionContext deletionContext = new TenantDeletionContext(new TenantInfo(TENANT_ID), rollback);
		Configuration configuration = new Configuration("idp.provider", idpProvider);
		configuration.setRawValue(idpProvider);
		deletionContext.setConfigurations(Collections.singletonList(configuration));
		return deletionContext;
	}

}
