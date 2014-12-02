package com.sirma.itt.objects.alfresco4.services.converter;

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
public interface ObjectConverterContants extends ConverterConstants {

	/** The object. */
	@CacheConfiguration(name = ConverterConstants.CACHE_PREFIX + ObjectConverterContants.OBJECT, container = "obj", eviction = @Eviction(maxEntries = 100), expiration = @Expiration(maxIdle = 600000, interval = 60000), doc = @Documentation(""
			+ "Cache used to store the reverse property mapping for DMS property names conversions. For each unique object definition type. "
			+ "<br>Minimal value expression: objectDefinitions * 1.2"))
	String OBJECT = "OBJECT";
}
