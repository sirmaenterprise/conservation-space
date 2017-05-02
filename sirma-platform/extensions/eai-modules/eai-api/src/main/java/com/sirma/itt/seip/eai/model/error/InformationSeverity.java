package com.sirma.itt.seip.eai.model.error;

/**
 * The {@link InformationSeverity} enum is the logging message severity - based on ICD specification.
 * 
 * @author bbanchev
 */
public enum InformationSeverity {
	/** fatal code. */
	FATAL,
	/** error code. */
	ERROR,
	/** warn code. */
	WARN,
	/** info code. */
	INFO,
	/** debug code. */
	DEBUG,
	/** trace code. */
	TRACE;
}
