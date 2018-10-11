package com.sirma.itt.seip.script.extensions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.io.FileExtensionFilter;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;

/**
 * {@link GlobalBindingsExtension} implementation to preload external scripts located in a folder configured via
 * application property.
 *
 * @author BBonev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 100)
public class FolderScriptProvider implements GlobalBindingsExtension {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FolderScriptProvider.class);

	private static final FileExtensionFilter JS_FILTER = new FileExtensionFilter("js");

	/**
	 * Folder on the server to read and preload javascript files for the server side script engine. The files should
	 * have .js file extension in order to be loaded.
	 */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "script.externalLocation", defaultValue = ".", sensitive = true, type = File.class, label = "Folder on the server to read and preload javascript files for the server side script engine. The files should have .js file extension in order to be loaded.")
	private ConfigurationProperty<File> locationConfig;

	@Override
	public Map<String, Object> getBindings() {
		return Collections.emptyMap();
	}

	@Override
	public Collection<String> getScripts() {
		File location = locationConfig.get();
		if (location == null || !location.canRead()) {
			return Collections.emptyList();
		}
		LOGGER.trace("Scanning {} for java script files to load.", location.getAbsolutePath());
		List<String> scripts = Collections.emptyList();
		if (location.isFile() && JS_FILTER.accept(null, location.getName())) {
			String script = readFile(location);
			if (StringUtils.isNotBlank(script)) {
				scripts = Collections.singletonList(script);
			}
		} else if (location.isDirectory()) {
			File[] files = location.listFiles(JS_FILTER);
			scripts = readFiles(files);

		}
		return scripts;
	}

	private static List<String> readFiles(File[] files) {
		if (files == null) {
			return Collections.emptyList();
		}
		List<String> scripts = new ArrayList<>(files.length);
		for (File file : files) {
			String script = readFile(file);
			if (StringUtils.isNotBlank(script)) {
				scripts.add(script);
			}
		}
		return scripts;
	}

	private static String readFile(File file) {
		try (InputStream input = new FileInputStream(file)) {
			LOGGER.debug("Reading external JS file {}", file);
			return IOUtils.toString(new InputStreamReader(input, StandardCharsets.UTF_8));
		} catch (IOException e) {
			LOGGER.warn("Failed to load script file {}", file, e);
		}
		return null;
	}

}
