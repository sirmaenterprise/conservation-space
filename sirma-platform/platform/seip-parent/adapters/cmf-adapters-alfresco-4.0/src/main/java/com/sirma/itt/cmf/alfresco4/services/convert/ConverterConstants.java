package com.sirma.itt.cmf.alfresco4.services.convert;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.Expiration;

/**
 * DMS converter types
 *
 * @author BBonev
 */
@CacheConfiguration(name = ConverterConstants.CACHE_PREFIX
		+ ConverterConstants.GENERAL, eviction = @Eviction(maxEntries = 100) , expiration = @Expiration(maxIdle = 600000, interval = 60000) , doc = @Documentation(""
				+ "Cache used to store the reverse property mapping for DMS property names conversions. For each unique workflow definition type. "
				+ "<br>Minimal value expression: workflowDefinitions * 1.2") )
public interface ConverterConstants {

	/** The Constant CACHE_PREFIX. */
	String CACHE_PREFIX = "DMS_TYPE_CACHE_FOR_";

	/** The default converter type. */
	String GENERAL = "GENERAL";
}
