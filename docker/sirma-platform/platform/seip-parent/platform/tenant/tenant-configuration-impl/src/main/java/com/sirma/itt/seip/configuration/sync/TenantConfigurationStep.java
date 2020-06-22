package com.sirma.itt.seip.configuration.sync;

import static org.apache.commons.lang.StringUtils.trimToNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.build.ConfigurationInstanceProvider;
import com.sirma.itt.seip.configuration.build.RawConfigurationAccessor;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantDeletionContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;

/**
 * This step inserts all configurations for the tenant that are not configured by other steps. This
 * should be one of the last steps.
 *
 * @author BBonev
 */
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 20)
@Extension(target = TenantStep.DELETION_STEP_NAME, order = 17)
public class TenantConfigurationStep extends AbstractTenantStep {

	@Inject
	private RawConfigurationAccessor configurationAccessor;
	@Inject
	private SecurityContextManager contextManager;
	@Inject
	private ConfigurationManagement configurationManagement;
	@Inject
	private ConfigurationInstanceProvider configurationInstanceProvider;

	@Override
	public String getIdentifier() {
		return "Configurations";
	}

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		String tenantId = context.getTenantInfo().getTenantId();
		Map<String, Serializable> properties = data.getProperties();
		contextManager.executeAsTenant(tenantId).biConsumer(this::syncConfigurations, properties, tenantId);
		return true;
	}

	@Override
	public boolean delete(TenantStepData data, TenantDeletionContext context) {
		contextManager.executeAsTenant(context.getTenantInfo().getTenantId())
				.executable(configurationManagement::removeAllConfigurations);
		return true;
	}

	void syncConfigurations(Map<String, Serializable> properties, String tenantId) {

		// insert any configurations passed via the create tenant configuration
		Collection<Configuration> toAdd = configurationInstanceProvider.getRegisteredConfigurations().stream()
				.filter(properties::containsKey)
				.map(key -> new Configuration(key, (String) properties.get(key), tenantId))
				.collect(Collectors.toList());
		configurationManagement.addConfigurations(toAdd);
	}

	@Override
	protected InputStream provideExternalModel() {

		List<JSONObject> properties = configurationInstanceProvider.getAllInstances().stream()
				.filter(tenantConfigFilter()).map(confInstance -> toJson(confInstance)).collect(Collectors.toList());
		JSONArray jsonArray = new JSONArray(properties);

		JSONObject data = new JSONObject();
		JsonUtil.addToJson(data, TenantStepData.KEY_ID, getIdentifier());
		JsonUtil.addToJson(data, TenantStepData.KEY_PROPERTIES_LIST, jsonArray);
		return new ByteArrayInputStream(data.toString().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Skip group and system configurations
	 *
	 * @return the predicate
	 */
	private static Predicate<ConfigurationInstance> tenantConfigFilter() {
		return c -> !c.isComplex() && !c.isSystemConfiguration();
	}

	private JSONObject toJson(ConfigurationInstance configuration) {
		String value = configurationAccessor.getRawConfigurationValue(configuration.getName());
		if (value == null) {
			value = trimToNull(((ConfigurationPropertyDefinition) configuration.getAnnotation()).defaultValue());
		}
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, TenantStepData.KEY_ID, configuration.getName());
		JsonUtil.addToJson(object, TenantStepData.KEY_VALUE, value);
		setTypeAndValidator(configuration, object);
		JsonUtil.addToJson(object, "required", "false");
		JsonUtil.addToJson(object, "label", "label." + configuration.getName());
		JsonUtil.addToJson(object, "tooltip", configuration.getLabel());
		return object;
	}

	/**
	 * Sets the type and validator.
	 *
	 * @param configuration
	 *            the configuration
	 * @param object
	 *            the object
	 */
	private static void setTypeAndValidator(ConfigurationInstance configuration, JSONObject object) {
		String type = "text";
		String validator = "^.+$";
		if (Number.class.isAssignableFrom(configuration.getType())) {
			type = "number";
			validator = "^[\\d\\.]+$";
		} else if (Boolean.class.isAssignableFrom(configuration.getType())) {
			type = "boolean";
			validator = "true|false";
		}
		JsonUtil.addToJson(object, "type", type);
		JsonUtil.addToJson(object, "validator", validator);
	}

}