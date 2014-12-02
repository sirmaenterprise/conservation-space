package com.sirma.itt.cmf.alfresco4.services.convert;

import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.CacheConfigurations;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.util.Documentation;

/**
 * DMS converter types
 *
 * @author BBonev
 */
@CacheConfigurations({
		@CacheConfiguration(name = ConverterConstants.CACHE_PREFIX + ConverterConstants.CASE, container = "cmf", eviction = @Eviction(maxEntries = 100), expiration = @Expiration(maxIdle = 600000, interval = 60000), doc = @Documentation(""
				+ "Cache used to store the reverse property mapping for DMS property names conversions. For each unique case definition type. "
				+ "<br>Minimal value expression: caseDefinitions * 1.2")),
		@CacheConfiguration(name = ConverterConstants.CACHE_PREFIX + ConverterConstants.DOCUMENT, container = "cmf", eviction = @Eviction(maxEntries = 100), expiration = @Expiration(maxIdle = 600000, interval = 60000), doc = @Documentation(""
				+ "Cache used to store the reverse property mapping for DMS property names conversions. For each unique document definition type. "
				+ "<br>Minimal value expression: documentDefinitions * 1.2")),
		@CacheConfiguration(name = ConverterConstants.CACHE_PREFIX + ConverterConstants.TASK, container = "cmf", eviction = @Eviction(maxEntries = 100), expiration = @Expiration(maxIdle = 600000, interval = 60000), doc = @Documentation(""
				+ "Cache used to store the reverse property mapping for DMS property names conversions. For each unique task definition type. "
				+ "<br>Minimal value expression: taskDefinitions * 1.2")),
		@CacheConfiguration(name = ConverterConstants.CACHE_PREFIX + ConverterConstants.WORKFLOW, container = "cmf", eviction = @Eviction(maxEntries = 100), expiration = @Expiration(maxIdle = 600000, interval = 60000), doc = @Documentation(""
				+ "Cache used to store the reverse property mapping for DMS property names conversions. For each unique workflow definition type. "
				+ "<br>Minimal value expression: workflowDefinitions * 1.2")),
		@CacheConfiguration(name = ConverterConstants.CACHE_PREFIX + ConverterConstants.SECTION, container = "cmf", eviction = @Eviction(maxEntries = 100), expiration = @Expiration(maxIdle = 600000, interval = 60000), doc = @Documentation(""
				+ "Cache used to store the reverse property mapping for DMS property names conversions. For each unique section definition type. "
				+ "<br>Minimal value expression: workflowDefinitions * 1.2")),
		@CacheConfiguration(name = ConverterConstants.CACHE_PREFIX + ConverterConstants.GENERAL, container = "cmf", eviction = @Eviction(maxEntries = 100), expiration = @Expiration(maxIdle = 600000, interval = 60000), doc = @Documentation(""
				+ "Cache used to store the reverse property mapping for DMS property names conversions. For each unique workflow definition type. "
				+ "<br>Minimal value expression: workflowDefinitions * 1.2")), })
public interface ConverterConstants {

	/** The Constant CACHE_PREFIX. */
	String CACHE_PREFIX = "DMS_TYPE_CACHE_FOR_";

	/** The case. */
	String CASE = "CASE";

	/** The document. */
	String DOCUMENT = "DOCUMENT";

	/** The section. */
	String SECTION = "SECTION";

	/** The task. */
	String TASK = "TASK";

	/** The workflow. */
	String WORKFLOW = "WORKFLOW";

	/** The task. */
	String GENERAL = "GENERAL";
}
