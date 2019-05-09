package com.sirma.sep.instance.batch;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.enterprise.inject.Alternative;

/**
 * Fake alternative of the {@link BatchProperties} so we can store and retrieve the run properties
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 17/01/2019
 */
@Alternative
public class BatchPropertiesFake extends BatchProperties {

	private static Map<Long, Properties> propertiesMapping = new HashMap<>();

	@Override
	public String getJobProperty(long executionId, String key, String defaultValue) {
		return propertiesMapping.getOrDefault(executionId, new Properties()).getProperty(key, defaultValue);
	}

	static void addProperties(long executionId, Properties properties) {
		propertiesMapping.put(executionId, properties);
	}
}
