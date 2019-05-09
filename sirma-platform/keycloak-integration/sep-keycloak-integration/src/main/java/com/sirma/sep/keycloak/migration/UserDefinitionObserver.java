package com.sirma.sep.keycloak.migration;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.resources.synchronization.RemoteUserStoreAdapterProxy;
import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.context.ThreadFactories;
import com.sirma.itt.seip.tenant.context.Tenant;
import com.sirma.itt.seip.tenant.context.TenantManager;
import com.sirma.itt.seip.tenant.provision.IdpTenantInfo;
import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * Listens for changes in the user definition and if it finds that the mappings are migrated to Keycloak, runs migration
 * for the current tenant.
 * <p>
 * The migration steps consist of the following:
 * <ol>
 * <li>tenant provisioning</li>
 * <li>updates session timeout value</li>
 * <li>marks deactivated users as disabled in Keycloak</li>
 * <li>changes idp provider configurations to Keycloak</li>
 * <li>revokes the jwt tokens</li>
 * </ol>
 *
 * @author smustafov
 */
@ApplicationScoped
public class UserDefinitionObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final EmfUser USER_INSTANCE = new EmfUser();

	@Inject
	private UserPreferences userPreferences;

	@Inject
	private SecurityConfiguration securityConfiguration;

	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private ConfigurationManagement configurationManagement;

	@Inject
	private RemoteUserStoreAdapterProxy userStoreAdapterProxy;

	@Inject
	private JwtConfiguration jwtConfiguration;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private ResourceService resourceService;

	@Inject
	private KeycloakTenantMigration keycloakTenantMigration;

	@Inject
	private TenantManager tenantManager;

	private ExecutorService executorService;

	@PostConstruct
	void initialize() {
		executorService = Executors.newSingleThreadExecutor(
				ThreadFactories.createSystemThreadFactory(securityContextManager, ThreadFactories::asDaemon));
	}

	/**
	 * Listens for definitions change and runs the migration if the mappings in user definition are migrated to Keycloak.
	 *
	 * @param event definition change event
	 */
	public void onDefinitionChange(@Observes DefinitionsChangedEvent event) {
		if (shouldMigrate()) {
			String currentTenantId = securityContextManager.getCurrentContext().getCurrentTenantId();

			LOGGER.info("User definition changed to Keycloak. Starting migration for tenant: {}", currentTenantId);

			runMigration(currentTenantId);

			LOGGER.info("Successfully migrated tenant {} to Keycloak IdP", currentTenantId);
		}
	}

	private boolean shouldMigrate() {
		return isTenantOnWsoIdp() && isUserFieldsMappingMigrated();
	}

	private boolean isTenantOnWsoIdp() {
		return SecurityConfiguration.WSO_IDP.equals(securityConfiguration.getIdpProviderName().get());
	}

	private boolean isUserFieldsMappingMigrated() {
		return definitionService.getInstanceDefinition(USER_INSTANCE)
				.fieldsStream()
				.filter(PropertyDefinition.hasDmsType())
				.noneMatch(property -> property.getDmsType().contains(":"));
	}

	private void runMigration(String tenantId) {
		provisionTenant(tenantId);

		updateSessionConfig();

		updateMailSettings();

		deactivateMarkedUsers();

		changeTenantIdpConfig(tenantId);
	}

	private void provisionTenant(String tenantId) {
		// this is executed in the context of system tenant
		Future<?> future = executorService.submit(() -> provisionTenantTask(tenantId));
		try {
			future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new EmfRuntimeException("Failed tenant provisioning for keycloak", e);
		}
	}

	private void provisionTenantTask(String tenantId) {
		Tenant tenant = tenantManager.getTenant(tenantId)
				.orElseThrow(() -> new EmfRuntimeException("Tenant not found: " + tenantId));

		IdpTenantInfo idpTenantInfo = new IdpTenantInfo();
		idpTenantInfo.setTenantId(tenant.getTenantId());
		idpTenantInfo.setTenantDisplayName(tenant.getDisplayName());
		idpTenantInfo.setTenantDescription(tenant.getDescription());

		keycloakTenantMigration.provision(idpTenantInfo);
	}

	private void updateSessionConfig() {
		keycloakTenantMigration.updateSessionConfig(userPreferences.getSessionTimeout());
	}

	private void updateMailSettings() {
		keycloakTenantMigration.updateMailSettings();
	}

	private void deactivateMarkedUsers() {
		List<User> inactiveUsers = resourceService.getAllUsers()
				.stream()
				.filter(user -> !user.isActive())
				.collect(Collectors.toList());

		keycloakTenantMigration.deactivateUsers(inactiveUsers);
	}

	private void changeTenantIdpConfig(String tenantId) {
		Configuration idpProviderConfig = new Configuration(securityConfiguration.getIdpProviderName().getName(),
				SecurityConfiguration.KEYCLOAK_IDP, tenantId);
		Configuration syncProviderConfig = new Configuration(
				userStoreAdapterProxy.getSynchronizationProviderName().getName(), SecurityConfiguration.KEYCLOAK_IDP,
				tenantId);

		Configuration revocationConfig = new Configuration(jwtConfiguration.getRevocationTimeConfig().getName(),
				ISO8601DateFormat.format(new Date()), tenantId);

		configurationManagement
				.updateConfigurations(Arrays.asList(idpProviderConfig, syncProviderConfig, revocationConfig));
	}

}
