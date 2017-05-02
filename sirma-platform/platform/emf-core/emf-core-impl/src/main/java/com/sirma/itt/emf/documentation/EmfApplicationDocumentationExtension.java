package com.sirma.itt.emf.documentation;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONObject;

import com.sirma.itt.emf.extensions.DocumentationExtension;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.plugin.Plugin;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Extension that builds documentation information about
 * <ul>
 * <li>configurations - all possible configurations with their explanations
 * <li>activeConfigurations - the currently configured/active configurations
 * <li>exampleConfiguration - example configuration file based on the default values for all configurations
 * <li>events - the list of all registered system events
 * <li>plugins - the list of all available plugins
 * </ul>
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ApplicationDocumentationExtension.TARGET_NAME, priority = 1)
public class EmfApplicationDocumentationExtension implements ApplicationDocumentationExtension {

	private static final String CLASS_NAME = "className";
	private static final String EXAMPLE_CONFIGURATION = "exampleConfiguration";
	private static final String PLUGINS = "plugins";
	private static final String EVENTS = "events";
	private static final String ACTIVE_CONFIGURATIONS = "activeConfigurations";
	private static final String CONFIGURATIONS = "configurations";

	/** The documentation extension. */
	@Inject
	private DocumentationExtension documentationExtension;

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

	@Override
	public String generate(String option) {
		if (EqualsHelper.nullSafeEquals(option, EVENTS, true)) {
			return listClasses(EVENTS, EmfEvent.class);
		} else if (EqualsHelper.nullSafeEquals(option, PLUGINS, true)) {
			return listClasses(PLUGINS, Plugin.class);
		}
		return "{\"error\": \"Option not supported: " + option + "\"}";
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
		List<JSONObject> annotations = new LinkedList<>();
		for (Class<?> c : classes) {
			JSONObject jsonObject = new JSONObject();
			JsonUtil.addToJson(jsonObject, CLASS_NAME, c.getCanonicalName());
			JsonUtil.addToJson(jsonObject, "doc", c.getAnnotation(Documentation.class).value());
			annotations.add(jsonObject);
		}
		JSONObject result = new JSONObject();
		JsonUtil.addToJson(result, base, annotations);
		return result.toString();
	}

}
