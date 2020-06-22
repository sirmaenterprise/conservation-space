package com.sirma.itt.emf.info;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the {@link VersionInfo} extension that loads a properties file located on the class path.
 *
 * @author BBonev
 */
public abstract class AbstractVersionInfo implements VersionInfo {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractVersionInfo.class);

	protected static final String[] SKIPPED_INFO_KEYS = { "m2e.projectLocation", "m2e.projectName" };

	@Override
	public String getFileInfoLocation() {
		String locationPath = getLocationPath();
		if (locationPath == null) {
			return null;
		}
		URL resource = this.getClass().getClassLoader().getResource(locationPath);
		if (resource != null) {
			return resource.toString();
		}
		return null;
	}

	/**
	 * Gets the location path of the properties file that points the location.
	 *
	 * @return the location path
	 */
	protected abstract String getLocationPath();

	@Override
	public Properties getFileInfo() {
		Properties props = new Properties();
		try {
			String location = getLocationPath();
			if (location != null) {
				try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(location)) {
					if (stream == null) {
						LOGGER.warn("No properties file found at location: {}", location);
						return props;
					}
					props.load(stream);

					filterProperties(props);
				}
			}
		} catch (IOException e) {
			LOGGER.error("Failed to load version resource file: ", e);
		}

		return props;
	}

	private void filterProperties(Properties properties) {
		for (String key : SKIPPED_INFO_KEYS) {
			properties.remove(key);
		}
	}
}
