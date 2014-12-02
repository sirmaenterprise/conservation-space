package com.sirma.itt.pm.alfresco4.services.convert;

import com.sirma.itt.cmf.alfresco4.services.convert.ConverterConstants;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.util.Documentation;

/**
 * DMS converter types
 * 
 * @author BBonev
 */
public interface PmConverterContants extends ConverterConstants {

	/** The project. */
	@CacheConfiguration(name = ConverterConstants.CACHE_PREFIX + PmConverterContants.PROJECT, container = "pm", eviction = @Eviction(maxEntries = 100), expiration = @Expiration(maxIdle = 600000, interval = 60000), doc = @Documentation(""
			+ "Cache used to store the reverse property mapping for DMS property names conversions. For each unique project definition type. "
			+ "<br>Minimal value expression: projectDefinitions * 1.2"))
	String PROJECT = "PROJECT";
}
