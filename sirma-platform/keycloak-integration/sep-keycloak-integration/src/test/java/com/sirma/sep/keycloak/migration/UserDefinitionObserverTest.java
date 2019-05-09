package com.sirma.sep.keycloak.migration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.synchronization.RemoteUserStoreAdapterProxy;
import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.Tenant;
import com.sirma.itt.seip.tenant.context.TenantManager;
import com.sirma.itt.seip.tenant.provision.IdpTenantInfo;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;

/**
 * Tests for {@link UserDefinitionObserver}.
 *
 * @author smustafov
 */
public class UserDefinitionObserverTest {

	private static final String TENANT_ID = "sep.test";
	private static final String TENANT_DISPLAY_NAME = "Test SEP";
	private static final String TENANT_DESCRIPTION = "Tenant for testing";

	private static final String WSO_USER_NAME = "urn:scim:schemas:core:1.0:userName";
	private static final String WSO_FIRST_NAME = "urn:scim:schemas:core:1.0:name.givenName";
	private static final String WSO_LAST_NAME = "urn:scim:schemas:core:1.0:name.familyName";
	private static final String WSO_EMAIL = "urn:scim:schemas:core:1.0:emails";
	private static final String WSO_COUNTRY = "urn:scim:schemas:core:1.0:country";

	private static final String KEYCLOAK_FIRST_NAME = "firstName";
	private static final String KEYCLOAK_LAST_NAME = "lastName";
	private static final String KEYCLOAK_EMAIL = "email";
	private static final String KEYCLOAK_COUNTRY = "country";

	@InjectMocks
	private UserDefinitionObserver observer;

	@Mock
	private UserPreferences userPreferences;

	@Mock
	private SecurityConfiguration securityConfiguration;

	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	@Mock
	private ConfigurationManagement configurationManagement;

	@Mock
	private RemoteUserStoreAdapterProxy userStoreAdapterProxy;

	@Mock
	private JwtConfiguration jwtConfiguration;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private ResourceService resourceService;

	@Mock
	private KeycloakTenantMigration keycloakTenantMigration;

	@Mock
	private TenantManager tenantManager;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		observer.initialize();

		when(securityConfiguration.getSystemAdminUsername()).thenReturn("systemadmin");
		when(securityConfiguration.getAdminUserPassword()).thenReturn(new ConfigurationPropertyMock<>("test"));
		when(securityContextManager.getCurrentContext().getCurrentTenantId()).thenReturn(TENANT_ID);

		Tenant tenant = new Tenant();
		tenant.setTenantId(TENANT_ID);
		tenant.setDisplayName(TENANT_DISPLAY_NAME);
		tenant.setDescription(TENANT_DESCRIPTION);
		when(tenantManager.getTenant(TENANT_ID)).thenReturn(Optional.of(tenant));

		when(jwtConfiguration.getRevocationTimeConfig()).thenReturn(new ConfigurationPropertyMock<>());

