package com.sirmaenterprise.sep.eai.spreadsheet.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.instance.context.InstanceContextService;

/**
 * Defines the eai specific system properties.
 * 
 * @author gshevkedov
 */
public class EAISystemProperties {
	/** Import status data entry value. */
	public static final String IMPORT_STATUS = "ImportStatus";
	/** Content source data entry value */
	public static final String CONTENT_SOURCE = "contentSource";
	/** Parent to child relation. */
	public static final String PART_OF = InstanceContextService.PART_OF_URI;
	/** sep content id. */
	public static final String PRIMARY_CONTENT_ID = DefaultProperties.PRIMARY_CONTENT_ID;

	private static final Set<String> SYSTEM_PROPERTIES_SET = new HashSet<>(
			Arrays.asList(IMPORT_STATUS, CONTENT_SOURCE, PRIMARY_CONTENT_ID, PART_OF));

	private EAISystemProperties() {
		// constants class
	}

	/**
	 * Check if property is considered eai system property
	 * 
	 * @param property
	 *            the property to test
	 * @return true if property is system
	 */
	public static boolean isSystemProperty(String property) {
		return SYSTEM_PROPERTIES_SET.contains(property);
	}

}
