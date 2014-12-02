package com.sirma.itt.emf.documentation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.configuration.SystemConfiguration;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.extensions.DocumentationExtension;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.util.ReflectionUtils;

/**
 * Extension that builds documentation information about
 * <ul>
 * <li>configurations - all possible configurations with their explanations
 * <li>activeConfigurations - the currently configured/active configurations
 * <li>exampleConfiguration - example configuration file based on the default values for all
 * configurations
 * <li>events - the list of all registered system events
 * <li>plugins - the list of all available plugins
 * </ul>
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ApplicationDocumentationExtension.TARGET_NAME, priority = 1)
public class EmfApplicationDocumentationExtension implements ApplicationDocumentationExtension {

	private static final String BR = "<br>#";
	private static final String CLASS_NAME = "className";
	private static final String EXAMPLE_CONFIGURATION = "exampleConfiguration";
	private static final String PLUGINS = "plugins";
	private static final String EVENTS = "events";
	private static final String ACTIVE_CONFIGURATIONS = "activeConfigurations";
	private static final String CONFIGURATIONS = "configurations";

	/** The documentation extension. */
	@Inject
	private DocumentationExtension documentationExtension;

	@Inject
	private Instance<SystemConfiguration> configuration;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> generationOptions() {
		Set<String> set = CollectionUtils.createHashSet(5);
		set.add(CONFIGURATIONS);
		set.add(ACTIVE_CONFIGURATIONS);
		set.add(EVENTS);
		set.add(PLUGINS);
		set.add(EXAMPLE_CONFIGURATION);
		return set;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String generate(String option) {
		if (EqualsHelper.nullSafeEquals(option, CONFIGURATIONS, true)) {
			return listClasses(CONFIGURATIONS, Configuration.class);
		} else if (EqualsHelper.nullSafeEquals(option, EVENTS, true)) {
			return listClasses(EVENTS, EmfEvent.class);
		} else if (EqualsHelper.nullSafeEquals(option, PLUGINS, true)) {
			return listClasses(PLUGINS, Plugin.class);
		} else if (EqualsHelper.nullSafeEquals(option, EXAMPLE_CONFIGURATION, true)) {
			return generateDoc(EXAMPLE_CONFIGURATION, Configuration.class);
		} else if (EqualsHelper.nullSafeEquals(option, ACTIVE_CONFIGURATIONS, true)) {
			return buildActiveConfigurations();
		}
		return "{\"error\": \"Option not supported: " + option + "\"}";
	}

	/**
	 * Builds the active configurations.
	 * 
	 * @return the string
	 */
	private String buildActiveConfigurations() {
		SystemConfiguration systemConfiguration = configuration.get();
		Set<String> keys = systemConfiguration.getConfigurationKeys();
		List<JSONObject> annotations = new LinkedList<JSONObject>();
		List<String> list = new ArrayList<String>(keys);
		Collections.sort(list);
		Set<String> sortedKeys = new LinkedHashSet<String>(list);
		for (String key : sortedKeys) {
			JSONObject object = new JSONObject();
			String value = systemConfiguration.getConfiguration(key);
			if ((key.toLowerCase().contains("pass") || key.toLowerCase().contains("key"))
					&& StringUtils.isNotNullOrEmpty(value)) {
				value = value.replaceAll(".", "*");
			}
			if (StringUtils.isNullOrEmpty(value)) {
				value = "[not set]";
			}
			JsonUtil.addToJson(object, CLASS_NAME, "<b>" + key + "</b>=" + value);
			JsonUtil.addToJson(object, "doc", "");
			annotations.add(object);
		}
		JSONObject result = new JSONObject();
		JsonUtil.addToJson(result, ACTIVE_CONFIGURATIONS, annotations);
		return result.toString();
	}

	/**
	 * List classes.
	 * 
	 * @param base
	 *            the base
	 * @param targetClass
	 *            the target class
	 * @return the string
	 */
	private String listClasses(String base, Class<?> targetClass) {
		List<Class<?>> classes = documentationExtension.getTypedClasses(targetClass);
		try {
			List<JSONObject> annotations = new LinkedList<JSONObject>();
			for (Class<?> c : classes) {
				if (targetClass.equals(Configuration.class)) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put(CLASS_NAME, "<b>" + c.getCanonicalName() + "</b>");
					jsonObject.put("doc", c.getAnnotation(Documentation.class).value());
					annotations.add(jsonObject);

					Field[] fields = c.getFields();
					for (Field field : fields) {
						Documentation annotation = field.getAnnotation(Documentation.class);
						Object value = ReflectionUtils.getFieldValue(field, c);
						if ((annotation != null) && (value != null)) {
							jsonObject = new JSONObject();
							jsonObject.put(CLASS_NAME, field.getName() + "=<b>" + value + "</b>");
							jsonObject.put("doc", annotation.value());
							annotations.add(jsonObject);
						}
					}
				} else {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put(CLASS_NAME, c.getCanonicalName());
					jsonObject.put("doc", c.getAnnotation(Documentation.class).value());
					annotations.add(jsonObject);
				}
			}
			JSONObject result = new JSONObject();
			result.put(base, annotations);
			return result.toString();
		} catch (JSONException e) {
			return "{\"error\": \"" + e.getMessage() + "\"}";
		}
	}

	/**
	 * Generate doc.
	 * 
	 * @param base
	 *            the base
	 * @param targetClass
	 *            the target class
	 * @return the string
	 */
	private String generateDoc(String base, Class<?> targetClass) {
		List<Class<?>> classes = documentationExtension.getTypedClasses(targetClass);
		try {
			List<JSONObject> annotations = new LinkedList<JSONObject>();
			for (Class<?> c : classes) {
				if (targetClass.equals(Configuration.class)) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put(CLASS_NAME, "#<br># "
							+ c.getAnnotation(Documentation.class).value().toUpperCase() + BR);
					jsonObject.put("doc", "");
					annotations.add(jsonObject);

					Field[] fields = c.getFields();
					for (Field field : fields) {
						Documentation annotation = field.getAnnotation(Documentation.class);
						Object value = ReflectionUtils.getFieldValue(field, c);
						if ((annotation != null) && (value != null)) {
							jsonObject = new JSONObject();
							jsonObject.put(
									CLASS_NAME,
									"#" + annotation.value().replaceAll("<p>|<br>", BR)
											+ "<br>" + value + "="
											+ getDefaultValue(annotation.value()));
							jsonObject.put("doc", "");
							annotations.add(jsonObject);
						}
					}
				} else {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put(CLASS_NAME, c.getCanonicalName());
					jsonObject.put("doc", c.getAnnotation(Documentation.class).value());
					annotations.add(jsonObject);
				}
			}
			JSONObject result = new JSONObject();
			result.put(base, annotations);
			return result.toString();
		} catch (JSONException e) {
			return "{\"error\": \"" + e.getMessage() + "\"}";
		}
	}

	/**
	 * Gets the default value.
	 * 
	 * @param value
	 *            the value
	 * @return the default value
	 */
	private String getDefaultValue(String value) {
		// this could be optimized with regular expression
		int defIndex = value.toLowerCase().indexOf("default value");
		if (defIndex < 0) {
			return "";
		}

		int indexOf = value.indexOf(":", defIndex);
		if (indexOf > 0) {
			String defValue = value.substring(indexOf + 1, value.length());
			defValue = defValue.replaceAll("<b>|</b>", "");
			return defValue.trim();
		}
		return "";
	}

}
