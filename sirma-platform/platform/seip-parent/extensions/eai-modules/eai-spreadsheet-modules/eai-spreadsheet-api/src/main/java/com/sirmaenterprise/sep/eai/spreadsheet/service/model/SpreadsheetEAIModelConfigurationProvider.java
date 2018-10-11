package com.sirmaenterprise.sep.eai.spreadsheet.service.model;

import com.sirma.itt.seip.eai.exception.EAIModelException;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;

/**
 * Interface defining {@link ModelConfiguration} provider for spreadsheet processing.
 * 
 * @author bbanchev
 */
@FunctionalInterface
public interface SpreadsheetEAIModelConfigurationProvider {

	/**
	 * Provide {@link ModelConfiguration} for spreadsheet processing. The underlying provisioning mechanism could be
	 * arbitrary.
	 *
	 * @return the model configuration or exception of failure
	 * @throws EAIModelException
	 *             on model parsing failure
	 */
	ModelConfiguration provideModel() throws EAIModelException;

}