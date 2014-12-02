package com.sirma.itt.idoc.web.util;

import com.sirma.itt.emf.annotation.Optional;
import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.util.Documentation;

/**
 * Configuration key constants for Idoc.
 * 
 * @author yasko
 * 
 */
@Documentation("Configuration property keys used in idoc module.")
public final class IntelligentDocumentConfigurationProperties implements Configuration {
	
	/**
	 * Private constructor.
	 */
	private  IntelligentDocumentConfigurationProperties() {
		
	}
	
	/**
	 * Time format pattern to be used in JS.
	 */
	@Optional
	@Documentation("Time format pattern to be used in JS.")
	public static final String JS_TIME_FORMAT = "js.time.format";

	/**
	 * Interval in milliseconds in which a draft verion of the intelligent
	 * document will be saved.
	 */
	@Optional
	@Documentation("Interval in milliseconds in which a draft verion of the intelligent document will be saved.")
	public static final String DRAFT_AUTOSAVE_INTERVAL_MILLIS = "idoc.draftAutosaveIntervalMillis";

}
