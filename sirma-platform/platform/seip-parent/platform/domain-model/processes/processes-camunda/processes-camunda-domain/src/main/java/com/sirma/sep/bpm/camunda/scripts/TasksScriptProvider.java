package com.sirma.sep.bpm.camunda.scripts;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.script.ScriptInstance;

/**
 * Contains some common operation for tasks processing. Extends the Server side API for definitions scripts.
 *
 * @author A. Kunchev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 294)
public class TasksScriptProvider implements GlobalBindingsExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String[] EMPTY = new String[] {};
	@Override
	public Map<String, Object> getBindings() {
		return Collections.<String, Object> singletonMap("tasks", this);

	}

	@Override
	public Collection<String> getScripts() {
		return Collections.emptyList();
	}

	/**
	 * Used to retrieve task assignees. The properties where assignees should be searched are passed as parameter.
	 *
	 * @param instance
	 *            the instance in which properties will be searched
	 * @param properties
	 *            the properties which values will be searched
	 * @return string array with the collected users
	 */
	@SuppressWarnings("unchecked")
	public String[] getTaskAssignees(ScriptInstance instance, String[] properties) {
		if (instance == null) {
			LOGGER.warn("The passed instance was null. Empty collection will be returned.");
			return EMPTY;
		}
		Set<String> result = new HashSet<>();
		for (String property : properties) {
			Serializable value = instance.get(property);
			if (value instanceof String) {
				result.add((String) value);
			} else if (value instanceof Collection<?>) {
				result.addAll((Collection<String>) value);
			}
		}
		return CollectionUtils.toArray(result, String.class);
	}
}
