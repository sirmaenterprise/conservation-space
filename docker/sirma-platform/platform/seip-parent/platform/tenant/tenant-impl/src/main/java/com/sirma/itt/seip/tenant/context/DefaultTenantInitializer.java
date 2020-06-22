/**
 *
 */
package com.sirma.itt.seip.tenant.context;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tenant.TenantManagementService;
import com.sirma.itt.seip.tenant.exception.TenantValidationException;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationModelBuilder;

/**
 * Default tenant initialization via external config or system properties.
 *
 * @author BBonev
 */
public class DefaultTenantInitializer {

	private static final String INITIAL_TENANT_CONFIG = "default.tenant.cfg";

	@Inject
	private TenantManagementService tenantCreationService;

	@Inject
	private TenantInitializationModelBuilder modelBuilder;

	/**
	 * Try creating default tenant.
	 *
	 * @throws TenantValidationException
	 *             the tenant validation exception
	 */
	public void tryCreatingDefaultTenant() throws TenantValidationException {
		String tenantConfig = StringUtils.trimToNull(System.getProperty(INITIAL_TENANT_CONFIG));
		boolean isExternalConfigValid = tenantConfig != null && new File(tenantConfig).isFile();
		if (isExternalConfigValid) {
			try {
				initializeTenantFromConfig(tenantConfig);
			} catch (IOException e) {
				throw new TenantValidationException("Could not create tenant because: ", e);
			}
		} else {
			String tenantId = StringUtils.trimToNull(System.getProperty(SecurityContext.DEFAULT_TENANT_ID_KEY));
			if (tenantId == null) {
				return;
			}
			JSONObject jsonObject = new JSONObject();
			JSONObject initConfig = new JSONObject();
			JsonUtil.addToJson(initConfig, "id", "TenantInitialization");
			JSONObject property = new JSONObject();
			JsonUtil.addToJson(property, "id", "tenantid");
			JsonUtil.addToJson(property, "value", tenantId);
			JsonUtil.append(initConfig, "properties", property);
			JsonUtil.append(jsonObject, "data", initConfig);
			createTenantUsingConfig(jsonObject);
		}
	}

	private void initializeTenantFromConfig(String tenantConfig) throws IOException, TenantValidationException {
		String string = FileUtils.readFileToString(new File(tenantConfig), StandardCharsets.UTF_8);
		if (StringUtils.trimToNull(string) == null) {
			FileUtils.write(new File(tenantConfig), tenantCreationService.provideModel().toJSONObject().toString());
			throw new TenantValidationException("Provided empty file for tenant initialization."
					+ " Written default configuration tree in the file! Fill the configurations and run the server again");
		}
		boolean multipleConfigs = JsonUtil.isArray(string);
		if (multipleConfigs) {
			createMultiple(string, tenantConfig);
		} else {
			JSONObject config = JsonUtil.createObjectFromString(string);
			if (config == null) {
				throw new TenantValidationException("Invalid tenant configuration located at " + tenantConfig);
			}
			createTenantUsingConfig(config);
		}
	}

	/**
	 * Create multiple tenants from the given configurations.
	 *
	 * @param data
	 *            the data
	 * @param path
	 *            the path
	 * @throws TenantValidationException
	 *             the tenant validation exception
	 */
	private void createMultiple(String data, String path) throws TenantValidationException {
		JSONArray configs = JsonUtil.createArrayFromString(data);
		if (configs == null) {
			throw new TenantValidationException("Invalid tenant configuration located at " + path);
		}
		for (int i = 0; i < configs.length(); i++) {
			JSONObject config = JsonUtil.getFromArray(configs, i, JSONObject.class);
			createTenantUsingConfig(config);
		}
	}

	private void createTenantUsingConfig(JSONObject config) {
		tenantCreationService.create(modelBuilder.getBuilder().setModel(config).build());
	}

}
