package com.sirma.itt.seip.instance.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;

/**
 * Adds logging to server side scripts. This is done via added a global binding with id log and methods that accept
 * single string.
 *
 * @author BBonev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 1.005)
public class ScriptImporter implements GlobalBindingsExtension {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptImporter.class);
	private static final String IMPORT_FUNCTION = "import.js";

	@Inject
	private DefinitionService definitionService;

	@Override
	public Map<String, Object> getBindings() {
		return Collections.<String, Object> singletonMap("importer", this);
	}

	@Override
	public Collection<String> getScripts() {
		return ResourceLoadUtil.loadResources(getClass(), IMPORT_FUNCTION);
	}

	/**
	 * Load script from a definition by script path.
	 *
	 * @param path
	 *            the path to script to load
	 * @return the script or <code>null</code> if not found.
	 */
	public String[] loadScript(String path) {
		String rootPath = PathHelper.extractRootPath(path);
		DefinitionModel definition = definitionService.find(rootPath);
		if (definition != null) {
			if (path.endsWith("*")) {
				return loadAllScripts(definition);
			}
			Identity identity = PathHelper.iterateByPath(definition, path);
			if (identity instanceof PropertyDefinition) {
				String script = ((PropertyDefinition) identity).getDefaultValue();
				script = StringUtils.trimToEmpty(script);
				if (isValidScript(script)) {
					LOGGER.debug("Going to load a script dinamically identified by path: {}", path);
					LOGGER.trace("Going to load a script\n: {}", script);
					return new String[] { script };
				}
				return null;
			}
		}
		LOGGER.warn("Could not find script to load: {}", path);
		return null;
	}

	private static String[] loadAllScripts(DefinitionModel definition) {
		List<String> scripts = new ArrayList<>(definition.getFields().size());
		for (PropertyDefinition property : definition.getFields()) {
			String script = property.getDefaultValue();
			script = StringUtils.trimToEmpty(script);
			if (isValidScript(script)) {
				LOGGER.debug("Going to load a script dinamically identified by path: {}", PathHelper.getPath(property));
				LOGGER.trace("Going to load a script\n: {}", script);
				scripts.add(script);
			}
		}
		return scripts.toArray(new String[scripts.size()]);
	}

	private static boolean isValidScript(String script) {
		return !script.isEmpty();
	}
}
