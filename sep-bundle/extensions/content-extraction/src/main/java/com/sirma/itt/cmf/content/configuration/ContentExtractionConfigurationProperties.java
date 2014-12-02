/*
 *
 */
package com.sirma.itt.cmf.content.configuration;

import com.sirma.itt.emf.annotation.Optional;
import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.util.Documentation;

/**
 * Content extraction configuration properties.
 *
 * @author bbanchev
 */
@Documentation("Base content extraction configuration properties.")
public interface ContentExtractionConfigurationProperties extends Configuration {
	/** The system language. */
	@Documentation("Location for the externally managed jar file containing a tika extraction executor!.Default value is null")
	@Optional
	String CONTENT_EXTRACT_TIKA_LOCATION = "content.extract.tika.location";

}