		when(userPreferences.getSessionTimeout()).thenReturn(1800);
	}

	@Test
	public void should_NotRunMigration_When_MappingsAreNotMigrated() {
		mockIdpConfig(SecurityConfiguration.WSO_IDP);
		mockDefinition(Arrays.asList(WSO_USER_NAME, WSO_FIRST_NAME, WSO_LAST_NAME, WSO_EMAIL));

		observer.onDefinitionChange(null);

		verify(keycloakTenantMigration, never()).provision(any());
	}

	@Test
	public void should_NotRunMigration_When_IdpConfigIsSetToKeycloak() {
		mockIdpConfig(SecurityConfiguration.KEYCLOAK_IDP);
		mockDefinition(Arrays.asList(WSO_USER_NAME, WSO_FIRST_NAME, WSO_LAST_NAME, WSO_EMAIL));

		observer.onDefinitionChange(null);

		verify(keycloakTenantMigration, never()).provision(any());
	}

	@Test
	public void should_NotRunMigration_When_AlreadyMigrated() {
		mockIdpConfig(SecurityConfiguration.KEYCLOAK_IDP);
		mockDefinition(Arrays.asList(KEYCLOAK_FIRST_NAME, KEYCLOAK_LAST_NAME, KEYCLOAK_EMAIL));

		observer.onDefinitionChange(null);

		verify(keycloakTenantMigration, never()).provision(any());
	}

	@Test
	public void should_NotRunMigration_When_OnlyPartOfTheFieldsAreMigrated() {
		mockIdpConfig(SecurityConfiguration.WSO_IDP);
		mockDefinition(Arrays.asList(KEYCLOAK_FIRST_NAME, WSO_LAST_NAME, WSO_EMAIL));

		observer.onDefinitionChange(null);

		verify(keycloakTenantMigration, never()).provision(any());
	}

	@Test
	public void should_NotRunMigration_When_OnlyOneFieldIsNotMigrated() {
		mockIdpConfig(SecurityConfiguration.WSO_IDP);
		mockDefinition(Arrays.asList(KEYCLOAK_FIRST_NAME, KEYCLOAK_LAST_NAME, WSO_EMAIL));

		observer.onDefinitionChange(null);

		verify(keycloakTenantMigration, never()).provision(any());
	}

	@Test
	public void should_NotRunMigration_When_ThereAreNotMigratedFieldsInRegion() {
		mockIdpConfig(SecurityConfiguration.WSO_IDP);
		mockDefinition(Arrays.asList(KEYCLOAK_FIRST_NAME, KEYCLOAK_LAST_NAME, KEYCLOAK_EMAIL),
				Collections.singletonList(WSO_COUNTRY));

		observer.onDefinitionChange(null);

		verify(keycloakTenantMigration, never()).provision(any());
	}

	@Test(expected = EmfRuntimeException.class)
	public void should_ThrowException_When_TenantProvisioningFails() {
		mockIdpConfig(SecurityConfiguration.WSO_IDP);
		mockDefinition(Arrays.asList(KEYCLOAK_FIRST_NAME, KEYCLOAK_LAST_NAME, KEYCLOAK_EMAIL));

		when(tenantManager.getTenant(TENANT_ID)).thenReturn(Optional.empty());

		observer.onDefinitionChange(null);
	}

	@Test
	public void should_CorrectlyMigrate() {
		mockIdpConfig(SecurityConfiguration.WSO_IDP);
		mockDefinition(Arrays.asList(KEYCLOAK_FIRST_NAME, KEYCLOAK_LAST_NAME, KEYCLOAK_EMAIL));
		mockUsers();

		observer.onDefinitionChange(null);

		verifyMigrated();
	}

	@Test
	public void should_CorrectlyMigrate_WithMappingsInRegion() {
		mockIdpConfig(SecurityConfiguration.WSO_IDP);
		mockDefinition(Collections.singletonList(KEYCLOAK_EMAIL),
				Arrays.asList(KEYCLOAK_FIRST_NAME, KEYCLOAK_LAST_NAME, KEYCLOAK_COUNTRY));
		mockUsers();

		observer.onDefinitionChange(null);

		verifyMigrated();
	}

	private void verifyMigrated() {
		verifyTenantProvisioned();

		verify(keycloakTenantMigration).updateSessionConfig(1800);

		verify(keycloakTenantMigration).updateMailSettings();

		verifyUsersDeactivated();

		verifyTenantConfigs();
	}

	private void verifyTenantProvisioned() {
		ArgumentCaptor<IdpTenantInfo> argumentCaptor = ArgumentCaptor.forClass(IdpTenantInfo.class);
		verify(keycloakTenantMigration).provision(argumentCaptor.capture());

		IdpTenantInfo tenantInfo = argumentCaptor.getValue();
		assertEquals(TENANT_ID, tenantInfo.getTenantId());
		assertEquals(TENANT_DISPLAY_NAME, tenantInfo.getTenantDisplayName());
		assertEquals(TENANT_DESCRIPTION, tenantInfo.getTenantDescription());
	}

	private void verifyUsersDeactivated() {
		ArgumentCaptor<List> argumentCaptor = ArgumentCaptor.forClass(List.class);
		verify(keycloakTenantMigration).deactivateUsers(argumentCaptor.capture());
		assertEquals(2, argumentCaptor.getValue().size());
	}

	private void verifyTenantConfigs() {
		ArgumentCaptor<Collection> argumentCaptor = ArgumentCaptor.forClass(Collection.class);
		verify(configurationManagement).updateConfigurations(argumentCaptor.capture());
		assertEquals(3, argumentCaptor.getValue().size());
	}

	private void mockIdpConfig(String idp) {
		when(securityConfiguration.getIdpProviderName()).thenReturn(new ConfigurationPropertyMock<>(idp));
		when(userStoreAdapterProxy.getSynchronizationProviderName()).thenReturn(new ConfigurationPropertyMock<>(idp));
	}

	private void mockDefinition(List<String> dmsTypes) {
		mockDefinition(dmsTypes, Collections.emptyList());
	}

	private void mockDefinition(List<String> dmsTypes, List<String> regionDmsTypes) {
		DefinitionMock model = new DefinitionMock();
		for (String dmsType : dmsTypes) {
			model.getFields().add(buildField(dmsType));
		}

		for (String regionDmsType : regionDmsTypes) {
			RegionDefinitionImpl region = new RegionDefinitionImpl();
			region.getFields().add(buildField(regionDmsType));
			model.getRegions().add(region);
		}

		when(definitionService.getInstanceDefinition(Mockito.any(EmfUser.class))).thenReturn(model);
	}

	private static PropertyDefinition buildField(String dmsType) {
		PropertyDefinitionProxy field = new PropertyDefinitionProxy();
		field.setDmsType(dmsType);
		return field;
	}

	private void mockUsers() {
		when(resourceService.getAllUsers())
				.thenReturn(Arrays.asList(buildUser(true), buildUser(true), buildUser(false), buildUser(false)));
	}

	private EmfUser buildUser(boolean active) {
		EmfUser emfUser = new EmfUser();
		emfUser.setActive(active);
		return emfUser;
	}

}
